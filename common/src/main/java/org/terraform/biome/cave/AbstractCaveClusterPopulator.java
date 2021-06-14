package org.terraform.biome.cave;

import java.util.ArrayList;
import java.util.Random;

import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.OneOneSevenBlockHandler;
import org.terraform.utils.version.Version;

public abstract class AbstractCaveClusterPopulator extends AbstractCavePopulator {

	protected abstract void oneUnit(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor);
	@Override
    public void populate(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor) {

		ArrayList<SimpleBlock[]> ceilFloorPairs = new ArrayList<>();
        
        FastNoise circleNoise = NoiseCacheHandler.getNoise(
                tw,
                NoiseCacheEntry.BIOME_CAVECLUSTER_CIRCLENOISE,
                world -> {
                    FastNoise n = new FastNoise((int) (world.getSeed() * 11));
                    n.SetNoiseType(FastNoise.NoiseType.Simplex);
                    n.SetFrequency(0.09f);

                    return n;
                });
        
        float radius = GenUtils.randInt(random, 5, 11);
        SimpleBlock center = new SimpleBlock(ceil.getPopData(), ceil.getX(), (ceil.getY() + floor.getY())/2, ceil.getZ());
        		
        for (float x = -radius; x <= radius; x++) {
            for (float z = -radius; z <= radius; z++) {
                SimpleBlock rel = center.getRelative(Math.round(x), 0, Math.round(z));
                
                //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                double equationResult = Math.pow(x, 2) / Math.pow(radius, 2)
                        + Math.pow(z, 2) / Math.pow(radius, 2);
                if (equationResult <= 1 + 0.7 * circleNoise.GetNoise(rel.getX(), rel.getZ())) {
                	Wall candidateFloorWall = new Wall(rel).findFloor(20);
                	Wall candidateCeilWall = new Wall(rel).findCeiling(20);
                	if(candidateFloorWall != null && candidateCeilWall != null) {
                		SimpleBlock candidateCeil = candidateCeilWall.get();
                		SimpleBlock candidateFloor = candidateFloorWall.get();
                		
                		//Don't allow amethysts to be part of this
                		if(Version.isAtLeast(17) 
                        		&& ((candidateFloor.getType() == OneOneSevenBlockHandler.AMETHYST_BLOCK
                        		|| candidateFloor.getType() == OneOneSevenBlockHandler.AMETHYST_CLUSTER)
                        				|| (candidateCeil.getType() == OneOneSevenBlockHandler.AMETHYST_BLOCK
                                        		|| candidateCeil.getType() == OneOneSevenBlockHandler.AMETHYST_CLUSTER))) {
                        	continue;
                        }
                		
	                	if(!candidateFloor.getRelative(0,1,0).getType().isSolid()) {
	                		if(!candidateCeil.getRelative(0,-1,0).getType().isSolid()) {
	                			if(candidateCeil.getY() - 1 > candidateFloor.getY() + 1) {
	                        		ceilFloorPairs.add(new SimpleBlock[] {
	                        				candidateCeil,
	                        				candidateFloor
	                        		});
	                        	}
	                    	}
	                	}
                		
                	}
                }
            }
        }
        
        for(SimpleBlock[] candidates:ceilFloorPairs) {
        	oneUnit(tw, random, candidates[0], candidates[1]);
        }
    }
}
