package darkman2412.AutoFurnace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryListener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.FurnaceAndDispenser;

public class AFInventoryListener extends InventoryListener {
	private static AutoFurnace plugin;

	public AFInventoryListener(final AutoFurnace plugin) {
		AFInventoryListener.plugin = plugin;
	}

	private HashMap<Location, ItemStack> fuel = new HashMap<Location, ItemStack>();
	private boolean givebucket = false;
	static List<BlockFace> directions = Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
	private static List<Material> burnables = Arrays.asList(Material.IRON_ORE, Material.GOLD_ORE, Material.SAND, Material.COBBLESTONE, Material.PORK, Material.CLAY_BALL, Material.getMaterial(17), Material.CACTUS, Material.DIAMOND_ORE, Material.RAW_FISH);
	static List<Material> fuels = Arrays.asList(Material.COAL, Material.WOOD, Material.SAPLING, Material.STICK, Material.FENCE, Material.WOOD_STAIRS, Material.TRAP_DOOR, Material.WOOD, Material.getMaterial(58), Material.BOOKSHELF, Material.CHEST, Material.JUKEBOX, Material.NOTE_BLOCK, Material.LOCKED_CHEST, Material.LAVA_BUCKET);

	@Override
	public void onFurnaceBurn(FurnaceBurnEvent e) {
		Location location = e.getFurnace().getLocation();
		ItemStack fuelitem = e.getFuel();
		fuel.put(location, fuelitem);
		Furnace state = (Furnace) e.getFurnace().getState();
		Inventory finventory = state.getInventory();
		ItemStack slot1 = finventory.getItem(1);
		if (slot1 != null && slot1.getType() != Material.AIR) {
			return;
		}

		if ((finventory.getItem(1) == null || finventory.getItem(1).getType() == Material.AIR) && getFuelChest(e.getFurnace()) != null) {
			reloadFuel(e.getFurnace());
		}
	}

	@Override
	public void onFurnaceSmelt(final FurnaceSmeltEvent event) {
		final Location locfurnaces = event.getFurnace().getLocation();
		if (AutoFurnace.usedplayers.containsKey(locfurnaces)) {
			final Player player = AutoFurnace.usedplayers.get(locfurnaces);
			ItemStack result = event.getResult();
			String resultname = AutoFurnace.ResultNames.get(result.getType().name());
			if (resultname == null) {
				resultname = result.getType().name();
			}
			ItemStack givebucketi = fuel.get(locfurnaces);
			if (givebucketi != null) {
				Material givebucketm = fuel.get(locfurnaces).getType();
				if (givebucketm != null && givebucketm == Material.LAVA_BUCKET) {
					givebucket = true;
					fuel.remove(locfurnaces);
				}
			}
			int firstempty = player.getInventory().firstEmpty();
			if (getOutputChest(event.getFurnace()) == null && (Boolean) AutoFurnace.config.get("enable-item-teleport")) {
				if (firstempty >= 0 && player.isOnline()) {
					if (givebucket) {
						player.getInventory().addItem(new ItemStack(Material.BUCKET));
						givebucket = false;
					}
					player.getInventory().addItem(result);
					if (!(Boolean) AutoFurnace.config.get("disable-message")) {
						player.sendMessage(ChatColor.GREEN + "[AutoFurnace]" + ChatColor.GREEN + "You got a(n) " + resultname + ".");
					}
					final Block furnace = event.getFurnace();
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						@Override
						public void run() {
							((Furnace) furnace.getState()).getInventory().clear(2);
						}
					});
				} else if (firstempty < 0) {
					if (givebucket) {
						final Block furnace = event.getFurnace();
						((Furnace) furnace.getState()).getInventory().setItem(1, new ItemStack(Material.BUCKET));
						if (!(Boolean) AutoFurnace.config.get("disable-message")) {
							player.sendMessage(ChatColor.GREEN + "[AutoFurnace]" + ChatColor.GREEN + "You're " + resultname + " is ready in your furnace!");
							player.sendMessage(ChatColor.GREEN + "[AutoFurnace]" + ChatColor.GREEN + "Don't forget your bucket!");
						}
						givebucket = false;
					} else {
						if (!(Boolean) AutoFurnace.config.get("disable-message")) {
							player.sendMessage(ChatColor.GREEN + "[AutoFurnace]" + ChatColor.GREEN + "You're " + resultname + " is ready in your furnace!");
						}
					}
				}
			} else if (getOutputChest(event.getFurnace()) != null) {
				if (givebucket) {
					Block output = getOutputChest(event.getFurnace());
					Chest chest = (Chest) output.getState();
					Inventory cinventory = chest.getInventory();
					if (chest != null) {
						cinventory.addItem(new ItemStack(Material.BUCKET));
					}
					givebucket = false;
				}
				if (!(Boolean) AutoFurnace.config.get("disable-message")) {
					player.sendMessage(ChatColor.GREEN + "[AutoFurnace]" + ChatColor.GREEN + "You got a(n) " + resultname + " in your chest.");
				}
			} else if (!(Boolean) AutoFurnace.config.get("enable-item-teleport")) {
				if (!(Boolean) AutoFurnace.config.get("disable-message")) {
					player.sendMessage(ChatColor.GREEN + "[AutoFurnace]" + ChatColor.GREEN + "You're " + resultname + " is ready in your furnace!");
				}
			}
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					Furnace furnace = (Furnace) event.getFurnace().getState();
					Block furnaceblock = event.getFurnace();
					ItemStack[] rawslot = furnace.getInventory().getContents();
					if (rawslot[0] == new ItemStack(Material.AIR) || rawslot[0] == null) {
						unload(furnaceblock, player);
						reload(furnaceblock);
					}
					if (rawslot[1] == null) {
						reloadFuel(furnaceblock);
					}
				}
			});
		}
	}

	static Block getInputChest(Block furnace) {
		Furnace state = (Furnace) furnace.getState();
		BlockFace facing = ((FurnaceAndDispenser) state.getData()).getFacing();
		BlockFace dir = directions.get((directions.indexOf(facing) + 1) % directions.size());
		Block adjacentchest = furnace.getRelative(dir);
		Block doublechest = adjacentchest.getRelative(dir);

		if (adjacentchest.getType() != Material.CHEST) {
			return null;
		}

		if (doublechest.getType() != Material.CHEST || doublechest.getType() == Material.AIR) {
			return adjacentchest;
		}

		Chest state1 = (Chest) doublechest.getState();
		ItemStack[] items = state1.getInventory().getContents();
		for (ItemStack item : items) {
			if (item != null && burnables.contains(item.getType())) {
				return doublechest;
			}
		}
		if (adjacentchest.getType() == Material.CHEST) {
			return adjacentchest;
		}

		return null;
	}

	private Block getOutputChest(Block furnace) {
		Furnace state = (Furnace) furnace.getState();
		BlockFace facing = ((FurnaceAndDispenser) state.getData()).getFacing();
		BlockFace dir = directions.get((directions.indexOf(facing) + 3) % directions.size());
		Block adjacentchest = furnace.getRelative(dir);
		Block doublechest = adjacentchest.getRelative(dir);
		if (adjacentchest.getType() == Material.CHEST && doublechest.getType() == Material.CHEST) {
			return doublechest;
		}

		if (adjacentchest.getType() == Material.CHEST) {
			return adjacentchest; //return that chest;
		}

		return null; // No output chest
	}

	static Block getFuelChest(Block furnace) {
		Furnace state = (Furnace) furnace.getState();
		BlockFace facing = ((FurnaceAndDispenser) state.getData()).getFacing();
		BlockFace dir = directions.get((directions.indexOf(facing) + 2) % directions.size());
		Block adjacentchest = furnace.getRelative(dir);
		Block doublechest = adjacentchest.getRelative(dir);

		if (adjacentchest.getType() != Material.CHEST) {
			return null;
		}

		if (doublechest.getType() != Material.CHEST || doublechest.getType() == Material.AIR) {
			return adjacentchest;
		}

		Chest state1 = (Chest) doublechest.getState();
		ItemStack[] items = state1.getInventory().getContents();
		for (ItemStack item : items) {
			if (item != null && fuels.contains(item.getType())) {
				return doublechest;
			}
		}
		if (adjacentchest.getType() == Material.CHEST) {
			return adjacentchest;
		}

		return null;
	}

	static void reload(Block furnace) {
		Furnace state = (Furnace) furnace.getState();
		Inventory finventory = state.getInventory();
		ItemStack slot0 = finventory.getItem(0);
		if (slot0 != null && slot0.getType() != Material.AIR) {
			return;
		}

		Block input = getInputChest(furnace);
		if (input == null) {
			return;
		}

		Chest chest = (Chest) input.getState();
		Inventory cinventory = chest.getInventory();
		ItemStack[] Items = cinventory.getContents();
		for (ItemStack items : Items) {
			if (items == null || !burnables.contains(items.getType())) {
				continue;
			}

			cinventory.clear(cinventory.first(items));
			finventory.setItem(0, items);
			break;
		}
	}

	static void reloadFuel(Block furnace) {
		Furnace state = (Furnace) furnace.getState();
		Inventory finventory = state.getInventory();
		ItemStack slot1 = finventory.getItem(1);
		if (slot1 != null && slot1.getType() != Material.AIR) {
			return;
		}

		Block input = getFuelChest(furnace);
		if (input == null) {
			return;
		}

		Chest chest = (Chest) input.getState();
		Inventory cinventory = chest.getInventory();
		ItemStack[] Items = cinventory.getContents();
		for (ItemStack items : Items) {
			if (items == null || !fuels.contains(items.getType())) {
				continue;
			}

			cinventory.clear(cinventory.first(items));
			finventory.setItem(1, items);
			break;
		}
	}

	private void unload(Block furnace, Player player) {
		Furnace state = (Furnace) furnace.getState();
		Block output = getOutputChest(furnace);
		if (output != null) {
			Chest chest = (Chest) output.getState();

			Inventory finventory = state.getInventory();
			Inventory cinventory = chest.getInventory();
			ItemStack item = finventory.getItem(2);
			finventory.clear(2);
			HashMap<Integer, ItemStack> remainingstuff = cinventory.addItem(item);
			if (!remainingstuff.isEmpty()) {
				finventory.setItem(2, remainingstuff.get(0));
				if (!(Boolean) AutoFurnace.config.get("disable-full")) {
					player.sendMessage(ChatColor.GREEN + "[AutoFurnace] You're output chest is full!");
				}
			}
		}

	}
}
