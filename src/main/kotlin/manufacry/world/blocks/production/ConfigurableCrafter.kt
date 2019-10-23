package manufacry.world.blocks.production

import io.anuke.arc.Core
import io.anuke.arc.collection.Array
import io.anuke.arc.function.Consumer
import io.anuke.arc.function.Supplier
import io.anuke.arc.graphics.g2d.TextureRegion
import io.anuke.arc.math.Mathf
import io.anuke.arc.scene.style.TextureRegionDrawable
import io.anuke.arc.scene.ui.ButtonGroup
import io.anuke.arc.scene.ui.ImageButton
import io.anuke.arc.scene.ui.layout.Table
import io.anuke.arc.util.Align
import io.anuke.arc.util.Log
import io.anuke.arc.util.Time
import io.anuke.mindustry.Vars.control
import io.anuke.mindustry.content.Fx
import io.anuke.mindustry.entities.Effects
import io.anuke.mindustry.entities.type.Player
import io.anuke.mindustry.entities.type.TileEntity
import io.anuke.mindustry.game.Cicon
import io.anuke.mindustry.gen.Sounds
import io.anuke.mindustry.gen.Tex
import io.anuke.mindustry.type.Item
import io.anuke.mindustry.ui.Styles
import io.anuke.mindustry.world.Block
import io.anuke.mindustry.world.Tile
import manufacry.Recipe
import manufacry.entities.traits.RecipeTrait
import manufacry.world.consumers.ConsumeRecipeItems
import manufacry.world.consumers.ConsumeRecipeLiquid
import manufacry.world.consumers.ConsumeRecipePower
import java.io.DataInput
import java.io.DataOutput

open class ConfigurableCrafter(name: String) : Block(name)
{
	
	protected var craftEffect: Effects.Effect = Fx.none
	protected var updateEffect: Effects.Effect = Fx.none
	protected var updateEffectChance = 0.04f
	private var outputsItems = false;
	protected var drawer: Consumer<Tile>? = null
	protected var drawIcons: Supplier<kotlin.Array<TextureRegion>>? = null
	private var lastOutputIndex: Int = -1;
	private var recipes: Array<Recipe> = Array();
	
	init
	{
		update = true
		solid = true
		hasItems = true
		hasPower = true
		hasLiquids = true
		health = 60
		idleSound = Sounds.machine
		idleSoundVolume = 0.03f
		configurable = true
	}
	
	fun addRecipe(newRecipe: Recipe)
	{
		for (recipe in recipes)
		{
			if (newRecipe.label === recipe.label) throw IllegalArgumentException(
				"Recipe must have a unique label! Found duplicate label ${newRecipe.label}")
		}
		recipes.add(newRecipe);
	}
	
	override fun playerPlaced(tile: Tile)
	{
		if (!configurable)
		{
			Core.app.post { tile.configure(0) };
		}
		else
		{
			Core.app.post { tile.configure(lastOutputIndex) };
		}
	}
	
	override fun init()
	{
		hasItems = false
		hasPower = false
		hasLiquids = false
		outputsLiquid = false
		outputsItems = false
		configurable = recipes.size > 1;
		for (recipe in recipes)
		{
			outputsItems = outputsItems || recipe.outputItems.isNotEmpty()
			outputsLiquid = outputsLiquid || recipe.outputLiquids.isNotEmpty()
			hasItems = hasItems || recipe.outputItems.isNotEmpty() && recipe.inputItems.isNotEmpty()
			hasLiquids = hasLiquids || recipe.outputLiquids.isNotEmpty() && recipe.inputLiquids.isNotEmpty()
			hasPower = hasPower || recipe.power > 0
		}
		
		consumes.add(ConsumeRecipeItems(recipes));
		consumes.add(ConsumeRecipePower(recipes));
		consumes.add(ConsumeRecipeLiquid(recipes));
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
		
		val holder = Supplier<Recipe> { entity.getRecipe() };
		
		val consumer = Consumer<IndexedValue<Recipe>> { recipe: IndexedValue<Recipe>? ->
			lastOutputIndex = recipe?.index ?: -1;
			tile.configure(lastOutputIndex);
		};
		
		val group = ButtonGroup<ImageButton>();
		group.setMinCheckCount(0);
		val cont = Table();
		cont.defaults().size(38f);
		
		for (indexRecipe in recipes.withIndex())
		{
			//if(!data.isUnlocked(item) && world.isZone()) continue;
			
			val cell =
				cont.addImageButton(Tex.whiteui, Styles.clearToggleTransi, 24f) { control.input.frag.config.hideConfig() };
			val button = cell.group(group).get();
			button.changed { consumer.accept(if (button.isChecked) indexRecipe else null) };
			button.style.imageUp = TextureRegionDrawable(indexRecipe.value.label.icon(Cicon.small));
			button.update { button.isChecked = holder.get() == indexRecipe.value };
			
			if (indexRecipe.index % 4 == 3)
			{
				cont.row();
			}
		}
		
		table.add(cont);
		table.row();
		table.label {
			entity.getRecipe()?.label?.localizedName() ?: "Select output."
		}.style(Styles.outlineLabel).center().growX().get().setAlignment(Align.center);
		
	}
	
	override fun configured(tile: Tile, player: Player, value: Int)
	{
		Log.info("Configured: ${tile.block().name} -> $value ($lastOutputIndex)");
		// TODO Clear items when config changed?
		tile.entity.items.clear();
		tile.entity.liquids.clear();
		tile.entity<FactoryEntity>().currentRecipeID = value.toShort();
	}
	
	override fun newEntity(): TileEntity
	{
		return FactoryEntity();
	}
	
	override fun outputsItems(): Boolean
	{
		return outputsItems;
	}
	
	override fun canProduce(tile: Tile): Boolean
	{
		Log.info("Crafter canProduce");
		val entity: FactoryEntity = tile.entity();
		val recipe = entity.getRecipe() ?: return false;
		Log.info("Crafter has recipe in canProduce making ${recipe.label.name}");
		// check input items
		for (inputItem in recipe.inputItems)
		{
			if (tile.entity.items.get(inputItem.item) < inputItem.amount)
			{
				Log.info("\tNo item input: $inputItem");
				return false;
			}
		}
		
		// check input liquids
		for (inputLiquid in recipe.inputLiquids)
		{
			if (tile.entity.liquids.get(inputLiquid.liquid) < inputLiquid.amount)
			{
				Log.info("\tNo liquid input: $inputLiquid");
				return false;
			}
		}
		
		// check output items
		if (outputsItems())
		{
			for (outputItem in recipe.outputItems)
			{
				
				if (tile.entity.items.get(outputItem.item) >= itemCapacity)
				{
					Log.info("\tOutput items full: $outputItem");
					return false;
				}
			}
		}
		
		//check output liquids
		if (outputsLiquid)
		{
			for (outputLiquid in recipe.outputLiquids)
			{
				if (tile.entity.liquids.get(outputLiquid.liquid) >= liquidCapacity)
				{
					Log.info("\tOutput liquid full: $outputLiquid");
					return false;
				}
			}
		}
		
		return true;
	}
	
	override fun update(tile: Tile)
	{
		val entity: FactoryEntity = tile.entity();
		val recipe = entity.getRecipe() ?: return;
		Log.info("Crafter has recipe in update(${canProduce(tile)}): " + entity.cons.valid());
		for (cons in entity.block.consumes.all())
		{
			Log.info("${cons.type()}\t${cons.valid(entity)}");
		}
		if (entity.cons.valid())
		{
			Log.info("Crafter valid");
			entity.progress += getProgressIncrease(entity, recipe.craftTime);
			entity.totalProgress += entity.delta();
			entity.warmup = Mathf.lerpDelta(entity.warmup, 1f, 0.02f);
			
			if (Mathf.chance(Time.delta().toDouble() * updateEffectChance))
			{
				Effects.effect(updateEffect, entity.x + Mathf.range(size * 4f), entity.y + Mathf.range(size * 4));
			}
		}
		else
		{
			entity.warmup = Mathf.lerp(entity.warmup, 0f, 0.02f);
		}
		
		if (entity.progress >= 1f)
		{
			Log.info("Crafter trigger");
			// Remove items/liquids from internal storage
			entity.cons.trigger();
			
			for (outputItem in recipe.outputItems)
			{
				useContent(tile, outputItem.item);
				for (i in 1..outputItem.amount)
				{
					offloadNear(tile, outputItem.item);
				}
			}
			
			for (outputLiquid in recipe.outputLiquids)
			{
				useContent(tile, outputLiquid.liquid);
				handleLiquid(tile, tile, outputLiquid.liquid, outputLiquid.amount);
			}
			
			Effects.effect(craftEffect, tile.drawx(), tile.drawy());
			entity.progress -= 1f;
		}
		
		for (outputItem in recipe.outputItems)
		{
			if (tile.entity.timer.get(timerDump, dumpTime.toFloat()))
			{
				tryDump(tile, outputItem.item);
			}
		}
		
		for (outputLiquid in recipe.outputLiquids)
		{
			tryDumpLiquid(tile, outputLiquid.liquid);
		}
	}
	
	override fun shouldIdleSound(tile: Tile): Boolean
	{
		return tile.entity.cons.valid()
	}
	
	override fun draw(tile: Tile)
	{
		drawer?.accept(tile) ?: super.draw(tile)
	}
	
	public override fun generateIcons(): kotlin.Array<TextureRegion>
	{
		Log.info("icons ${drawIcons?.get()}");
		return drawIcons?.get() ?: super.generateIcons();
	}
	
	override fun getMaximumAccepted(tile: Tile?, item: Item?): Int
	{
		return itemCapacity;
	}
	
	inner class FactoryEntity : TileEntity(), RecipeTrait
	{
		
		var progress: Float = 0f
		var totalProgress: Float = 0f
		var warmup: Float = 0f
		var currentRecipeID: Short = -1;
		
		override fun getRecipe(): Recipe?
		{
			return if (currentRecipeID > -1) recipes.get(currentRecipeID.toInt()) else null;
		}
		
		override fun config(): Int
		{
			return currentRecipeID.toInt();
		}
		
		//@Throws(IOException::class)
		override fun write(stream: DataOutput)
		{
			super.write(stream);
			stream.writeFloat(progress)
			stream.writeFloat(warmup)
			stream.writeByte(currentRecipeID.toInt());
		}
		
		//@Throws(IOException::class)
		override fun read(stream: DataInput, revision: Byte)
		{
			super.read(stream, revision);
			progress = stream.readFloat();
			warmup = stream.readFloat();
			currentRecipeID = stream.readByte().toShort();
		}
	}
	
}


