package xyz.gpsforlegends.houseupgrade;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import xyz.gpsforlegends.houseupgrade.listeners.PlayerJoinQuit;

public class HouseUpgradePlugin extends JavaPlugin{
	
	private static HouseUpgradePlugin instance;
	
	private static long shutdownTime;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		registerListeners();
		
		new File("plugins" + File.separator + "HouseUpgrade" + File.separator + "schematics").mkdirs();
		new File("plugins" + File.separator + "HouseUpgrade" + File.separator + "players").mkdirs();
		
		ConfigurationSerialization.registerClass(House.class);
		
		HouseUpgradePlugin.shutdownTime = getConfig().getLong("shutdown");

		HouseUpgradePlugin.instance = this;
		
		House.updateAllHousesAfterRestart();
	}
	
	@Override
	public void onDisable() {
		reloadConfig();
		getConfig().set("shutdown", System.currentTimeMillis());
		saveConfig();
		House.houses.get("GPSforLEGENDS").saveToFile();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO Auto-generated method stub
		if(command.getName().equalsIgnoreCase("house")){
			if(args.length == 1){
				if(sender instanceof Player){
					Player p = (Player)sender;
					String type = args[0];
					
					if(getConfig().contains("types." + type)){
						House h = House.houses.get(p.getName());
						
						if(h == null){
							h = new House(p.getName(), "test", p.getLocation());
							House.houses.put(p.getName(), h);
						}
						
						if(h.hasNextLevel()){
							if(!h.isUpgrading()){
								if(h.removeItemsToUpgrade(p)){
									p.sendMessage("removed items for upgrade");
								} else{
									p.sendMessage("You dont have the required items");
									return true;
								}
								h.startUpgrading(h.getMaxPlacedBlocks(), h.getTimeForNextLevel(), true);
							}
						}
					}
				}
			}
		}
		return true;
	}
	
	public static HouseUpgradePlugin getInstance(){
		return HouseUpgradePlugin.instance;
	}
	
	public static long getShutdownTime(){
		return HouseUpgradePlugin.shutdownTime;
	}
	
	private void registerListeners(){
		PluginManager pm = Bukkit.getPluginManager();
		
		pm.registerEvents(new PlayerJoinQuit(), this);
	}

}
