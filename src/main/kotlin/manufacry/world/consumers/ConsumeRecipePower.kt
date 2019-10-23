package manufacry.world.consumers;

import io.anuke.arc.collection.Array
import io.anuke.mindustry.entities.type.TileEntity;
import io.anuke.mindustry.world.consumers.ConsumePower;
import io.anuke.mindustry.world.consumers.ConsumeType

import manufacry.Recipe
import manufacry.entities.traits.RecipeTrait

class ConsumeRecipePower(private val recipes: Array<Recipe>) : ConsumePower(0f, 0f, false)
{
	
	override fun type(): ConsumeType
	{
		return ConsumeType.power;
	}
	
	override fun requestedPower(entity: TileEntity): Float
	{
		if (entity is RecipeTrait)
		{
			return entity.getRecipe()?.power ?: 0f;
		}
		return 0f;
	}
}
