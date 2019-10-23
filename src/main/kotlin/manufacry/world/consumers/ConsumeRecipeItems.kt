package manufacry.world.consumers

import io.anuke.arc.collection.Array
import io.anuke.mindustry.world.consumers.Consume
import io.anuke.arc.collection.Bits
import io.anuke.mindustry.world.consumers.ConsumeType
import io.anuke.mindustry.ui.ReqImage
import io.anuke.mindustry.ui.ItemImage
import io.anuke.mindustry.game.Cicon
import io.anuke.arc.scene.ui.layout.Table
import io.anuke.mindustry.world.Tile
import io.anuke.mindustry.entities.type.TileEntity
import io.anuke.mindustry.world.meta.BlockStats
import io.anuke.mindustry.world.meta.BlockStat
import manufacry.Recipe
import manufacry.entities.traits.RecipeTrait

class ConsumeRecipeItems(private val recipes: Array<Recipe>) : Consume()
{
	
	init
	{
	
	}
	
	override fun applyItemFilter(filter: Bits)
	{
		for (recipe in recipes)
		{
			
				for (inputItem in recipe.inputItems)
				{
					filter.set(inputItem.item.id.toInt());
				}
			
		}
	}
	
	override fun type(): ConsumeType
	{
		return ConsumeType.item;
	}
	
	override fun build(tile: Tile, table: Table)
	{
		for (recipe in recipes)
		{
			for (inputItem in recipe.inputItems)
			{
				table.add(ReqImage(ItemImage(inputItem.item.icon(Cicon.medium), inputItem.amount)) {
					tile.entity != null && tile.entity.items != null && tile.entity.items.has(inputItem.item, inputItem.amount)
				}).size(8f * 4).padRight(5f);
			}
		}
	}
	
	override fun getIcon(): String
	{
		return "icon-item";
	}
	
	override fun update(entity: TileEntity)
	{
	
	}
	
	override fun trigger(entity: TileEntity)
	{
		if (entity is RecipeTrait)
		{
			val schematic = entity.getRecipe() ?: return;
			for (inputItem in schematic.inputItems)
			{
				entity.items.remove(inputItem);
			}
		}
	}
	
	override fun valid(entity: TileEntity): Boolean
	{
		var hasItems = entity.items != null;
		if (entity is RecipeTrait)
		{
			val recipe = entity.getRecipe() ?: return false;
			for (inputItem in recipe.inputItems)
			{
				hasItems = hasItems && entity.items.has(inputItem.item);
			}
		}
		return hasItems;
	}
	
	override fun display(stats: BlockStats)
	{
		for (recipe in recipes)
		{
			for (inputitem in recipe.inputItems)
			{
				stats.add(BlockStat.input, inputitem);
			}
		}
	}
}
