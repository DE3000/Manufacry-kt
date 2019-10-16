package manufacry.world.consumers;

import io.anuke.arc.collection.Array
import io.anuke.mindustry.entities.type.TileEntity;
import io.anuke.mindustry.world.consumers.ConsumePower;
import manufacry.Schematic
import manufacry.entities.traits.SchematicTrait

class ConsumePowerSchematic(private val schematics: Array<Schematic>) : ConsumePower(0f, 0f, false)
{
	override fun requestedPower(entity: TileEntity): Float
	{
		if (entity is SchematicTrait)
		{
			return entity.getSchematic()?.power ?: 0f;
		}
		return 0f;
	}
}
