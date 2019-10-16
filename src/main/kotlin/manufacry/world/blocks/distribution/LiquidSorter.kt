package manufacry.world.blocks.distribution

import io.anuke.arc.*
import io.anuke.arc.graphics.g2d.*
import io.anuke.arc.math.*
import io.anuke.arc.scene.style.TextureRegionDrawable
import io.anuke.arc.scene.ui.ButtonGroup
import io.anuke.arc.scene.ui.ImageButton
import io.anuke.arc.scene.ui.layout.*
import io.anuke.arc.util.ArcAnnotate.*
import io.anuke.mindustry.entities.type.*
import io.anuke.mindustry.type.*
import io.anuke.mindustry.world.*

import java.io.*

import io.anuke.mindustry.Vars.content
import io.anuke.mindustry.Vars.control
import io.anuke.mindustry.game.Cicon
import io.anuke.mindustry.gen.Tex
import io.anuke.mindustry.ui.Styles
import io.anuke.mindustry.world.meta.BlockGroup

open class LiquidSorter(name: String) : Block(name)
{
	
	companion object
	{
		
		private var lastLiquid: Liquid? = null
	}
	
	protected var invert: Boolean = false;
	
	init
	{
		update = true
		solid = true
		hasLiquids = true
		group = BlockGroup.liquids
		outputsLiquid = true
		instantTransfer = true;
		configurable = true;
	}
	
	override fun outputsItems(): Boolean
	{
		return false;
	}
	
	override fun playerPlaced(tile: Tile)
	{
		if (lastLiquid != null)
		{
			Core.app.post { tile.configure(lastLiquid!!.id.toInt()) }
		}
	}
	
	override fun configured(tile: Tile, player: Player, value: Int)
	{
		tile.entity<LiquidSorterEntity>().sortLiquid = content.liquid(value)
	}
	
	override fun draw(tile: Tile)
	{
		super.draw(tile)
		
		val entity = tile.entity<LiquidSorterEntity>()
		if (entity.sortLiquid == null) return
		
		Draw.color(entity.sortLiquid!!.color)
		Draw.rect("center", tile.worldx(), tile.worldy())
		Draw.color()
	}
	
	override fun acceptLiquid(current: Tile?, source: Tile?, liquid: Liquid?, amount: Float): Boolean
	{
		val to = getTileTarget(liquid!!, amount, current!!, source!!, false)
		return to != null && to.link().block().acceptLiquid(to, current, liquid, amount)
	}
	
	fun tileInfo(tag: String, tile: Tile?): String
	{
		return "$tag ${tile?.block()?.name}@(${tile?.x},${tile?.y})"
	}
	
	override fun handleLiquid(current: Tile?, source: Tile?, liquid: Liquid?, amount: Float)
	{
		val to = getTileTarget(liquid!!, amount, current!!, source!!, true)
		if (amount > 0) System.out.println(
						"Handle (${liquid.name}:${amount})\t${tileInfo("Current", current)}\t${tileInfo("Source",
										source)}\t${tileInfo("Destination", to)}");
		to!!.block().handleLiquid(to, current, liquid, amount);
	}
	
	internal fun isSame(tile: Tile, other: Tile?): Boolean
	{
		return other != null && other.block() === this && other.entity<LiquidSorterEntity>().sortLiquid === tile.entity<LiquidSorterEntity>().sortLiquid
	}
	
	internal fun getTileTarget(liquid: Liquid, amount: Float, dest: Tile, source: Tile, flip: Boolean): Tile?
	{
		val entity = dest.entity<LiquidSorterEntity>();
		
		val dir = source.relativeTo(dest.x.toInt(), dest.y.toInt()).toInt()
		println("Target ($dir)\tAmount:${amount}\t${tileInfo("Dest", dest)}\t${tileInfo("Source", source)}\t")
		if (dir == -1) return null
		val to: Tile
		
		if (liquid === entity.sortLiquid != invert)
		{
			//prevent 3-chains
			if (isSame(dest, source) && isSame(dest, dest.getNearby(dir)))
			{
				return null
			}
			to = dest.getNearby(dir)
		}
		else
		{
			val a = dest.getNearby(Mathf.mod(dir - 1, 4))
			val b = dest.getNearby(Mathf.mod(dir + 1, 4))
			val ac = a != null && !(a.block().instantTransfer && source.block().instantTransfer) && a.block().acceptLiquid(a,
							dest, liquid, amount)
			val bc = b != null && !(b.block().instantTransfer && source.block().instantTransfer) && b.block().acceptLiquid(b,
							dest, liquid, amount)
			
			if (ac && !bc)
			{
				to = a
			}
			else if (bc && !ac)
			{
				to = b
			}
			else if (!bc)
			{
				return null
			}
			else
			{
				if (dest.rotation().toInt() == 0)
				{
					to = a
					if (flip)
					{
						dest.rotation(1)
					}
				}
				else
				{
					to = b
					if (flip) dest.rotation(0)
				}
			}
		}
		
		return to
	}
	
	override fun buildTable(tile: Tile, table: Table)
	{
		val entity = tile.entity<LiquidSorterEntity>()
		
		val items = content.liquids()
		
		val group = ButtonGroup<ImageButton>()
		group.setMinCheckCount(0)
		val cont = Table()
		
		for (i in 0 until items.size)
		{
			val button =
							cont.addImageButton(Tex.clear, Styles.clearToggleTransi, 24f) { control.input.frag.config.hideConfig() }
											.size(38f).group(group).get()
			button.changed {
				tile.configure((if (button.isChecked) items.get(i).id else -1).toInt())
				control.input.frag.config.hideConfig()
				lastLiquid = items.get(i)
			}
			button.style.imageUp = TextureRegionDrawable(items.get(i).icon(Cicon.medium))
			button.isChecked = entity.sortLiquid === items.get(i)
			
			if (i % 4 == 3)
			{
				cont.row()
			}
		}
		
		table.add(cont)
	}
	
	override fun newEntity(): TileEntity
	{
		return LiquidSorterEntity()
	}
	
	inner class LiquidSorterEntity : TileEntity()
	{
		
		@Nullable
		internal var sortLiquid: Liquid? = null
		
		override fun config(): Int
		{
			return (if (sortLiquid == null) -1 else sortLiquid!!.id).toInt()
		}
		
		override fun version(): Byte
		{
			return 2
		}
		
		@Throws(IOException::class)
		override fun write(stream: DataOutput)
		{
			super.write(stream)
			stream.writeShort((if (sortLiquid == null) -1 else sortLiquid!!.id).toInt())
		}
		
		@Throws(IOException::class)
		override fun read(stream: DataInput, revision: Byte)
		{
			super.read(stream, revision)
			sortLiquid = content.liquid(stream.readShort().toInt())
			if (revision.toInt() == 1)
			{
				DirectionalItemBuffer(20, 45f).read(stream)
			}
		}
	}
	
}
