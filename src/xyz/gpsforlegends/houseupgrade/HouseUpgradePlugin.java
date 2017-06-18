package xyz.gpsforlegends.houseupgrade;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HouseUpgradePlugin extends JavaPlugin{
	
	private static HouseUpgradePlugin instance;
	
	private static long shutdownTime;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		new File("plugins" + File.separator + "HouseUpgrade" + File.separator + "schematics").mkdirs();
		new File("plugins" + File.separator + "HouseUpgrade" + File.separator + "players").mkdirs();
		
		ConfigurationSerialization.registerClass(House.class);
		
		HouseUpgradePlugin.shutdownTime = getConfig().getLong("shutdown");

		HouseUpgradePlugin.instance = this;
		
		House.loadFromFile("GPSforLEGENDS");
		
		House h = House.houses.get("GPSforLEGENDS");
		
		h.placeBlocksAfterRestart();
		
		h.startUpgrading(5, 5, true);
	}
	
	@Override
	public void onDisable() {
		getConfig().set("shutdown", System.currentTimeMillis());
		
		House.houses.get("GPSforLEGENDS").saveToFile();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO Auto-generated method stub
		if(command.getName().equalsIgnoreCase("test")){
			Player p = (Player)sender;
			
			House h = new House(p.getName(), "test", p.getLocation());
			
			h.startUpgrading(10, 5, true);
		}
		return true;
	}
	
	public static HouseUpgradePlugin getInstance(){
		return HouseUpgradePlugin.instance;
	}
	
	public static long getShutdownTime(){
		return HouseUpgradePlugin.shutdownTime;
	}

}
