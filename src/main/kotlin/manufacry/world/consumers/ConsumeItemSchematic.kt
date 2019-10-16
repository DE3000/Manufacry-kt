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
import manufacry.Schematic
import manufacry.entities.traits.SchematicTrait

class ConsumeItemSchematic(private val schematics: Array<Schematic>) : Consume()
{
	
	init
	{
	
	}
	
	override fun applyItemFilter(filter: Bits)
	{
		for (schematic in schematics)
		{
			val stack = schematic.input;
			filter.set(stack.item.id.toInt());
		}
	}
	
	override fun type(): ConsumeType
	{
		return ConsumeType.item;
	}
	
	override fun build(tile: Tile, table: Table)
	{
		for (schematic in schematics)
		{
			val stack = schematic.input;
			table.add(ReqImage(ItemImage(stack.item.icon(Cicon.medium), stack.amount)) {
				tile.entity != null && tile.entity.items != null && tile.entity.items.has(stack.item, stack.amount)
			}).size(8f * 4).padRight(5f);
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
		if (entity is SchematicTrait)
		{
			val schematic = entity.getSchematic() ?: return;
			val stack = schematic.input;
			entity.items.remove(stack);
		}
	}
	
	override fun valid(entity: TileEntity): Boolean
	{
		val hasItems = false;
		if (entity is SchematicTrait)
		{
			val schematic = entity.getSchematic() ?: return false;
			
			return entity.items.has(schematic.input.item);
		}
		return entity.items != null && hasItems;
	}
	
	override fun display(stats: BlockStats)
	{
		for (schematic in schematics)
		{
			stats.add(BlockStat.input, schematic.input);
		}
	}
}