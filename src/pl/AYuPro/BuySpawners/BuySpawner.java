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
			sender.sendMessage(ChatColor.RED + "� ��� �� ���������� ����.");
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("spawner")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "��� ������� �� �������� � �������.");
				return true;
			} else {
				Player player = (Player) sender;
				try {
					arg = args[0];
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "�� ������ ������� ��� ����.");
					return true;
				}
				if (args[0].toLowerCase().equals("help")){
				     sender.sendMessage(ChatColor.RED + "========== ������ ��������� ����� �����: ==========");
				     sender.sendMessage(ChatColor.GOLD + "pig - ������");
				     sender.sendMessage(ChatColor.GOLD + "wolf - ����");
				     sender.sendMessage(ChatColor.GOLD + "zombie - �����");
				     sender.sendMessage(ChatColor.GOLD + "skeleton - ������");
				     sender.sendMessage(ChatColor.GOLD + "spider - ����");
				     sender.sendMessage(ChatColor.GOLD + "pig_zombie - ����������");
				     sender.sendMessage(ChatColor.GOLD + "creeper - ������");
				     sender.sendMessage(ChatColor.GOLD + "enderman - ��������, �������� ����");
				     sender.sendMessage(ChatColor.GOLD + "blaze - �����");
				     return true;
				}
				CreatureType crtype;
				
				try {
					crtype = CreatureType.valueOf(args[0].toUpperCase());
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "������������ �������� ��������, �������������� �������� /spawner help");
					return true;
				}
				
				Block b = player.getLocation().getBlock();
				
				if (!(wgPlugin.canBuild(player, b))){
					sender.sendMessage(ChatColor.RED + "��� ��������� �������� � ��� ������ ���� ����� � �������.");
					return true;
				}

				if (getConfig().getBoolean("spawner." + arg.toLowerCase() + ".enabled")||player.isOp()) {
					if (getConfig().getBoolean("spawner." + arg.toLowerCase() + ".vip")){
						if (!(sender.hasPermission("spawner.vip"))){
							sender.sendMessage(ChatColor.RED + "� ��� �� ���������� ����.");
							return true;
						}
					}
					EconomyResponse r = econ.withdrawPlayer(player.getName(), getConfig().getInt("spawner." + arg.toLowerCase() + ".price"));
					if (r.transactionSuccess()) {
						try {
							b.setType(Material.MOB_SPAWNER);
							CreatureSpawner s = (CreatureSpawner) b.getState();
							s.setCreatureType(crtype);
							sender.sendMessage(ChatColor.GREEN + "������� " + arg.toLowerCase() + "(s) ����������.");
							return true;
						} catch (Exception e){
							econ.depositPlayer(player.getName(), getConfig().getInt("spawner." + arg.toLowerCase() + ".price"));
							sender.sendMessage(ChatColor.RED + "������� �� ��� ���������� ��-�� ������.");
							return true;
						}
						
					} else {
						sender.sendMessage(ChatColor.RED + "� ��� ������������ �������.");
						return true;
					}
				} else {
					sender.sendMessage(ChatColor.RED + "���� ��� ����� � ������ ����������� ��� �����������.");
					return true;
				}
			}
		}
		
		
		
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
}
