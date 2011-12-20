package darkman2412.AutoFurnace;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AFPlayerListener extends PlayerListener {
	public static Player player = null;
	public static Location locfurn = null;

	public AFPlayerListener(final AutoFurnace plugin) {
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!((Boolean) AutoFurnace.config.get("leftclick")) && (event.getClickedBlock().getType() != Material.FURNACE || event.getClickedBlock().getType() != Material.BURNING_FURNACE/*||event.getClickedBlock().getType()!=Material.SIGN*/)) {
			return;
		}

		Player player = event.getPlayer();
		Block isFurnace = event.getClickedBlock();
		if (isFurnace == null) {
			return;
		}
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && isFurnace.getType().equals(Material.FURNACE) || isFurnace.getType().equals(Material.BURNING_FURNACE)) {
			if (AFInventoryListener.getInputChest(isFurnace) != null && (isFurnace.getType().equals(Material.FURNACE) || isFurnace.getType().equals(Material.BURNING_FURNACE))) {
				Location locfurn = event.getClickedBlock().getLocation();
				AutoFurnace.usedplayers.put(locfurn, player);
				AFInventoryListener.reload(isFurnace);
			} else {
				Location locfurn = event.getClickedBlock().getLocation();
				AutoFurnace.usedplayers.put(locfurn, player);
			}
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				Furnace state = (Furnace) event.getClickedBlock().getState();
				Inventory finventory = state.getInventory();
				ItemStack slot1 = finventory.getItem(1);
				if (slot1 != null && slot1.getType() != Material.AIR) {
					return;
				}

				if ((finventory.getItem(1) == null || finventory.getItem(1).getType() == Material.AIR) && AFInventoryListener.getFuelChest(event.getClickedBlock()) != null) {
					AFInventoryListener.reloadFuel(event.getClickedBlock());
				}
			}
		}
	}
}