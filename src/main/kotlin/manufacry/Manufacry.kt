package manufacry;

import io.anuke.arc.Core
import io.anuke.arc.function.Consumer
import io.anuke.arc.function.Supplier
import io.anuke.arc.graphics.g2d.Draw
import io.anuke.arc.graphics.g2d.TextureRegion
import io.anuke.arc.util.*;
import io.anuke.mindustry.content.*;
import io.anuke.mindustry.mod.*;
import io.anuke.mindustry.type.*;
import io.anuke.mindustry.world.Tile
import io.anuke.mindustry.world.blocks.production.LiquidConverter
import manufacry.content.FactoryItems
import manufacry.world.blocks.distribution.LiquidSorter
import manufacry.world.blocks.production.ConfigurableCrafter

data class Schematic(val input: ItemStack, val outputItem: ItemStack?, val power: Float, val craftTime: Float)

class Manufacry : Mod()
{
	
	private lateinit var testFactory: ConfigurableCrafter;
	
	//called at game initialisation, load content here
	override fun loadContent()
	{
		Log.info("Manufacry::loadContent() has been called.");
		FactoryItems.load();
		
		val solidBoiler = object : LiquidConverter("manufacry-steam-boiler-solid")
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
				Log.info("Name:\t$name\t${drawIcons.get()}")
				
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
				addSchematic(Schematic(ItemStack(Items.coal, 2), ItemStack(Items.graphite, 2), 1.8f, 30f));
				addSchematic(Schematic(ItemStack(Items.copper, 2), ItemStack(FactoryItems.copperPlate, 1), 2.0f, 60f));
				addSchematic(Schematic(ItemStack(Items.thorium, 4), ItemStack(Items.phasefabric, 1), 3.0f, 45f));
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
