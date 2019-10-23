package manufacry;

import io.anuke.arc.Core
import io.anuke.arc.function.Consumer
import io.anuke.arc.function.Supplier
import io.anuke.arc.graphics.g2d.Draw
import io.anuke.arc.graphics.g2d.TextureRegion
import io.anuke.arc.util.*;
import io.anuke.mindustry.content.*;
import io.anuke.mindustry.game.UnlockableContent
import io.anuke.mindustry.mod.*;
import io.anuke.mindustry.type.*;
import io.anuke.mindustry.world.Tile
import io.anuke.mindustry.world.blocks.production.LiquidConverter
import manufacry.content.FactoryItems
import manufacry.world.blocks.distribution.LiquidSorter
import manufacry.world.blocks.production.ConfigurableCrafter

data class Recipe(val label: UnlockableContent, val type: ContentType, val inputItems: Array<ItemStack>,
									val inputLiquids: Array<LiquidStack>, val power: Float, val craftTime: Float,
									val outputItems: Array<ItemStack>, val outputLiquids: Array<LiquidStack>)
{
	
	override fun equals(other: Any?): Boolean
	{
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		
		other as Recipe
		
		if (label != other.label) return false
		if (type != other.type) return false
		if (!inputItems.contentEquals(other.inputItems)) return false
		if (!inputLiquids.contentEquals(other.inputLiquids)) return false
		if (power != other.power) return false
		if (craftTime != other.craftTime) return false
		if (!outputItems.contentEquals(other.outputItems)) return false
		if (!outputLiquids.contentEquals(other.outputLiquids)) return false
		
		return true
	}
	
	override fun hashCode(): Int
	{
		var result = label.hashCode()
		result = 31 * result + type.hashCode()
		result = 31 * result + inputItems.contentHashCode()
		result = 31 * result + inputLiquids.contentHashCode()
		result = 31 * result + power.hashCode()
		result = 31 * result + craftTime.hashCode()
		result = 31 * result + outputItems.contentHashCode()
		result = 31 * result + outputLiquids.contentHashCode()
		return result
	}
}

class Manufacry : Mod()
{
	
	private lateinit var testFactory: ConfigurableCrafter;
	
	//called at game initialisation, load content here
	override fun loadContent()
	{
		Log.info("Manufacry::loadContent() has been called.");
		FactoryItems.load();
		
		val solidBoiler = object : LiquidConverter("manufacry-steam-boiler-solid1")
		{
			init
			{
				requirements(Category.crafting, ItemStack.with(Items.lead, 65, Items.silicon, 40, Items.titanium, 60))
				outputLiquid = LiquidStack(FactoryItems.steamLow, 0.25f)
				craftTime = 120f
				size = 2
				hasPower = true
				hasItems = true
				hasLiquids = true
				rotate = false
				solid = true
				outputsLiquid = true
				
				consumes.power(1f)
				consumes.item(Items.coal)
				consumes.liquid(Liquids.water, 0.25f)
				
				val liquidRegion = reg("-liquid")
				val topRegion = reg("-top")
				val bottomRegion = reg("-bottom")
				
				this.drawIcons = Supplier {
					arrayOf<TextureRegion>(Core.atlas.find("$name-bottom"), Core.atlas.find("$name-top"))
				}
				
				this.drawer = Consumer { tile: Tile ->
					val mod = tile.entity.liquids
					
					val rotation = if (rotate) tile.rotation() * 90 else 0
					
					Draw.rect(reg(bottomRegion), tile.drawx(), tile.drawy(), rotation.toFloat())
					
					if (mod.total() > 0.001f)
					{
						Draw.color(outputLiquid.liquid.color)
						Draw.alpha(mod.get(outputLiquid.liquid) / liquidCapacity)
						Draw.rect(reg(liquidRegion), tile.drawx(), tile.drawy(), rotation.toFloat())
						Draw.color()
					}
					
					Draw.rect(reg(topRegion), tile.drawx(), tile.drawy(), rotation.toFloat())
				}
			}
			
			override fun generateIcons(): Array<TextureRegion>
			{
				return arrayOf<TextureRegion>(Core.atlas.find("$name-bottom"), Core.atlas.find("$name-top"))
			}
		};
		
		testFactory = object : ConfigurableCrafter("manufacry-test-factory")
		{
			init
			{
				this.requirements(Category.crafting, ItemStack.with(Items.titanium, 10))
				size = 2;
				craftEffect = Fx.pulverize;
				updateEffect = Fx.purify;
				addRecipe(Recipe(label = Liquids.oil, type = ContentType.liquid, power = 0.1f, craftTime = 2f,
					inputItems = arrayOf(ItemStack(Items.coal, 4)), inputLiquids = arrayOf(LiquidStack(Liquids.water, 5f)),
					outputItems = emptyArray(), outputLiquids = arrayOf(LiquidStack(Liquids.oil, 1f))));
				addRecipe(
					Recipe(label = Items.graphite, type = ContentType.item, inputItems = arrayOf(ItemStack(Items.coal, 3)),
						inputLiquids = arrayOf(LiquidStack(Liquids.water, 0.1f)), power = 1.8f, craftTime = 30f,
						outputItems = arrayOf(ItemStack(Items.graphite, 2)), outputLiquids = emptyArray()));
				addRecipe(Recipe(label = FactoryItems.copperPlate, type = ContentType.item,
					inputItems = arrayOf(ItemStack(Items.copper, 2)), inputLiquids = emptyArray(),
					outputItems = arrayOf(ItemStack(FactoryItems.copperPlate, 1)), power = 2.0f, craftTime = 60f,
					outputLiquids = emptyArray()));
				addRecipe(Recipe(label = Items.phasefabric, type = ContentType.item,
					inputItems = arrayOf(ItemStack(Items.thorium, 4), ItemStack(Items.sand, 10)), inputLiquids = emptyArray(),
					outputItems = arrayOf(ItemStack(Items.phasefabric, 1)), power = 1.9f, craftTime = 90f,
					outputLiquids = emptyArray()));
				addRecipe(Recipe(label = Items.scrap, type = ContentType.item, inputItems = emptyArray(),
					inputLiquids = arrayOf(LiquidStack(Liquids.oil, 0.1f), LiquidStack(Liquids.slag, 0.1f)),
					outputItems = arrayOf(ItemStack(Items.scrap, 1)), power = 0.1f, craftTime = 2f,
					outputLiquids = emptyArray()));
				
			}
		};
		val factory2 = object : ConfigurableCrafter("manufacry-steam-boiler-solid")
		{
			init
			{
				this.requirements(Category.crafting, ItemStack.with(Items.titanium, 10))
				size = 2;
				craftEffect = Fx.steam;
				updateEffect = Fx.bubble;
				val steamLowRecipe = Recipe(label = FactoryItems.steamLow, type = ContentType.liquid,
					inputItems = arrayOf(ItemStack(Items.coal, 1)), inputLiquids = arrayOf(LiquidStack(Liquids.water, 1f)),
					power = 1.5f, craftTime = 30f, outputItems = emptyArray(),
					outputLiquids = arrayOf(LiquidStack(FactoryItems.steamLow, 0.66f)));
				addRecipe(steamLowRecipe);
				
				val liquidRegion = reg("-liquid")
				val topRegion = reg("-top")
				val bottomRegion = reg("-bottom")
				
				this.drawIcons = Supplier {
					arrayOf<TextureRegion>(Core.atlas.find("$name-bottom"), Core.atlas.find("$name-top"))
				}
				
				this.drawer = Consumer { tile: Tile ->
					val mod = tile.entity.liquids
					
					val rotation = if (rotate) tile.rotation() * 90 else 0
					
					Draw.rect(reg(bottomRegion), tile.drawx(), tile.drawy(), rotation.toFloat())
					
					if (mod.total() > 0.001f)
					{
						Draw.color(steamLowRecipe.outputLiquids[0].liquid.color)
						Draw.alpha(mod.get(steamLowRecipe.outputLiquids[0].liquid) / liquidCapacity)
						Draw.rect(reg(liquidRegion), tile.drawx(), tile.drawy(), rotation.toFloat())
						Draw.color()
					}
					
					Draw.rect(reg(topRegion), tile.drawx(), tile.drawy(), rotation.toFloat())
				}
			}
		};
		
		var liquidSorter = object : LiquidSorter("manufacry-liquid-sorter")
		{
			init
			{
				requirements(Category.liquid, ItemStack.with(Items.lead, 2, Items.metaglass, 2))
			}
		}
		
		var liquidSorterInverted = object : LiquidSorter("manufacry-liquid-sorter-inverted")
		{
			init
			{
				requirements(Category.liquid, ItemStack.with(Items.lead, 2, Items.metaglass, 2))
				invert = true;
			}
		}
		
	}
	
	//called after all mods have been loaded
	override fun init()
	{
		Log.info("Manufacry::init() has been called.");
	}
}
