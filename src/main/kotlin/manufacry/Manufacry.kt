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
import io.anuke.mindustry.world.blocks.power.ItemLiquidGenerator
import io.anuke.mindustry.world.blocks.production.LiquidConverter
import io.anuke.mindustry.world.meta.BuildVisibility
import manufacry.content.FactoryContent
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
	private lateinit var steamBoilerSoild: ConfigurableCrafter;
	private lateinit var steamBoilerLiquid: ConfigurableCrafter;
	private lateinit var turbineSmall: ItemLiquidGenerator;
	private lateinit var turbineMedium: ItemLiquidGenerator;
	private lateinit var turbineLarge: ItemLiquidGenerator;
	private lateinit var liquidSorter:LiquidSorter;
	private lateinit var liquidSorterInverted:LiquidSorter;
	
	//called at game initialisation, load content here
	override fun loadContent()
	{
		Log.info("Manufacry::loadContent() has been called.");
		FactoryContent.load();
		
		testFactory = object : ConfigurableCrafter("manufacry-test-factory")
		{
			init
			{
				this.requirements(Category.crafting, BuildVisibility.sandboxOnly, ItemStack.with(Items.titanium, 10))
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
				addRecipe(Recipe(label = FactoryContent.copperPlate, type = ContentType.item,
					inputItems = arrayOf(ItemStack(Items.copper, 2)), inputLiquids = emptyArray(),
					outputItems = arrayOf(ItemStack(FactoryContent.copperPlate, 1)), power = 2.0f, craftTime = 60f,
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
		
		// Boilers should not work as configurable crafters.
		steamBoilerSoild = object : ConfigurableCrafter("manufacry-steam-boiler-solid")
		{
			init
			{
				this.requirements(Category.crafting, ItemStack.with(Items.titanium, 10))
				size = 2;
				craftEffect = Fx.steam;
				updateEffect = Fx.bubble;
				addRecipe(Recipe(label = FactoryContent.steamLow, type = ContentType.liquid,
					inputItems = arrayOf(ItemStack(Items.coal, 1)), inputLiquids = arrayOf(LiquidStack(Liquids.water, 0.25f)),
					power = 0.5f, craftTime = 30f, outputItems = emptyArray(),
					outputLiquids = arrayOf(LiquidStack(FactoryContent.steamLow, 0.165f))));
				addRecipe(Recipe(label = FactoryContent.steamHigh, type = ContentType.liquid,
					inputItems = arrayOf(ItemStack(Items.pyratite, 1)), inputLiquids = arrayOf(LiquidStack(Liquids.water, 0.25f)),
					power = 0.5f, craftTime = 30f, outputItems = emptyArray(),
					outputLiquids = arrayOf(LiquidStack(FactoryContent.steamHigh, 0.25f))));
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
					val recipe = (tile.entity as FactoryEntity).getRecipe()
					
					if (recipe != null && mod.get(recipe.outputLiquids.first().liquid) > 0.0001f)
					{
						val liquidOut = recipe.outputLiquids.first()
						Draw.color(liquidOut.liquid.color)
						Draw.alpha(mod.get(liquidOut.liquid) / liquidCapacity)
						Draw.rect(reg(liquidRegion), tile.drawx(), tile.drawy(), rotation.toFloat())
						Draw.color()
					}
					
					Draw.rect(reg(topRegion), tile.drawx(), tile.drawy(), rotation.toFloat())
				}
			}
		};
		
		turbineSmall = object : ItemLiquidGenerator(false,true,"manufacry-steam-turbine-small")
		{
			init
			{
				requirements(Category.power, ItemStack.with(Items.copper, 35, Items.graphite, 25, Items.lead, 40, Items.silicon, 30))
				//defaults=true
				powerProduction = 10f
				hasLiquids = true
				size = 2
				minLiquidEfficiency = 0.8f
			}
			
			override fun getItemEfficiency(item: Item): Float
			{
				return 0f
			}
			
			override fun getLiquidEfficiency(liquid: Liquid): Float
			{
				return liquid.temperature
			}
		};
		
		liquidSorter = object : LiquidSorter("manufacry-liquid-sorter")
		{
			init
			{
				requirements(Category.liquid, ItemStack.with(Items.lead, 2, Items.metaglass, 2))
			}
		}
		
		liquidSorterInverted = object : LiquidSorter("manufacry-liquid-sorter-inverted")
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
