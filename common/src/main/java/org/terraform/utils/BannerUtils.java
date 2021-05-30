package org.terraform.utils;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import java.util.ArrayList;
import java.util.Random;

public class BannerUtils {
    private static final Material[] BANNERS = {
            Material.RED_BANNER,
            Material.ORANGE_BANNER,
            Material.YELLOW_BANNER,
            Material.LIME_BANNER,
            Material.GREEN_BANNER,
            Material.CYAN_BANNER,
            Material.BLUE_BANNER,
            Material.PURPLE_BANNER,
            Material.MAGENTA_BANNER,
            Material.BLACK_BANNER,
            Material.BROWN_BANNER,
            Material.PINK_BANNER,
            Material.WHITE_BANNER,
    };
    private static final Material[] WALL_BANNERS = {
            Material.RED_WALL_BANNER,
            Material.ORANGE_WALL_BANNER,
            Material.YELLOW_WALL_BANNER,
            Material.LIME_WALL_BANNER,
            Material.GREEN_WALL_BANNER,
            Material.CYAN_WALL_BANNER,
            Material.BLUE_WALL_BANNER,
            Material.PURPLE_WALL_BANNER,
            Material.MAGENTA_WALL_BANNER,
            Material.BLACK_WALL_BANNER,
            Material.BROWN_WALL_BANNER,
            Material.PINK_WALL_BANNER,
            Material.WHITE_WALL_BANNER,
    };

    public static Material randomBannerMaterial(Random rand) {
        return BANNERS[rand.nextInt(BANNERS.length)];
    }

    public static Material randomWallBannerMaterial(Random rand) {
        return WALL_BANNERS[rand.nextInt(WALL_BANNERS.length)];
    }

	/**
	 * kms
	 * https://minecraft.fandom.com/wiki/Banner/Patterns
		Pattern:"mr",Color:CYAN
		Pattern:"bs",Color:LIGHT_GRAY
		Pattern:"cs",Color:GRAY
		Pattern:"bo",Color:LIGHT_GRAY
		Pattern:"ms",Color:BLACK
		Pattern:"hh",Color:LIGHT_GRAY
		Pattern:"mc",Color:LIGHT_GRAY
		Pattern:"bo",Color:BLACK
	 */
    public static ArrayList<Pattern> getOminousBannerPatterns(){
    	return new ArrayList<Pattern>() {{
			add(new Pattern(DyeColor.CYAN, PatternType.RHOMBUS_MIDDLE));
			add(new Pattern(DyeColor.LIGHT_GRAY, PatternType.STRIPE_BOTTOM));
			add(new Pattern(DyeColor.GRAY, PatternType.STRIPE_CENTER));
			add(new Pattern(DyeColor.LIGHT_GRAY, PatternType.BORDER));
			add(new Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE));
			add(new Pattern(DyeColor.LIGHT_GRAY, PatternType.HALF_HORIZONTAL));
			add(new Pattern(DyeColor.LIGHT_GRAY, PatternType.CIRCLE_MIDDLE));
			add(new Pattern(DyeColor.BLACK, PatternType.BORDER));
		}};
    }
}
