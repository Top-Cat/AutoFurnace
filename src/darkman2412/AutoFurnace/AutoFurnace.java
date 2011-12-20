package darkman2412.AutoFurnace;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

@SuppressWarnings("deprecation")
public class AutoFurnace extends JavaPlugin {
	public Configuration config1;
	private final AFInventoryListener inventoryListener = new AFInventoryListener(this);
	private final AFPlayerListener playerListener = new AFPlayerListener(this);
	static HashMap<Location, Player> usedplayers = new HashMap<Location, Player>();
	static HashMap<String, String> ResultNames = new HashMap<String, String>();
	static HashMap<String, Object> config = new HashMap<String, Object>();
	static HashMap<Location, Boolean> isenabled = new HashMap<Location, Boolean>();
	Logger log = Logger.getLogger("Minecraft");

	@Override
	public void onEnable() {
		loadConfig();
		log.info("AutoFurnace has been enabled! Thanks for using this!");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.FURNACE_SMELT, inventoryListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.FURNACE_BURN, inventoryListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		loadResultNames();
	}

	@Override
	public void onDisable() {
		log.info("AutoFurnace has been disabled.");
	}

	private void loadConfig() {
		config1 = getConfiguration();
		config1.setHeader("##############################", "#  AutoFurnace Config File!  #", "#            1.5             #", "##############################");
		config.put("enable-item-teleport", config1.getBoolean("enable-item-teleport", true));
		if (config1.getBoolean("announcements.overall-disable", false)) {
			config.put("disable-message", true);
			config.put("disable-full", true);
		} else {
			config.put("disable-message", config1.getBoolean("announcements.disable-message", false));
			config.put("disable-full", config1.getBoolean("announcements.disable-full", false));
		}
		config.put("leftclick", !config1.getBoolean("disable-leftclick", false));
		//config.put("auto-update", config1.getBoolean("auto-update", false));
		config1.save();
	}

	private void loadResultNames() {
		ResultNames.put("IRON_INGOT", "iron ingot");
		ResultNames.put("GOLD_INGOT", "gold ingot");
		ResultNames.put("GLASS", "glass");
		ResultNames.put("STONE", "smooth stone");
		ResultNames.put("GRILLED_PORK", "cooked porkchop");
		ResultNames.put("CLAY_BRICK", "clay brick");
		ResultNames.put("COOKED_FISH", "cooked fish");
		ResultNames.put("COAL", "charcoal");
		ResultNames.put("INK_SACK", "cactus green");
	}
}
