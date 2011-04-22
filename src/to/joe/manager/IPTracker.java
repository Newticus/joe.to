package to.joe.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;

import to.joe.J2Plugin;
import to.joe.util.Flag;


public class IPTracker {
	private J2Plugin j2;
	private HashMap<String,String> known;
	private HashMap<String,Integer> totalcount,bannedcount;
	public ArrayList<String> badlist;

	public IPTracker(J2Plugin j2){
		this.j2=j2;
		this.known=new HashMap<String,String>();
		this.totalcount=new HashMap<String,Integer>();
		this.bannedcount=new HashMap<String,Integer>();
		this.badlist=new ArrayList<String>();
	}
	public void incoming(String name, String IP){
		if(j2.debug)
			System.out.println("Checking "+name);
		j2.mysql.userIP(name,IP);
		HashMap<String,Boolean> names=new HashMap<String,Boolean>();
		HashMap<String,Boolean> ips=new HashMap<String,Boolean>();
		names.put(name, false);
		ips=getIPs(names,ips);
		names=getNames(names,ips);
		ips=getIPs(names,ips);
		names=getNames(names,ips);
		ips=getIPs(names,ips);
		names=getNames(names,ips);
		ips=getIPs(names,ips);
		names=getNames(names,ips);
		known.remove(name);
		totalcount.remove(name);
		bannedcount.remove(name);
		if(names.size()>1){
			String nameslist="";
			int ohnoes=0;
			for(String n:names.keySet()){
				if(!n.equalsIgnoreCase(name)){
					if(j2.mysql.checkBans(n)==null){
						nameslist+=n+" ";
					}
					else{
						nameslist+="<span style='color:red'>"+n+"</span> ";
						ohnoes++;
					}
				}
			}
			String newknown="<tr><td><a href='../alias/detector.php?name="+name+"'>"+name+"</a></td><td>"+nameslist+"</td></tr>";
			known.put(name, newknown);
			totalcount.put(name, names.size());
			bannedcount.put(name, ohnoes);
			if(ohnoes>0){
				badlist.add(name);
			}
			if(j2.debug)
				System.out.println("Adding to list");
		}
		else{
			if(j2.debug)
				System.out.println("Not enough to add");
		}
	}
	public HashMap<String,Boolean> getIPs(HashMap<String,Boolean> names,HashMap<String,Boolean> ips){
		Set<String> keyset = names.keySet();
		ArrayList<String> newips=new ArrayList<String>();
		ArrayList<String> searched=new ArrayList<String>();
		for(String key:keyset){
			if(!names.get(key)){
				searched.add(key);
				ArrayList<String> tempips=j2.mysql.IPGetIPs(key);
				for(String i:tempips){
					if(!i.equals("")&&!newips.contains(i)&&!keyset.contains(i)){
						newips.add(i);
					}
				}
			}
		}
		for(String s:searched){
			ips.remove(s);
			ips.put(s, true);
		}
		for(String ip:newips){
			if(j2.debug)
				System.out.println("Found IP: "+ip);
			ips.put(ip, false);
		}
		return ips;
	}

	public HashMap<String,Boolean> getNames(HashMap<String,Boolean> names,HashMap<String,Boolean> ips){
		Set<String> keyset = ips.keySet();
		ArrayList<String> newnames=new ArrayList<String>();
		ArrayList<String> searched=new ArrayList<String>();
		for(String key:keyset){
			if(!ips.get(key)){
				searched.add(key);
				ArrayList<String> tempnames=j2.mysql.IPGetNames(key);
				for(String i:tempnames){
					if(!i.equals("")&&!newnames.contains(i)&&!keyset.contains(i)){
						newnames.add(i);
					}
				}
			}
		}
		for(String s:searched){
			ips.remove(s);
			ips.put(s, true);
		}
		for(String name:newnames){
			if(j2.debug)
				System.out.println("Found Name: "+name);
			names.put(name, false);
		}
		return names;
	}

	public String getKnown(String name){
		if(known.containsKey(name)){
			return known.get(name);
		}
		return "";
	}
	public int getTotal(String name){
		if(totalcount.containsKey(name)){
			return totalcount.get(name);
		}
		return 0;
	}
	public int getBanned(String name){
		if(bannedcount.containsKey(name)){
			return bannedcount.get(name);
		}
		return 0;
	}
	public void processJoin(String name){
		if(badlist.contains(name)){
			int total=this.getTotal(name)-1;
			int banned=this.getBanned(name);
			j2.irc.ircAdminMsg("mc"+j2.servernumber+": "+name+" matches "+total+" others: "+banned+" banned");
			j2.chat.msgByFlag(Flag.ADMIN, ChatColor.LIGHT_PURPLE+"User "+ChatColor.WHITE+name+ChatColor.LIGHT_PURPLE+" matches "+total+" others: "+banned+" banned");
		}
	}
}