package org.terraform.biome;

import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;

import java.util.Collection;

public class BiomeBlender {
    private final int mountainHeight = 80;
    private final TerraformWorld tw;
    double biomeThreshold = 0.4;
    boolean blendBiomeGrid;
    int riverThreshold = 5;
    boolean blendWater;
    int mountainThreshold = 5;
    boolean blendMountains;
    boolean blendBeachesToo = true;

    public BiomeBlender(TerraformWorld tw, boolean blendBiomeGrid, boolean blendWater, boolean blendMountains) {
        this.tw = tw;

        this.blendBiomeGrid = blendBiomeGrid;
        this.blendWater = blendWater;
        this.blendMountains = blendMountains;
    }

    public BiomeBlender(TerraformWorld tw) {
        this(tw, true, true, true);
    }

    /**
     * A value between 1 and 0 that gets closer to 0
     * when moving closer to the biome edge, mountain or water.
     * Checking each of those elements can be individually
     * disabled or enabled. Threshold values defined in the object
     * can control how quickly blending happens. Blending is linear.
     */
    public double getEdgeFactor(BiomeBank currentBiome, int x, int z) {
        return getEdgeFactor(currentBiome, x, z, blendBeachesToo ? HeightMap.RIVER.getHeight(tw, x, z) : 0);
    }

    /**
     * @param riverDepth Current river depth, has to have also negative values
     * @see BiomeBlender#getEdgeFactor(BiomeBank, int, int)
     */
    public double getEdgeFactor(BiomeBank currentBiome, int x, int z, double riverDepth) {
        double factor = 1;

        if (blendWater) {
            // Linear blending when closer to water

            double riverFactor = blendBeachesToo ? riverDepth / (-riverThreshold) :
                    (HeightMap.getPreciseHeight(tw, x, z) - TerraformGenerator.seaLevel) / riverThreshold;

            if (riverFactor < factor) factor = Math.max(0, riverFactor);
        }

        if (blendMountains) {
            // Linear blending when closer to mountains
            double mountainFactor = ((mountainHeight - 5) - HeightMap.getPreciseHeight(tw, x, z)) / (double) mountainThreshold;
            if (mountainFactor < factor) factor = Math.max(0, mountainFactor);
        }

        if (blendBiomeGrid) {
            // Same here when closer to biome edge
            double gridFactor = getGridEdgeFactor(currentBiome, tw,x,z);//getGridEdgeFactor(BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z),
            		//currentBiome,
                    //BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z).getTemperature(), BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z).getMoisture());
            if (gridFactor < factor) factor = gridFactor;
        }

        return factor;
    }

    /*
        Get edge factor only based on land, ignore rivers
        Updated to reflect new changes to heightmap and biomes
     */
    public double getGridEdgeFactor(BiomeBank currentBiome, TerraformWorld tw, int x, int z) {
    	SimpleLocation target  = new SimpleLocation(x,0,z);
    	Collection<BiomeSection> sections = BiomeSection.getSurroundingSections(tw, x, z);

    	BiomeSection mostDominantTarget = null;
    	double dominantDominance = -100;
    	for (BiomeSection section : sections) {
    	    if (section.getBiomeBank() == currentBiome) {
    	        double dom = section.getDominance(target);
    	        if (dom > dominantDominance) {
    	            mostDominantTarget = section;
    	            dominantDominance = dom;
                }

            }
        }
    	if (mostDominantTarget == null) return 0;

        double factor = 1;
    	for (BiomeSection section : sections) {
    	    if (section.getBiomeBank() == currentBiome) continue;

    	    float dom = section.getDominance(target);
    	    double diff = Math.max(0, dominantDominance - dom);

    	    factor = Math.min(factor, diff);
        }

    	return Math.min(factor, 1);
    }

    /**
     * @param biomeThreshold Value between > 0 and 1, defines how quickly
     *                       output value approaches 0 when near biome edge.
     *                       Default of 0.25, which means blending will start
     *                       1/4 "biome grid units" from biome edge.
     */
    public BiomeBlender setBiomeThreshold(double biomeThreshold) {
        this.biomeThreshold = biomeThreshold;
        return this;
    }

    /**
     * @param riverThreshold Default value of 5, which means
     *                       linear blending happens when river
     *                       depth is more than -5 (0 > dep > -5).
     */
    public BiomeBlender setRiverThreshold(int riverThreshold) {
        this.riverThreshold = riverThreshold;
        return this;
    }

    /**
     * @param mountainThreshold Default value of 5, which means
     *                          linear blending happens when closer
     *                          than 5 blocks from mountain height threshold.
     */
    public BiomeBlender setMountainThreshold(int mountainThreshold) {
        this.mountainThreshold = mountainThreshold;
        return this;
    }

    /**
     * @param blendBeachesToo If false, blending will happen *riverThreshold*
     *                        away from sea level instead of beach level. In other words,
     *                        controls if blending happens based on sea level or river depth.
     */
    public BiomeBlender setBlendBeaches(boolean blendBeachesToo) {
        this.blendBeachesToo = blendBeachesToo;
        return this;
    }
}
