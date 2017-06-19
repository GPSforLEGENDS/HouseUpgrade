package xyz.gpsforlegends.houseupgrade.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import xyz.gpsforlegends.houseupgrade.House;

public class PlayerJoinQuit implements Listener{
	
	@EventHandler
	public void pj(PlayerJoinEvent evt){
		House.houses.put(evt.getPlayer().getName(), House.loadFromFile(evt.getPlayer().getName()));
	}
	
	@EventHandler
	public void pq(PlayerQuitEvent evt){
		House h = House.houses.get(evt.getPlayer().getName());
		
		if(h != null){
			h.saveToFile();
		}
		
		House.houses.remove(evt.getPlayer().getName());
	}

}
