package to.joe.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.PlayerInventory;

import to.joe.J2;

public class Damages {
	public ArrayList<String> PvPsafe,PvEsafe;
	public HashMap<String,ArrayList<Wolf>> allWolf;
	J2 j2;
	public Damages(J2 j2){
		this.j2=j2;
		this.startDamageTimer();
		this.restartManager();
	}
	public void restartManager(){
		this.clear();
		this.allWolf=new HashMap<String,ArrayList<Wolf>>();
		this.timer1=new ArrayList<String>();
		this.timer2=new ArrayList<String>();
	}
	public void processJoin(String name){
		if(j2.safemode){
			this.protect(name);
		}
	}
	public void protect(String name){
		this.protectP(name);
		this.protectE(name);
	}
	public void protectP(String name){
		if(!this.PvPsafe.contains(name)){
			this.PvPsafe.add(name);
		}
	}
	public void protectE(String name){
		if(!this.PvEsafe.contains(name)){
			this.PvEsafe.add(name);
		}
	}
	public void danger(String name){
		this.dangerP(name);
		this.dangerE(name);
	}
	public void dangerP(String name){
		this.PvPsafe.remove(name);
	}
	public void dangerE(String name){
		this.PvEsafe.remove(name);
	}
	public void clear(){
		this.PvPsafe=new ArrayList<String>();
		this.PvEsafe=new ArrayList<String>();
	}
	public boolean woof(String target){
		List<Player> list=j2.getServer().matchPlayer(target);
		if(list.size()!=1)
			return false;
		Player player=list.get(0);
		this.danger(player.getName());
		ArrayList<Wolf> wlist=new ArrayList<Wolf>();
		boolean hated=j2.ihatewolves;
		PlayerInventory i=player.getInventory();
		i.setBoots(null);
		i.setChestplate(null);
		i.setHelmet(null);
		i.setLeggings(null);
		j2.ihatewolves=false;
		for(int x=0;x<10;x++){
			Wolf bob=(Wolf)player.getWorld().spawnCreature(player.getLocation(),CreatureType.WOLF);
			wlist.add(bob);
			bob.setAngry(true);
			bob.setTarget(player);
		}
		j2.ihatewolves=hated;
		allWolf.put(player.getName(), wlist);
		return true;
	}
	public void arf(String target){
		if(allWolf.containsKey(target)){
			for(Wolf i:allWolf.get(target)){
				i.damage(100);
			}
			allWolf.remove(target);
			if(j2.safemode){
				this.protect(target);
			}
		}
	}
	private boolean stop;
	private ArrayList<String> timer1;
	private ArrayList<String> timer2;
	public void addToTimer(String name){
		synchronized(sync){
			timer1.add(name);
		}
	}
	private void startDamageTimer() {
		stop = false;
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (stop) {
					timer.cancel();
					return;
				}
				update();
			}
		}, 1000, 1000);
	}
	private void update(){
		synchronized(sync){
			for(String n:timer2){
				this.processJoin(n);
			}
			timer2=new ArrayList<String>(timer1);
			timer1 = new ArrayList<String>();
		}
	}
	private Object sync= new Object();
}
