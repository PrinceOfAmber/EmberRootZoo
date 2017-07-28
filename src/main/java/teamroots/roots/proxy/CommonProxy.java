package teamroots.roots.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import teamroots.roots.RegistryManager;
import teamroots.roots.Roots; 
import teamroots.roots.capability.RootsCapabilityManager;
import teamroots.roots.effect.EffectManager; 
import teamroots.roots.network.PacketHandler; 
import teamroots.roots.ritual.RitualRegistry;
import teamroots.roots.spell.SpellRegistry;
import teamroots.roots.util.FeyMagicManager;
import teamroots.roots.util.Fields;
import teamroots.roots.util.OfferingUtil;

public class CommonProxy {
	
	public void preInit(FMLPreInitializationEvent event){
		RootsCapabilityManager.register();
		EffectManager.init();
		RegistryManager.registerAll();
		PacketHandler.registerMessages();
		SpellRegistry.init();
		RitualRegistry.init();
		OfferingUtil.init();
		FeyMagicManager.init();
	}
	
	 
}
