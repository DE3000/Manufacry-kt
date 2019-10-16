package manufacry.world.blocks.production

import io.anuke.mindustry.world.*;
import io.anuke.mindustry.type.Category;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.entities.type.TileEntity;
import io.anuke.mindustry.type.Item;
import java.io.DataOutput;
import java.io.DataInput;
import io.anuke.arc.scene.ui.layout.Table;
import io.anuke.arc.Core;
import io.anuke.mindustry.world.blocks.production.GenericCrafter;
import io.anuke.arc.function.Supplier;
import io.anuke.arc.function.Consumer;
import io.anuke.arc.scene.ui.ButtonGroup;
import io.anuke.arc.scene.ui.ImageButton;
import io.anuke.arc.scene.style.TextureRegionDrawable;
import io.anuke.arc.collection.Array;
import io.anuke.mindustry.ui.Styles;
import io.anuke.mindustry.gen.Tex;
import io.anuke.mindustry.Vars.*;
import io.anuke.arc.util.Align;
import io.anuke.mindustry.entities.type.Player;
import io.anuke.mindustry.game.Cicon
import io.anuke.arc.collection.ArrayMap
import io.anuke.arc.math.Mathf
import io.anuke.arc.util.Time
import io.anuke.mindustry.entities.Effects
import manufacry.Schematic
import manufacry.entities.traits.SchematicTrait
import manufacry.world.consumers.ConsumeItemSchematic
import manufacry.world.consumers.ConsumePowerSchematic

open class ConfigurableCrafter(name: String) : GenericCrafter(name)
{
	
	companion object
	{
		
		private var lastOutput: Item? = null;
		private var schematics: ArrayMap<Item, Schematic> = ArrayMap();
	}
	
	init
	{
		solid = true
		update = true
		hasItems = true
		hasPower = true
		configurable = true
	}
	
	fun addSchematic(schematic: Schematic)
	{
		// TODO Liquids
		val item = schematic.outputItem?.item ?: return;
		schematics.put(item, schematic);
	}
	
	override fun playerPlaced(tile: Tile)
	{
		if (lastOutput != null)
		{
			Core.app.post { tile.configure(lastOutput?.id?.toInt() ?: 0) };
		}
	}
	
	override fun init()
	{
		consumes.add(ConsumeItemSchematic(schematics.values().toArray()));
		consumes.add(ConsumePowerSchematic(schematics.values().toArray()));
		super.init();
	}

//	override fun draw(tile: Tile) {
//		var tilesize = Vars.tilesize.toFloat();
//		Draw.rect(
//			region,
//			tile.drawx().toFloat(),
//			tile.drawy().toFloat(),
//			tilesize,
//			tilesize,
//			tile.rotation().toFloat() * 90f
//		);
//	}
	
	override fun buildTable(tile: Tile, table: Table)
	{
		
		val entity: FactoryEntity = tile.entity();
		
		val holder = Supplier<Item> { entity.getSchematic()?.outputItem?.item };
		
		val consumer = Consumer<Item> { item: Item? ->
			lastOutput = item;
			tile.configure(item?.id?.toInt() ?: -1);
		};
		
		//var items = Vars.content.items();
		val items = Array<Item>();
		
		for (key in schematics.keys())
		{
			items.add(key);
		}
		
		val group = ButtonGroup<ImageButton>();
		group.setMinCheckCount(0);
		val cont = Table();
		cont.defaults().size(38f);
		
		for ((i, item) in items.withIndex())
		{
			//if(!data.isUnlocked(item) && world.isZone()) continue;
			
			val cell = cont.addImageButton(Tex.whiteui, Styles.clearToggleTransi, 24f) { control.input.frag.config.hideConfig() };
			val button = cell.group(group).get();
			button.changed { consumer.accept(if (button.isChecked) item else null) };
			button.style.imageUp = TextureRegionDrawable(item.icon(Cicon.small));
			button.update { button.isChecked = holder.get() == item };
			
			if (i % 4 == 3)
			{
				cont.row();
			}
		}
		
		table.add(cont);
		table.row();
		table.label {
			entity.getSchematic()?.outputItem?.item?.localizedName() ?: "Select output."
		}.style(Styles.outlineLabel).center().growX().get().setAlignment(Align.center);
		
	}
	
	override fun configured(tile: Tile, player: Player, value: Int)
	{
		// TODO Clear items when config changed?
		tile.entity.items.clear();
		tile.entity<FactoryEntity>().currentSchematicID = value.toShort();
	}
	
	override fun newEntity(): TileEntity
	{
		return FactoryEntity();
	}
	
	override fun acceptItem(item: Item, tile: Tile, source: Tile): Boolean
	{
		val hasS = consumes.itemFilters.get(item.id.toInt());
		return hasS && tile.entity.items.get(item) < getMaximumAccepted(tile, item);
	}
	
	override fun outputsItems(): Boolean
	{
		return true;
	}
	
	override fun canProduce(tile: Tile): Boolean
	{
		val entity: FactoryEntity = tile.entity();
		val schematic = entity.getSchematic() ?: return false;
		if (tile.entity.items.get(schematic.input.item) < schematic.input.amount)
		{
			return false;
		}
		
		if (schematic.outputItem != null && tile.entity.items.get(schematic.outputItem.item) >= itemCapacity)
		{
			return false;
		}
		return outputLiquid == null || tile.entity.liquids.get(outputLiquid.liquid) < liquidCapacity;
	}
	
	override fun update(tile: Tile)
	{
		val entity: FactoryEntity = tile.entity();
		val schematic = entity.getSchematic() ?: return;
		if (entity.cons.valid())
		{
			entity.progress += getProgressIncrease(entity, schematic.craftTime);
			entity.totalProgress += entity.delta();
			entity.warmup = Mathf.lerpDelta(entity.warmup, 1f, 0.02f);
			
			if (Mathf.chance(Time.delta().toDouble() * updateEffectChance))
			{
				Effects.effect(updateEffect, entity.x + Mathf.range(size * 4f), entity.y + Mathf.range(size * 4));
			}
		} else
		{
			entity.warmup = Mathf.lerp(entity.warmup, 0f, 0.02f);
		}
		
		if (entity.progress >= 1f)
		{
			entity.items.remove(schematic.input);
			if (schematic.outputItem != null)
			{
				useContent(tile, schematic.outputItem.item);
				for (i in 1..schematic.outputItem.amount)
				{
					offloadNear(tile, schematic.outputItem.item);
				}
			}
			
			// TODO Liquids
			if (outputLiquid != null)
			{
				useContent(tile, outputLiquid.liquid);
				handleLiquid(tile, tile, outputLiquid.liquid, outputLiquid.amount);
			}
			
			Effects.effect(craftEffect, tile.drawx(), tile.drawy());
			entity.progress -= 1f;
		}
		
		if (schematic.outputItem != null && tile.entity.timer.get(timerDump, dumpTime.toFloat()))
		{
			tryDump(tile, schematic.outputItem.item);
		}
		
		if (outputLiquid != null)
		{
			tryDumpLiquid(tile, outputLiquid.liquid);
		}
	}
	
	inner class FactoryEntity : GenericCrafterEntity(), SchematicTrait
	{
		
		var currentSchematicID: Short = -1;
		
		override fun getSchematic(): Schematic?
		{
			return if (currentSchematicID > -1) schematics.get(content.item(currentSchematicID.toInt())) else null;
		}
		
		override fun config(): Int
		{
			return currentSchematicID.toInt();
		}
		
		//@Throws(IOException::class)
		override fun write(stream: DataOutput)
		{
			super.write(stream);
			stream.writeByte(currentSchematicID.toInt());
		}
		
		//@Throws(IOException::class)
		override fun read(stream: DataInput, revision: Byte)
		{
			super.read(stream, revision);
			val id = stream.readByte();
			currentSchematicID = id.toShort();
		}
	}
	
}


