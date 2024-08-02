package net.subthy.testmod.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VoxelShapeHelper {

    public static VoxelShape combineAll(Collection<VoxelShape> shapes) {
        VoxelShape result = Shapes.empty();
        for (VoxelShape shape : shapes) {
            result = Shapes.join(result, shape, BooleanOp.OR);
        }
        return result.optimize();
    }

    public static VoxelShape combineAll(VoxelShape... shapes) {
        VoxelShape result = Shapes.empty();
        for (VoxelShape shape : shapes) {
            result = Shapes.join(result, shape, BooleanOp.OR);
        }
        return result.optimize();
    }

    public static Map<Direction, VoxelShape> getRotatedShapes(VoxelShape shape) {
        return Arrays.stream(Direction.values()).collect(Collectors.toMap(Function.identity(), direction -> VoxelShapeHelper.rotateShape(shape, direction)));
    }

    public static Map<Direction, VoxelShape> getHorizontalRotatedShapes(VoxelShape shape) {
        return Util.make(new EnumMap<>(Direction.class), map -> {
            map.put(Direction.NORTH, shape);
            map.put(Direction.EAST, rotateShapeHorizontal(shape, Direction.EAST));
            map.put(Direction.SOUTH, rotateShapeHorizontal(shape, Direction.SOUTH));
            map.put(Direction.WEST, rotateShapeHorizontal(shape, Direction.WEST));
        });
    }

    public static VoxelShape rotateShape(VoxelShape shape, Direction direction) {
        List<VoxelShape> rotatedPieces = new ArrayList<>();
        Vec3 vec3 = new Vec3(-0.5, -0.5, -0.5);

        shape.toAabbs().forEach(aabb -> {
            aabb = aabb.move(vec3.x, vec3.y, vec3.z);

            AABB rotated = switch (direction) {
                case DOWN -> aabb;
                case UP -> new AABB(aabb.minX, -aabb.minY, -aabb.minZ, aabb.maxX, -aabb.maxY, -aabb.maxZ);
                case NORTH -> new AABB(aabb.minX, -aabb.minZ, aabb.minY, aabb.maxX, -aabb.maxZ, aabb.maxY);
                case SOUTH -> new AABB(-aabb.minX, -aabb.minZ, -aabb.minY, -aabb.maxX, -aabb.maxZ, -aabb.maxY);
                case WEST -> new AABB(aabb.minY, -aabb.minZ, -aabb.minX, aabb.maxY, -aabb.maxZ, -aabb.maxX);
                case EAST -> new AABB(-aabb.minY, -aabb.minZ, aabb.minX, -aabb.maxY, -aabb.maxZ, aabb.maxX);
            };

            rotatedPieces.add(Shapes.create(rotated.move(-vec3.x, -vec3.z, -vec3.z)));
        });

        return rotatedPieces.stream().reduce(Shapes.empty(), Shapes::or);
    }

    public static VoxelShape rotateShapeHorizontal(VoxelShape shape, Direction direction) {
        List<VoxelShape> rotatedPieces = new ArrayList<>();
        Vec3 vec3 = new Vec3(-0.5, -0.5, -0.5);

        shape.toAabbs().forEach(aabb -> {
            aabb = aabb.move(vec3.x, vec3.y, vec3.z);

            AABB rotated = switch (direction) {
                case NORTH -> aabb;
                case SOUTH -> new AABB(-aabb.minX, aabb.minY, -aabb.minZ, -aabb.maxX, aabb.maxY, -aabb.maxZ);
                case WEST -> new AABB(aabb.minZ, aabb.minY, -aabb.minX, aabb.maxZ, aabb.maxY, -aabb.maxX);
                case EAST -> new AABB(-aabb.minZ, aabb.minY, aabb.minX, -aabb.maxZ, aabb.maxY, aabb.maxX);
                default -> throw new IllegalStateException("Unexpected value: " + direction);
            };

            rotatedPieces.add(Shapes.create(rotated.move(-vec3.x, -vec3.z, -vec3.z)));
        });

        return rotatedPieces.stream().reduce(Shapes.empty(), Shapes::or);
    }

    private static double[] adjustValues(Direction direction, double minX, double minZ, double maxX, double maxZ) {
        switch (direction) {
            case WEST -> {
                double temp_minX = minX;
                minX = 1.0F - maxX;
                double temp_minZ = minZ;
                minZ = 1.0F - maxZ;
                maxX = 1.0F - temp_minX;
                maxZ = 1.0F - temp_minZ;
            }
            case NORTH -> {
                double temp_minX = minX;
                minX = minZ;
                minZ = 1.0F - maxX;
                maxX = maxZ;
                maxZ = 1.0F - temp_minX;
            }
            case SOUTH -> {
                double temp_minX = minX;
                minX = 1.0F - maxZ;
                double temp_minZ = minZ;
                minZ = temp_minX;
                double temp_maxX = maxX;
                maxX = 1.0F - temp_minZ;
                maxZ = temp_maxX;
            }
        }
        return new double[]{minX, minZ, maxX, maxZ};
    }

    public static EnumMap<Direction.Axis, VoxelShape> rotateAxis(VoxelShape shape) {
        return new EnumMap<>(ImmutableMap.of(
                Direction.Axis.Y, shape,
                Direction.Axis.X, rotateAxis(shape, Direction.Axis.X),
                Direction.Axis.Z, rotateAxis(shape, Direction.Axis.Z)
        ));
    }

    public static VoxelShape rotateAxis(VoxelShape shape, Direction.Axis axis) {
        if (axis == Direction.Axis.Y) {
            return shape;
        }
        Set<VoxelShape> rotatedShapes = new HashSet<>();

        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            y1 = (y1 * 16); y2 = (y2 * 16);
            x1 = (x1 * 16); x2 = (x2 * 16);
            z1 = (z1 * 16); z2 = (z2 * 16);

            if (axis == Direction.Axis.X) {
                rotatedShapes.add(Block.box(y1, x1, z1, y2, x2, z2));
            } else {
                rotatedShapes.add(Block.box(x1, z1, y1, x2, z2, y2));
            }
        });

        return rotatedShapes.stream().reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    }

    public static VoxelShape add(double x1, double y1, double z1, double x2, double y2, double z2, VoxelShape... shapes) {
        Set<VoxelShape> result = new HashSet<>();
        for (VoxelShape shape : shapes) {
            shape.forAllBoxes((x, y, z, x3, y3, z3) -> {
                x = (x * 16); x3 = (x3 * 16);
                y = (y * 16); y3 = (y3 * 16);
                z = (z * 16); z3 = (z3 * 16);

                result.add(Block.box(x + x1, y + y1, z + z1, x3 + x2, y3 + y2, z3 + z2));
            });
        }
        return result.stream().reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).orElse(Shapes.empty());
    }
}
