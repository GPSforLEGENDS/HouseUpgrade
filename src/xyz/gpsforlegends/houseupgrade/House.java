package xyz.gpsforlegends.houseupgrade;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.util.Countable;

@SuppressWarnings("deprecation")
@SerializableAs("HouseUpgrade")
public class House implements ConfigurationSerializable{
	
	private boolean upgrading;
	
	private int level;
	
	private String owner;
	private String type;
	
	private Location loc;
	
	private CuboidClipboard cc;
	private Vector remainingBlocks;
	
	//Only stores houses where the owner is online
	public static ConcurrentHashMap<String, House> houses = new ConcurrentHashMap<>();
	
	public House(String owner, String type, Location loc){
		this.owner = owner;
		this.type = type;
		this.loc = loc;
		upgrading = false;
		level = 0;
		remainingBlocks = new Vector(0, 0, 0);
		
		House.houses.put(owner, this);
	}
	
	public House(String owner, String type, Location loc, boolean upgrading){
		this.owner = owner;
		this.type = type;
		this.loc = loc;
		this.upgrading = upgrading;
		level = 0;
		remainingBlocks = new Vector(0, 0, 0);
		
		House.houses.put(owner, this);
	}
	
	//Time in minutes
	public void startUpgrading(int placedBlocks, int time, boolean skipAir){
		
		try {
			cc = SchematicFormat.MCEDIT.load(getSchematicFile());
		} catch (DataException | IOException e) {
			e.printStackTrace();
		}
		
		if(cc != null){
			cc.setOrigin(new Vector(loc.getX(), loc.getY(), loc.getZ()));
			
			final EditSession es = new EditSession(new BukkitWorld(loc.getWorld()), Integer.MAX_VALUE);
			es.setFastMode(true);
			final Vector origin = new Vector(loc.getX(), loc.getY(), loc.getZ());
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					int i = 0;
					while(i < placedBlocks){
						if(remainingBlocks.getBlockX() >= cc.getWidth()){
							if(remainingBlocks.getBlockZ() >= cc.getLength() - 1){
								if(remainingBlocks.getBlockY() >= cc.getHeight() - 1){
									remainingBlocks = remainingBlocks.setX(0);
									remainingBlocks = remainingBlocks.setY(0);
									remainingBlocks = remainingBlocks.setZ(0);
									levelup();
									cancel();
								}
								remainingBlocks = remainingBlocks.setY(remainingBlocks.getBlockY() + 1);
								remainingBlocks = remainingBlocks.setZ(0);
							}
							remainingBlocks = remainingBlocks.setZ(remainingBlocks.getBlockZ() + 1);
							remainingBlocks = remainingBlocks.setX(0);
						}
						BaseBlock bb = cc.getBlock(remainingBlocks);
						
						if(skipAir && bb.isAir()){
							remainingBlocks = remainingBlocks.add(1, 0, 0);
							continue;
						}
						
						try {
							es.setBlock(new Vector(remainingBlocks.getBlockX(), remainingBlocks.getBlockY(), remainingBlocks.getBlockZ()).add(origin), bb);
						} catch (ArrayIndexOutOfBoundsException | MaxChangedBlocksException e) {
							e.printStackTrace();
						}
	
						remainingBlocks = remainingBlocks.add(1, 0, 0);
						i++;
					}
				}
			}.runTaskTimer(HouseUpgradePlugin.getInstance(), 1, calculatePeriod(placedBlocks, time, skipAir));
			
			upgrading = true;
		}
	}
	
	public void startUpgrading(int placedBlocks, int time, boolean skipAir, Vector offset){
		
		try {
			cc = SchematicFormat.MCEDIT.load(getSchematicFile());
		} catch (DataException | IOException e) {
			e.printStackTrace();
		}
		
		if(cc != null){
			cc.setOrigin(new Vector(loc.getX(), loc.getY(), loc.getZ()));
			
			final EditSession es = new EditSession(new BukkitWorld(loc.getWorld()), Integer.MAX_VALUE);
			es.setFastMode(true);
			final Vector origin = new Vector(loc.getX(), loc.getY(), loc.getZ());
			
			remainingBlocks = offset;
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					int i = 0;
					while(i < placedBlocks){
						if(remainingBlocks.getBlockX() >= cc.getWidth()){
							if(remainingBlocks.getBlockZ() >= cc.getLength() - 1){
								if(remainingBlocks.getBlockY() >= cc.getHeight() - 1){
									remainingBlocks = remainingBlocks.setX(0);
									remainingBlocks = remainingBlocks.setY(0);
									remainingBlocks = remainingBlocks.setZ(0);
									levelup();
									cancel();
								}
								remainingBlocks = remainingBlocks.setY(remainingBlocks.getBlockY() + 1);
								remainingBlocks = remainingBlocks.setZ(0);
							}
							remainingBlocks = remainingBlocks.setZ(remainingBlocks.getBlockZ() + 1);
							remainingBlocks = remainingBlocks.setX(0);
						}
						BaseBlock bb = cc.getBlock(remainingBlocks);
						
						if(skipAir && bb.isAir()){
							remainingBlocks = remainingBlocks.add(1, 0, 0);
							continue;
						}
						
						try {
							es.setBlock(new Vector(remainingBlocks.getBlockX(), remainingBlocks.getBlockY(), remainingBlocks.getBlockZ()).add(origin), bb);
						} catch (ArrayIndexOutOfBoundsException | MaxChangedBlocksException e) {
							e.printStackTrace();
						}
	
						remainingBlocks = remainingBlocks.add(1, 0, 0);
						i++;
					}
				}
			}.runTaskTimer(HouseUpgradePlugin.getInstance(), 1, calculatePeriod(placedBlocks, time, skipAir, offset));
			
			upgrading = true;
		}
	}
	
	private void levelup(){
		upgrading = false;
		level++;
		remainingBlocks = new Vector(0, 0, 0);
	}
	
	//Calculate how fast the blocks have to be placed. Returns -1 if no CuboidClipboard was found
	//Time in minutes
	//periodtime might be inaccurate for a short time
	private long calculatePeriod(int placedBlocks, int time, boolean skipAir){
		if(cc != null){
			int amount = 0;
			
			List<Countable<Integer>> blocks = cc.getBlockDistribution();
			
			int i;
			for(i = 0; i < blocks.size(); i++){
				if(!skipAir || blocks.get(i).getID() != 0){
					amount += blocks.get(i).getAmount();
				}
			}
			
			int periods = amount / placedBlocks;
			
			double periodTime = (double) (time * 60 * 20) / periods;
			
			return Math.round(periodTime);
		}
		
		return -1;
	}
	
	private long calculatePeriod(int placedBlocks, int time, boolean skipAir, Vector offset){
		if(cc != null){
			int amount = 0;
			Vector v = new Vector(0, 0, 0);
			
			for(int w = offset.getBlockX(); w < cc.getWidth(); w++){
				for(int l = offset.getBlockZ(); l < cc.getLength(); l++){
					for(int h = offset.getBlockX(); h < cc.getHeight(); h++){
						BaseBlock bb = cc.getBlock(v.add(w, h, l));
						
						if(!skipAir || bb.getId() != 0){
							amount++;
						}
					}
				}
			}
			
			int periods = amount / placedBlocks;
			
			double periodTime = (double) (time * 60 * 20) / periods;
			
			return Math.round(periodTime);
		}
		
		return -1;
	}
	
	public void placeBlocksAfterRestart(){
		if(upgrading){
			try {
				cc = SchematicFormat.MCEDIT.load(getSchematicFile());
			} catch (DataException | IOException e) {
				e.printStackTrace();
			}
			
			if(cc != null){
				long dif = System.currentTimeMillis() - HouseUpgradePlugin.getShutdownTime();
				
				int placedBlocks = getMaxPlacedBlocks();
				
				long period = calculatePeriod(placedBlocks, getTimeForNextLevel(), true);
				
				long q = dif / period;
				
				cc.setOrigin(new Vector(loc.getX(), loc.getY(), loc.getZ()));
				
				final EditSession es = new EditSession(new BukkitWorld(loc.getWorld()), Integer.MAX_VALUE);
				es.setFastMode(true);
				final Vector origin = new Vector(loc.getX(), loc.getY(), loc.getZ());
				
				int i = 0;
				while(i < q + placedBlocks){
					if(remainingBlocks.getBlockX() >= cc.getWidth()){
						if(remainingBlocks.getBlockZ() >= cc.getLength() - 1){
							if(remainingBlocks.getBlockY() >= cc.getHeight() - 1){
								remainingBlocks = remainingBlocks.setX(0);
								remainingBlocks = remainingBlocks.setY(0);
								remainingBlocks = remainingBlocks.setZ(0);
								levelup();
								break;
							}
							remainingBlocks = remainingBlocks.setY(remainingBlocks.getBlockY() + 1);
							remainingBlocks = remainingBlocks.setZ(0);
						}
						remainingBlocks = remainingBlocks.setZ(remainingBlocks.getBlockZ() + 1);
						remainingBlocks = remainingBlocks.setX(0);
					}
					BaseBlock bb = cc.getBlock(remainingBlocks);
					
					if(bb.isAir()){
						remainingBlocks = remainingBlocks.add(1, 0, 0);
						continue;
					}
					
					try {
						es.setBlock(new Vector(remainingBlocks.getBlockX(), remainingBlocks.getBlockY(), remainingBlocks.getBlockZ()).add(origin), bb);
					} catch (ArrayIndexOutOfBoundsException | MaxChangedBlocksException e) {
						e.printStackTrace();
					}
		
					remainingBlocks = remainingBlocks.add(1, 0, 0);
					i++;
				}
			}
		}
	}
	
	public boolean removeItemsToUpgrade(Player  p){
		
		List<ItemStack> toRemoveList = getItemsForNextLevel();
		
		if(!hasItemsToUpgrade(p, toRemoveList)){
			return false;
		}
		
		ItemStack[] toRemoveArr = new ItemStack[toRemoveList.size()];
		
		toRemoveList.toArray(toRemoveArr);
		
		p.getInventory().removeItem(toRemoveArr);
		
		return true;
	}
	
	private boolean hasItemsToUpgrade(Player p, List<ItemStack> toRemove){
		for(ItemStack needed : toRemove){
			if(!p.getInventory().containsAtLeast(needed, needed.getAmount())){
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public List<ItemStack> getItemsForNextLevel(){
		return (List<ItemStack>) HouseUpgradePlugin.getInstance().getConfig().getList("types." + type + ".levels." + (level + 1) + ".items");
	}
	
	public int getTimeForNextLevel(){
		return HouseUpgradePlugin.getInstance().getConfig().getInt("types." + type + ".levels." + (level + 1) + ".time");
	}
	
	public int getMaxPlacedBlocks(){
		return HouseUpgradePlugin.getInstance().getConfig().getInt("types." + type + ".levels." + (level + 1) + ".blocks-placed");
	}
	
	//Returns the schematic file if it exists otherwise return null
	public File getSchematicFile(){
		String schemName = getSchematicName(level + 1);
		File schem = new File("plugins" + File.separator + "HouseUpgrade" + File.separator + "schematics", schemName + ".schematic");
		
		if(schem != null && schem.exists()){
			return schem;
		} else{
			return null;
		}
	}
	
	public String getSchematicName(int level){
		return HouseUpgradePlugin.getInstance().getConfig().getString("types." + type + ".levels." + level + ".schematic");
	}
	
	public int getLevel() {
		return level;
	}
	
	public boolean isUpgrading(){
		return upgrading;
	}
	
	public String getOwner(){
		return owner;
	}
	
	public String getType(){
		return type;
	}

	public Location getLocation(){
		return loc;
	}

	@Override
	public Map<String, Object> serialize() {
		//TODO
		LinkedHashMap<String, Object> result = new LinkedHashMap<>();
		result.put("level", level);
		result.put("owner", owner);
		result.put("type", type);
		result.put("location", loc);
		result.put("upgrading", upgrading);
		result.put("vector.x", remainingBlocks.getBlockX());
		result.put("vector.y", remainingBlocks.getBlockY());
		result.put("vector.z", remainingBlocks.getBlockZ());
		return result;
	}
	
	public static House deserialize(Map<String, Object> map){
		
		int level = (int) map.get("level");
		String owner = (String) map.get("owner");
		String type = (String) map.get("type");
		Location loc = (Location) map.get("location");
		boolean upgrading = (boolean) map.get("upgrading");
		int x = (int) map.get("vector.x");
		int y = (int) map.get("vector.y");
		int z = (int) map.get("vector.z");
		
		House h = new House(owner, type, loc, true);
		
		h.level = level;
		h.upgrading = upgrading;
		h.remainingBlocks = new Vector(x, y, z);
		
		return h;
	}
	
	public static void loadFromFile(String owner){
		File file = new File("plugins" + File.separator + "HouseUpgrade" + File.separator + "players", owner + ".yml");
				
		if(file != null && file.exists()){
			YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
					
			House h = (House) playerConfig.get("house");
					
			houses.put(owner, h);
		}
	}
	
	public void saveToFile(){
		File file = new File("plugins" + File.separator + "HouseUpgrade" + File.separator + "players", owner + ".yml");
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
				
		playerConfig.set("house", this);
				
		try {
			playerConfig.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
