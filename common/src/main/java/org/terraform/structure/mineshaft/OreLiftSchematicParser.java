package org.terraform.structure.mineshaft;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class OreLiftSchematicParser extends SchematicParser {
	
	boolean isBadlands = false;
	public OreLiftSchematicParser() {}
	
	public OreLiftSchematicParser(boolean isBadlands) {
		this.isBadlands = isBadlands;
	}
	
    @Override
    public void applyData(SimpleBlock block, BlockData data) {
        if (BlockUtils.ores.contains(data.getMaterial())) {
            data = Bukkit.createBlockData(GenUtils.randMaterial(BlockUtils.ores));
        }
        //super.applyData(block, data);
        //Override the normal way as it updates physics.
        //Don't update physics because rails are cancer.
        
        if(this.isBadlands) {
        	 if (data.getMaterial().toString().contains("OAK")) {
                 data = Bukkit.createBlockData(
                         data.getAsString().replaceAll("oak_","dark_oak_")
                 );
                 super.applyData(block, data);
             }
        }
        
        super.applyData(block, data);
    }
}