package org.terraform.structure.mineshaft;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.flat.BadlandsHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.structure.room.PathGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Vector2f;

import java.util.ArrayList;
import java.util.Random;

public class BadlandsMinePopulator extends MultiMegaChunkStructurePopulator {
    static int sandRadius = TConfigOption.BIOME_BADLANDS_PLATEAU_SAND_RADIUS.getInt();
    static int mineDistance = TConfigOption.BIOME_BADLANDS_MINE_DISTANCE.getInt();
    static int hallwayLen = 12;

    // These gets overwritten every spawn
    BlockFace outDir, inDir;
    SimpleBlock entrance, gateStart, shaft;

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, ArrayList<BiomeBank> biomes) {
        if (!biomes.contains(BiomeBank.BADLANDS)) return false;

        for (Vector2f pos : GenUtils.randomObjectPositions(tw, chunkX >> 4, chunkZ >> 4, mineDistance, mineDistance * 0.3f)) {
            if ((int) pos.x == chunkX && (int) pos.y == chunkZ) {
                SimpleBlock s = getSpawnPosition(tw, chunkX, chunkZ);
                return s != null && getSpawnDirection(tw, s.getX(), s.getZ()) != null;
            }
        }

        return false;
    }

    @Override
    public int[] getNearestFeature(TerraformWorld world, int rawX, int rawZ) {
        return new int[0];
    }

    @Override
    public Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ) {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public int[][] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc) {
        return new int[0][];
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        // Get all positions and faces
        entrance = getSpawnPosition(tw, data.getChunkX(), data.getChunkZ());
        if(entrance == null) return;
        inDir = getSpawnDirection(tw, entrance.getX(), entrance.getZ());
        if(inDir == null) return;
        outDir = inDir.getOppositeFace();
        entrance = entrance.getRelative(outDir, sandRadius);
        gateStart = entrance.getRelative(inDir, 2 + 5);
        shaft = entrance.getRelative(inDir, hallwayLen + 5);

        Random random = tw.getHashedRand(entrance.getX(), entrance.getY(), entrance.getZ(), 4);
        System.out.println("CARVIIIIIINNNNGGGGGGG " + entrance.getX() + " " + entrance.getY() + " " + entrance.getZ());

        // Spawning stuff
        spawnShaft(random, shaft);

        PathGenerator g = new PathGenerator(entrance.getRelative(inDir.getModX() * 2, -1, inDir.getModZ() * 2),
                new Material[] {Material.CAVE_AIR}, new Random(), new int[]{0,0}, new int[]{0,0});
        g.setPopulator(new BadlandsMineshaftPathPopulator(random));
        g.generateStraightPath(null, inDir, hallwayLen);

        spawnEntrance(gateStart, outDir);
        patchEntrance(entrance, inDir);


    }

    SimpleBlock getSpawnPosition(TerraformWorld tw, int chunkX, int chunkZ) {
        for (int x = chunkX << 4; x < chunkX << 4 + 15; x += 3) {
            for (int z = chunkZ << 4; z < chunkZ << 4 + 15; z += 3) {
                if (BadlandsHandler.mineCanSpawn(tw, x, z))
                    return new SimpleBlock(new Location(tw.getWorld(),x, HeightMap.getBlockHeight(tw, x, z), z));
            }
        }

        return null;
    }

    public static BlockFace getSpawnDirection(TerraformWorld tw, int x, int z) {
        SimpleBlock base = new SimpleBlock(new Location(tw.getWorld(), x, 0, z));

        for (BlockFace face : BlockUtils.directBlockFaces) {
            SimpleBlock right = base.getRelative(BlockUtils.getRight(face), 3);
            SimpleBlock left = base.getRelative(BlockUtils.getLeft(face), 3);

            SimpleBlock b = right.getRelative(face, 20);
            if (!BadlandsHandler.containsPlateau(tw, b.getX(), b.getZ()))
                continue;

            b = right.getRelative(face.getOppositeFace(), sandRadius + 2);
            if (BadlandsHandler.containsPlateau(tw, b.getX(), b.getZ()))
                continue;

            b = left.getRelative(face, 20);
            if (!BadlandsHandler.containsPlateau(tw, b.getX(), b.getZ()))
                continue;

            b = left.getRelative(face.getOppositeFace(), sandRadius + 2);
            if (BadlandsHandler.containsPlateau(tw, b.getX(), b.getZ()))
                continue;

            if (!BadlandsHandler.containsPlateau(tw, base.getX() + face.getModX() * 10,
                    base.getZ() + face.getModZ() * 10))
                continue;

            return face;
        }

        return null;
    }

    void spawnEntrance(SimpleBlock entrance, BlockFace direction) {
        entrance = entrance.getRelative(direction.getModX(), -1, direction.getModZ());

        // Place support frame
        try {
            SimpleBlock framePos = entrance.getRelative(BlockUtils.getRight(direction), 1).getRelative(direction);
            TerraSchematic entranceSchematic = TerraSchematic.load("badlands-mineshaft/badlands-mine-entrance", framePos);
            entranceSchematic.parser = new BadlandsMineEntranceParser();
            entranceSchematic.setFace(direction);
            entranceSchematic.apply();
        } catch(Exception ignored) {
            TerraformGeneratorPlugin.logger.error("An error occurred reading Badlands Mine Entrance schematic file.");
        }
    }

    void patchEntrance(SimpleBlock entrance, BlockFace direction) {
        BlockFace nextDir = BlockUtils.getRight(direction);
        fillWithBlock(entrance.getRelative(nextDir.getModX() * 2, -1, nextDir.getModZ() * 2),
                entrance.getRelative(-nextDir.getModX() * 2, -4, -nextDir.getModZ() * 2).getRelative(direction, 2), Material.RED_SAND);
    }

    void fillWithBlock(SimpleBlock start, SimpleBlock end, Material material) {
        for(int x = Math.min(start.getX(), end.getX()); x <= Math.min(start.getX(), end.getX()) + Math.abs(start.getX() - end.getX()); x++) {
            for(int z = Math.min(start.getZ(), end.getZ()); z <= Math.min(start.getZ(), end.getZ()) + Math.abs(start.getZ() - end.getZ()); z++) {
                for(int y = Math.min(start.getY(), end.getY()); y <= Math.min(start.getY(), end.getY()) + Math.abs(start.getY() - end.getY()); y++) {
                    new SimpleBlock(start.getPopData(), x, y, z).lsetType(material);
                }
            }
        }
    }

    private void spawnShaft(Random random, SimpleBlock shaft) {
        int depth = 25;
        int shaftStart = - 6;
        int supportR = 3;

        // Carving at ground level
        BlockUtils.carveCaveAir(random.nextInt(777123),
                5 / 2f,
                4,
                5 / 2f,
                shaft,
                true,
                BlockUtils.badlandsStoneLike);

        ArrayList<SimpleBlock> platforms = new ArrayList<>();
        for (double i = 0; i < depth; i ++) { // Carve shaft
            double width = 6 + Math.pow((i % 6) * 0.2, 2);

            SimpleBlock centerBlock = shaft.getRelative(GenUtils.randInt(random, -1, 1),
                    (int) Math.round(- i + shaftStart), GenUtils.randInt(random, -1, 1));

            BlockUtils.carveCaveAir(random.nextInt(777123),
                    (float) width / 2f,
                    2,
                    (float) width / 2f,
                    centerBlock,
                    true,
                    BlockUtils.badlandsStoneLike);

            if (i % 6 > 4 && i > 0) { // Add mineshaft platform positions
                for (int b = 0; b < 1; b++) {
                    double angle = GenUtils.randDouble(random, 0, 2 * Math.PI);
                    int xAdd = (int) Math.round(Math.sin(angle) * 3);
                    int zAdd = (int) Math.round(Math.cos(angle) * 3);

                    SimpleBlock platform = centerBlock.getRelative(xAdd, 0, zAdd);
                    platform = GenUtils.getTrueHighestBlockBelow(platform);

                    platforms.add(platform);
                }
            }
        }

        // Spawn platforms with loot
        for (SimpleBlock platform : platforms) {
            if (GenUtils.chance(random, 3, 4))
                spawnShaftPlatform(platform);
        }

        // Vertical support structure
        BlockFace right = BlockUtils.getRight(inDir);
        BlockFace left = BlockUtils.getLeft(inDir);
        ArrayList<SimpleBlock> mainPillars = new ArrayList<>(); // Order does matter here
        mainPillars.add( // Front right
                shaft.getRelative(inDir.getModX() * supportR, shaftStart, inDir.getModZ() * supportR).getRelative(right, supportR));
        mainPillars.add( // Front left
                shaft.getRelative(inDir.getModX() * supportR, shaftStart, inDir.getModZ() * supportR).getRelative(left, supportR));
        mainPillars.add( // Rear left
                shaft.getRelative(outDir.getModX() * supportR, shaftStart, outDir.getModZ() * supportR).getRelative(left, supportR));
        mainPillars.add( // Rear right
                shaft.getRelative(outDir.getModX() * supportR, shaftStart, outDir.getModZ() * supportR).getRelative(right, supportR));

        // Two random pillars
        ArrayList<SimpleBlock> supportPillars = new ArrayList<>(mainPillars);
        int xAdd = GenUtils.randInt(random, -supportR, supportR);
        int zAdd = (supportR - Math.abs(xAdd)) * (random.nextBoolean() ? 1 : -1);
        supportPillars.add(
                shaft.getRelative(outDir.getModX() * xAdd, shaftStart, outDir.getModZ() * xAdd).getRelative(left, zAdd));

        xAdd = GenUtils.randInt(random, -supportR, supportR);
        zAdd = (supportR - Math.abs(xAdd)) * (random.nextBoolean() ? 1 : -1);
        supportPillars.add(
                shaft.getRelative(outDir.getModX() * xAdd, shaftStart, outDir.getModZ() * xAdd).getRelative(left, zAdd));

        supportPillars.removeIf(n -> GenUtils.chance(random, 1, 5));

        // Place vertical
        for (SimpleBlock pillar : supportPillars) {
            for (int y = 0; y < depth + 5; y++) {
                pillar.getRelative(0, - y, 0).lsetType(Material.DARK_OAK_FENCE);
            }
        }

        // Horizontal support structure
        for (SimpleBlock platform : platforms) {
            int y = platform.getY();

            BlockFace face = BlockUtils.getLeft(inDir);
            for (int i = 0; i < mainPillars.size(); i++) {
                if (supportPillars.contains(mainPillars.get(i)) &&
                        supportPillars.contains(mainPillars.get(i + 1 >= mainPillars.size() ? 0 : i + 1))
                        && GenUtils.chance(random, 2, 3)) {
                    SimpleBlock mainPillar = mainPillars.get(i);
                    for (int add = 1; add < 2 * supportR; add++) {
                        SimpleBlock b = new SimpleBlock(platform.getPopData(), mainPillar.getX(), y,
                                mainPillar.getZ()).getRelative(face, add);
                        if (b.getType() == Material.STONE || b.getType().isAir()) {
                            b.setType(Material.DARK_OAK_FENCE);
                            BlockUtils.correctSurroundingMultifacingData(b);

                            if (GenUtils.chance(random, 1, 12) &&
                                    !b.getRelative(0, -1, 0).getType().isSolid()) {
                                Lantern l = (Lantern) Bukkit.createBlockData(Material.LANTERN);
                                l.setHanging(true);
                                b.getRelative(0, -1, 0).setBlockData(l);
                            }
                        }
                    }
                }

                face = BlockUtils.getLeft(face);
            }
        }
    }

    private void spawnShaftPlatform(SimpleBlock center) {
        BlockUtils.carveCaveAir(new Random().nextInt(777123),
                2.5f,
                1.5f,
                2.5f,
                center.getRelative(0, 2, 0),
                true,
                BlockUtils.badlandsStoneLike);

        center.setType(Material.DARK_OAK_PLANKS);
        ArrayList<SimpleBlock> lootBlocks = new ArrayList<>();
        lootBlocks.add(center.getRelative(0, 1, 0));

        for (BlockFace face : BlockUtils.directBlockFaces) {
            center.getRelative(face).setType(Material.DARK_OAK_PLANKS);
            lootBlocks.add(center.getRelative(face).getRelative(0, 1, 0));

            Stairs stairs = (Stairs) Bukkit.createBlockData(Material.DARK_OAK_STAIRS);
            stairs.setHalf(Bisected.Half.TOP);
            stairs.setFacing(face.getOppositeFace());
            center.getRelative(face, 2).setBlockData(stairs);

            SimpleBlock lantern = center.getRelative(face, 2).getRelative(0, 1, 0);
            if (lantern.getType().isAir() && GenUtils.chance(1, 4))
                lantern.setType(Material.LANTERN);

            Slab slab = (Slab) Bukkit.createBlockData(Material.DARK_OAK_SLAB);
            slab.setType(Slab.Type.TOP);
            center.getRelative(face).getRelative(BlockUtils.getRight(face)).setBlockData(slab);
            lootBlocks.add(center.getRelative(face).getRelative(BlockUtils.getRight(face)).getRelative(0, 1, 0));
        }

        for (SimpleBlock lootBlock : lootBlocks) {
            if (GenUtils.chance(9, 10)) {
                setLootBlock(lootBlock);

                if (GenUtils.chance(4, 10)) {
                    setLootBlock(lootBlock.getRelative(0, 1, 0));
                }
            }
        }
    }

    private void setLootBlock(SimpleBlock lootBlock) {
        if (GenUtils.chance(1, 25) && !lootBlock.getType().isSolid()) {
            lootBlock.setType(Material.BARREL);
            lootBlock.setBlockData(BlockUtils.getRandomBarrel());
            lootBlock.getPopData().lootTableChest(lootBlock.getX(), lootBlock.getY(), lootBlock.getZ(), TerraLootTable.ABANDONED_MINESHAFT);
        } else {
            lootBlock.lsetType(GenUtils.randMaterial(BlockUtils.ores));
        }
    }
}
