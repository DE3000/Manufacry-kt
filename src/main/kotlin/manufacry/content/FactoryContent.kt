package manufacry.content

import io.anuke.mindustry.type.Item
import io.anuke.mindustry.game.ContentList
import io.anuke.arc.graphics.Color
import io.anuke.mindustry.type.ItemType
import io.anuke.arc.util.Log
import io.anuke.mindustry.content.Fx
import io.anuke.mindustry.content.StatusEffects
import io.anuke.mindustry.entities.Effects
import io.anuke.mindustry.type.Liquid
import io.anuke.mindustry.type.StatusEffect

object FactoryContent : ContentList
{
	
	lateinit var copperPlate: Item;
	lateinit var steamLow: Liquid;
	lateinit var steamHigh: Liquid;
	lateinit var scaldingEffect: StatusEffect;
	lateinit var waterDistilled:Liquid;
	
	override fun load()
	{
		Log.debug("Manufacry::FactoryItems::load() has been called.");
		//region StatusEffects
		scaldingEffect = object : StatusEffect()
		{
			init
			{
				this.damage = 0.01f;
				this.effect = Fx.steam;
			}
		};
		//endregion
		
		//region Items
		copperPlate = object : Item("manufacry-copper-plate", Color.valueOf("d99d73"))
		{
			init
			{
				type = ItemType.material;
				hardness = 1;
				cost = 2f;
				alwaysUnlocked = true;
			}
		};
		//endregion
		
		//region Liquids
		waterDistilled = object: Liquid("manufacry-water-distilled",Color.valueOf("4cabf7")){
			init
			{
				heatCapacity = 0.5f
				effect = StatusEffects.wet
			}
		}
		steamLow = object : Liquid("manufacry-steam-low", Color.valueOf("999999"))
		{
			init
			{
				viscosity = 1.0f;
				temperature = 0.8f;
				effect = scaldingEffect;
			}
		}
		steamHigh = object : Liquid("manufacry-steam-high", Color.valueOf("dcdcdc"))
		{
			init
			{
				viscosity = 1.5f;
				temperature = 1.1f;
				effect = scaldingEffect;
			}
		}
		//endregion
		
		
	}
}
