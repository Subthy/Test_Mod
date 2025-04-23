package net.subthy;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class BossConfig {
    public String id;
    public String entity;
    public int maxMinions;
    public List<String> minionTypes = new ArrayList<>();
    public int respawnTicks = 600; // default value

    public static BossConfig fromFile(File file) throws IOException {
        String raw = Files.readString(file.toPath());
        JsonObject json = new Gson().fromJson(raw, JsonObject.class);

        BossConfig config = new BossConfig();
        config.id = file.getName().replace(".json", "");
        config.entity = json.get("entity").getAsString();
        config.maxMinions = json.get("maxMinions").getAsInt();
        json.get("minionTypes").getAsJsonArray().forEach(e -> config.minionTypes.add(e.getAsString()));

        if (json.has("respawnTicks")) {
            config.respawnTicks = json.get("respawnTicks").getAsInt();
        }

        return config;
    }
}

