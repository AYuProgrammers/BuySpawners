package pl.AYuPro.BuySpawners;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

@SuppressWarnings("deprecation")
public class BuySpawner extends JavaPlugin {

	public static Economy econ = null;
	private String arg;
    WorldGuardPlugin wgPlugin = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
	
	public void onEnable() {
		
		getConfig().options().copyDefaults(true);
		saveConfig();
		if (!setupEconomy()) {Bukkit.getServer().getLogger().severe(String.format("[%s] - ", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		Bukkit.getServer().getLogger().info("BuySpawners by AYuPro Loaded");
	}
	
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer()
				.getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
	
	
	public void onDisable() {
		Bukkit.getServer().getLogger().info("BuySpawners by AYuPro Unloaded");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
		if (!(sender.hasPermission("spawner.cmd"))){
			sender.sendMessage(ChatColor.RED + "У Вас не достаточно прав.");
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("spawner")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Эта команда не работает в консоли.");
				return true;
			} else {
				Player player = (Player) sender;
				try {
					arg = args[0];
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Вы должны указать тип моба.");
					return true;
				}
				if (args[0].toLowerCase().equals("help")){
				     sender.sendMessage(ChatColor.RED + "========== Список возможных типов мобов: ==========");
				     sender.sendMessage(ChatColor.GOLD + "pig - свинья");
				     sender.sendMessage(ChatColor.GOLD + "wolf - волк");
				     sender.sendMessage(ChatColor.GOLD + "zombie - зомби");
				     sender.sendMessage(ChatColor.GOLD + "skeleton - скелет");
				     sender.sendMessage(ChatColor.GOLD + "spider - паук");
				     sender.sendMessage(ChatColor.GOLD + "pig_zombie - свинозомби");
				     sender.sendMessage(ChatColor.GOLD + "creeper - крипер");
				     sender.sendMessage(ChatColor.GOLD + "enderman - эндермен, странник края");
				     sender.sendMessage(ChatColor.GOLD + "blaze - ифрит");
				     return true;
				}
				CreatureType crtype;
				
				try {
					crtype = CreatureType.valueOf(args[0].toUpperCase());
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Неправильное название существа, воспользуйтесь справкой /spawner help");
					return true;
				}
				
				Block b = player.getLocation().getBlock();
				
				if (!(wgPlugin.canBuild(player, b))){
					sender.sendMessage(ChatColor.RED + "Для установки спавнера у Вас должны быть права в регионе.");
					return true;
				}

				if (getConfig().getBoolean("spawner." + arg.toLowerCase() + ".enabled")||player.isOp()) {
					if (getConfig().getBoolean("spawner." + arg.toLowerCase() + ".vip")){
						if (!(sender.hasPermission("spawner.vip"))){
							sender.sendMessage(ChatColor.RED + "У Вас не достаточно прав.");
							return true;
						}
					}
					EconomyResponse r = econ.withdrawPlayer(player.getName(), getConfig().getInt("spawner." + arg.toLowerCase() + ".price"));
					if (r.transactionSuccess()) {
						try {
							b.setType(Material.MOB_SPAWNER);
							CreatureSpawner s = (CreatureSpawner) b.getState();
							s.setCreatureType(crtype);
							sender.sendMessage(ChatColor.GREEN + "Спавнер " + arg.toLowerCase() + "(s) установлен.");
							return true;
						} catch (Exception e){
							econ.depositPlayer(player.getName(), getConfig().getInt("spawner." + arg.toLowerCase() + ".price"));
							sender.sendMessage(ChatColor.RED + "Спавнер не был установлен из-за ошибки.");
							return true;
						}
						
					} else {
						sender.sendMessage(ChatColor.RED + "У Вас недостаточно средств.");
						return true;
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Этот тип мобов в списке отключенных или недоступных.");
					return true;
				}
			}
		}
		return false;
	}
}
