package org.terraform.biome.ocean;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class OceansHandler extends AbstractOceanHandler {

    public OceansHandler(BiomeType oceanType) {
		super(oceanType);
		// TODO Auto-generated constructor stub
	}

	@Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
    	if(this.oceanType == BiomeType.DEEP_OCEANIC)
    		return Biome.DEEP_OCEAN;
        return Biome.OCEAN;
    }

//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 2);
//		gen.setScale(0.005);
//		
//		return (int) (gen.noise(x, z, 0.5, 0.5)*7D+50D);
//	}

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.GRAVEL,
        		Material.GRAVEL,
                GenUtils.randMaterial(rand, Material.STONE, Material.GRAVEL, Material.STONE),
                GenUtils.randMaterial(rand, Material.STONE),
                GenUtils.randMaterial(rand, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {
        boolean growsKelp = world.getHashedRand(rawX>>4,rawZ>>4,371412).nextBoolean();
        //Set ground near sea level to sand
        if(surfaceY >= TerraformGenerator.seaLevel - 2) {
            data.setType(rawX, surfaceY, rawZ, Material.SAND);
        }else if(surfaceY >= TerraformGenerator.seaLevel - 4) {
            if(random.nextBoolean())
                data.setType(rawX, surfaceY, rawZ, Material.SAND);
        }

        if (!BlockUtils.isStoneLike(data.getType(rawX, surfaceY, rawZ))) return;
        if (GenUtils.chance(random, 10, 100)) { //SEA GRASS/KELP
            CoralGenerator.generateKelpGrowth(data, rawX, surfaceY + 1, rawZ);
        } else if (GenUtils.chance(random, 3, 50)
                && growsKelp
                && surfaceY + 1 < TerraformGenerator.seaLevel - 10) {
            generateKelp(rawX, surfaceY + 1, rawZ, data, random);
        }
    }

    private void generateKelp(int x, int y, int z, PopulatorDataAbstract data, Random random) {
        for (int ny = y; ny < TerraformGenerator.seaLevel - GenUtils.randInt(5, 15); ny++) {
            data.setType(x, ny, z, Material.KELP_PLANT);
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		
		//Spawn rocks
		SimpleLocation[] rocks = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 25, 0.4f);
        
        for (SimpleLocation sLoc : rocks) {
            if (data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome()) {
                int rockY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
                sLoc.setY(rockY);
                if(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()) != Material.GRAVEL)
                		continue;
                
                BlockUtils.replaceSphere(
                		random.nextInt(9987),
                		(float) GenUtils.randDouble(random, 3, 7), 
                		(float) GenUtils.randDouble(random, 2, 4), 
                		(float) GenUtils.randDouble(random, 3, 7), 
                		new SimpleBlock(data,sLoc), 
                		true, 
                		GenUtils.randMaterial(
                				Material.STONE,
                				Material.GRANITE,
                				Material.ANDESITE,
                				Material.DIORITE
                		));
            }
        }
	}


}
