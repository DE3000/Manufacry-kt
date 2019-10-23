package manufacry.world.consumers

import io.anuke.arc.collection.Array
import io.anuke.arc.collection.Bits
import io.anuke.arc.scene.ui.layout.Table
import io.anuke.arc.util.Log
import io.anuke.mindustry.entities.type.TileEntity
import io.anuke.mindustry.ui.LiquidDisplay
import io.anuke.mindustry.ui.ReqImage
import io.anuke.mindustry.world.Tile
import io.anuke.mindustry.world.consumers.ConsumeLiquidBase
import io.anuke.mindustry.world.consumers.ConsumeType
import io.anuke.mindustry.world.meta.BlockStat
import io.anuke.mindustry.world.meta.BlockStats
import manufacry.Recipe
import manufacry.entities.traits.RecipeTrait

class ConsumeRecipeLiquid(private val recipes: Array<Recipe>) : ConsumeLiquidBase(0f)
{
	
	init
	{
	
	}
	
	override fun applyLiquidFilter(filter: Bits)
	{
		for (recipe in recipes)
		{
			for (inputLiquid in recipe.inputLiquids)
			{
				filter.set(inputLiquid.liquid.id.toInt());
			}
			
		}
	}
	
	override fun type(): ConsumeType
	{
		return ConsumeType.liquid;
	}
	
	override fun build(tile: Tile, table: Table)
	{
		for (recipe in recipes)
		{
			for (inputLiquid in recipe.inputLiquids)
			{
				table.add(ReqImage(LiquidDisplay(inputLiquid.liquid, inputLiquid.amount, false)) {
					tile.entity != null && tile.entity.liquids != null && (tile.entity.liquids.get(
									inputLiquid.liquid) > inputLiquid.amount)
				}).size(8f * 4).padRight(5f);
			}
		}
	}
	
	override fun getIcon(): String
	{
		return "icon-liquid-consume";
	}
	
	override fun update(entity: TileEntity)
	{
	
	}
	
	override fun trigger(entity: TileEntity)
	{
		if (entity is RecipeTrait)
		{
			val schematic = entity.getRecipe() ?: return;
			for (inputLiquid in schematic.inputLiquids)
			{
				entity.liquids.remove(inputLiquid.liquid,inputLiquid.amount);
			}
		}
	}
	
	override fun valid(entity: TileEntity): Boolean
	{
		var hasLiquids = entity.liquids != null;
		if (entity is RecipeTrait)
		{
			val recipe = entity.getRecipe() ?: return false;
			for (inputLiquid in recipe.inputLiquids)
			{
				Log.info("Consume liquid:" + inputLiquid+" "+entity.liquids.get(inputLiquid.liquid));
				hasLiquids = hasLiquids && entity.liquids.get(inputLiquid.liquid) > inputLiquid.amount;
			}
		}
		
		return hasLiquids;
	}
	
	override fun display(stats: BlockStats)
	{
		for (recipe in recipes)
		{
			for (inputLiquid in recipe.inputLiquids)
			{
				stats.add(BlockStat.input, inputLiquid.liquid, inputLiquid.amount, false);
			}
		}
	}
	
}
