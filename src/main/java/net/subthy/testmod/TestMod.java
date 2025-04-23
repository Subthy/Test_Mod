package net.subthy.testmod;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.subthy.BossConfig;
import net.subthy.testmod.block.ModBlocks;
import net.subthy.testmod.effects.ModEffects;
import net.subthy.testmod.enchantment.ModEnchantments;
import net.subthy.testmod.items.ModItems;
import net.subthy.testmod.util.ModCreativeModeTabs;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;

@Mod(TestMod.MOD_ID)
@Mod.EventBusSubscriber(modid = TestMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TestMod {
    public static final String MOD_ID = "testmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Map<String, BossConfig> BOSSES = new HashMap<>();
    private static final Map<UUID, RespawnTask> respawnTasks = new HashMap<>();

    public TestMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEffects.register(modEventBus);
        ModEnchantments.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        loadBossConfigs();
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {}

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("spawn_boss")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    BossConfig config = BOSSES.get(id);
                                    if (config == null) return 0;

                                    ServerLevel level = ctx.getSource().getLevel();
                                    BlockPos position = new BlockPos((int) ctx.getSource().getPosition().x, (int) ctx.getSource().getPosition().y, (int) ctx.getSource().getPosition().z);
                                    EntityType<?> bossType = EntityType.byString(config.entity).orElse(null);
                                    if (bossType == null || !(bossType.create(level) instanceof Mob boss)) return 0;

                                    boss.setPos(position.getX(), position.getY(), position.getZ());
                                    boss.getPersistentData().putBoolean("SpawnedByCommand", true);
                                    level.addFreshEntity(boss);
                                    boss.getPersistentData().putBoolean("PendingMinionSpawn", true);
                                    return 1;
                                }))
        );
    }

    private static void applyBossLogic(ServerLevel level, Mob boss, BossConfig config) {
        List<String> types = config.minionTypes;
        for (int i = 0; i < config.maxMinions; i++) {
            String minionId = types.get(i % types.size());
            EntityType<?> minionType = EntityType.byString(minionId).orElse(null);
            if (minionType != null && minionType.create(level) instanceof Mob minion) {
                minion.setPos(boss.getX() + i, boss.getY(), boss.getZ() + i);
                minion.setCustomName(Component.literal("Minion").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF00))));
                minion.setCustomNameVisible(true);
                minion.getPersistentData().putUUID("BossUUID", boss.getUUID());
                minion.getPersistentData().putInt("RespawnTicks", config.respawnTicks);
                level.addFreshEntity(minion);
                boss.getPersistentData().putBoolean("PendingMinionSpawn", true);

            }
        }
    }




    private void spawnMinion(ServerLevel level, Mob boss, EntityType<?> minionType, int offsetX, int offsetZ, int respawnTicks) {
        if (minionType != null && minionType.create(level) instanceof Mob minion) {
            minion.setPos(boss.getX() + offsetX, boss.getY(), boss.getZ() + offsetZ);
            minion.setCustomName(Component.literal("Minion").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF00))));
            minion.setCustomNameVisible(true);
            minion.getPersistentData().putUUID("BossUUID", boss.getUUID());
            minion.getPersistentData().putInt("RespawnTicks", respawnTicks);
            level.addFreshEntity(minion);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMinionDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;

        if (event.getEntity() instanceof Mob minion && minion.getCustomName() != null && "Minion".equals(minion.getCustomName().getString())) {
            UUID minionId = minion.getUUID();

            if (minion.getPersistentData().contains("BossUUID")) {
                UUID bossUUID = minion.getPersistentData().getUUID("BossUUID");
                int ticks = minion.getPersistentData().getInt("RespawnTicks");
                if (ticks <= 0) ticks = 600;
                respawnTasks.put(minionId, new RespawnTask(level, minion.blockPosition(), minion.getType(), bossUUID, ticks));
                LOGGER.debug("RespawnTask added for minion: UUID={}, Position={}, TicksRemaining={}", minionId, minion.blockPosition(), ticks);
            } else {
                LOGGER.warn("Minion {} died but has no BossUUID tag!", minionId);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Mob mob && mob.getCustomName() != null && "Minion".equals(mob.getCustomName().getString())) {
            event.getDrops().clear();
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !respawnTasks.isEmpty()) {
            LOGGER.debug("Ticking {} respawn task(s)", respawnTasks.size());

            respawnTasks.entrySet().removeIf(entry -> {
                RespawnTask task = entry.getValue();
                Mob boss = (Mob) task.level.getEntity(task.bossUUID);

                if (boss == null || !boss.isAlive()) {
                    LOGGER.debug("Skipping respawn: Boss is dead for task {}", entry.getKey());
                    return true;
                }

                if (task.tick()) {
                    Mob minion = (Mob) task.entityType.create(task.level);
                    if (minion != null) {
                        minion.setPos(task.position.getX(), task.position.getY(), task.position.getZ());
                        minion.setCustomName(Component.literal("Minion").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF00))));
                        minion.setCustomNameVisible(true);
                        minion.getPersistentData().putUUID("BossUUID", task.bossUUID);
                        minion.getPersistentData().putInt("RespawnTicks", task.ticksRemaining);
                        task.level.addFreshEntity(minion);
                        LOGGER.info("Minion respawned at {} with type {}", task.position, task.entityType);
                    } else {
                        LOGGER.warn("Failed to create minion of type {}", task.entityType);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        if (!mob.getPersistentData().getBoolean("MinionsSpawned") &&
                mob.getPersistentData().getBoolean("PendingMinionSpawn")) {

            mob.getPersistentData().putBoolean("MinionsSpawned", true);
            mob.getPersistentData().remove("PendingMinionSpawn");

            for (BossConfig config : BOSSES.values()) {
                EntityType<?> configType = EntityType.byString(config.entity).orElse(null);
                if (configType != null && mob.getType().equals(configType)) {
                    applyBossLogic((ServerLevel) mob.level(), mob, config);
                    break;
                }
            }
        }

        if (!mob.getPersistentData().contains("FirstAttacker")) {
            if (event.getSource().getEntity() instanceof Mob || event.getSource().getEntity() instanceof net.minecraft.server.level.ServerPlayer) {
                mob.getPersistentData().putUUID("FirstAttacker", event.getSource().getEntity().getUUID());
            }
        }
    }



    @SubscribeEvent
    public static void onMobSpawn(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // Skip if manually spawned via command or spawn egg
        if (mob.getPersistentData().getBoolean("SpawnedByCommand") ||
                mob.getPersistentData().getBoolean("PendingMinionSpawn")) return;

        for (BossConfig config : BOSSES.values()) {
            EntityType<?> configType = EntityType.byString(config.entity).orElse(null);
            if (configType != null && mob.getType().equals(configType)) {
                LOGGER.info("Marking boss for future minion spawn: {}", mob);
                mob.getPersistentData().putBoolean("PendingMinionSpawn", true);
                break;
            }
        }
    }


    private void loadBossConfigs() {
        File configDir = new File("config/rpgbosses/bosses");
        if (!configDir.exists() && !configDir.mkdirs()) return;

        File[] configFiles = configDir.listFiles((d, name) -> name.endsWith(".json"));
        if (configFiles == null) return;

        for (File file : configFiles) {
            try {
                BossConfig config = BossConfig.fromFile(file);
                BOSSES.put(config.id, config);
            } catch (Exception ignored) {
            }
        }
    }

    private static class RespawnTask {
        private final ServerLevel level;
        private final BlockPos position;
        private final EntityType<?> entityType;
        private final UUID bossUUID;
        private int ticksRemaining;

        public RespawnTask(ServerLevel level, BlockPos position, EntityType<?> entityType, UUID bossUUID, int ticks) {
            this.level = level;
            this.position = position;
            this.entityType = entityType;
            this.bossUUID = bossUUID;
            this.ticksRemaining = ticks;
        }

        public boolean tick() {
            return --ticksRemaining <= 0;
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MinecraftForge.EVENT_BUS.register(ModEnchantments.CHOPPER.get());
        }
    }
}
