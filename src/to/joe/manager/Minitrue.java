package to.joe.manager;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.echo28.bukkit.vanish.Vanish;
import to.joe.J2;
import to.joe.util.Flag;

public class Minitrue {
	private J2 j2;
	private Vanish vanish;
	private boolean vanishing;
	public Minitrue(J2 j2){
		this.j2=j2;
		this.vanishing=false;
	}
	public void restartManager(){
		Vanish p = null;
		Plugin test = this.j2.getServer().getPluginManager().getPlugin("Vanish");
		if(test != null && test instanceof Vanish) {
			p = (Vanish)test;
		}
		if(p == null) {
			j2.logWarn("Failed to find Vanish. Oh dear.");
		}
		else{
			this.vanishing=true;
		}

		this.vanish = p;
	}
	public void processJoin(Player player){
		this.announceJoin(player.getName());
	}
	public void vanish(Player player){
		if(this.vanishing){
			vanish.vanish(player);
			if(this.invisible(player)){//assume player is NOW invisible
				this.announceLeave(player.getName());
			}
			else{//now visible
				this.announceJoin(player.getName());
			}
		}
		else{
			player.sendMessage(ChatColor.AQUA+"Can't vanish D:");
		}
	}
	public boolean invisible(Player player){
		if(this.vanishing)
			return vanish.invisible.contains(player);
		else return false;
	}
	public void announceJoin(String playerName){
		j2.chat.msgAll(ChatColor.YELLOW+"Now arriving: "+playerName);
	}
	public void announceLeave(String playerName){
		if(j2.users.isOnline(playerName)){
			j2.chat.msgAll(ChatColor.YELLOW+"Now departing: "+playerName);
		}
	}
	public boolean chat(Player player,String message){
		if(this.invisible(player)){
			j2.chat.aMsg(player.getName(), message);
			return true;
		}
		return false;
	}

	public int numinvis(){
		if(this.vanishing)
			return this.vanish.invisible.size();
		return 0;
	}

	public void who(CommandSender sender){
		Player[] players=j2.getServer().getOnlinePlayers();
		boolean isAdmin=this.qualified(sender);
		int curlen=0;
		int maxlen=320;
		int playercount=players.length;
		if(!isAdmin)
			playercount-=this.numinvis();
		String msg="Players ("+playercount+"/"+j2.playerLimit+"):";

		for(char ch:msg.toCharArray()){
			curlen+=Chats.characterWidths[(int)ch];
		} //now we have our base length
		int pc=0;

		for(Player p: players){
			boolean invis=j2.minitrue.invisible(p);
			if(!invis||isAdmin){
				String name=p.getName();
				String cname=ChatColor.WHITE+name;
				try{
					cname=j2.users.getUser(name).getColorName();
				}
				catch(Exception e){
					this.j2.users.playerReset(name);
					this.j2.users.addUser(name);
					this.j2.users.processJoin(p);
					cname=ChatColor.GREEN+name;
				}
				if(isAdmin){
					if(j2.hasFlag(p, Flag.TRUSTED)){
						cname=ChatColor.DARK_GREEN+name;
					}
					if(j2.hasFlag(p,Flag.ADMIN)){
						if(invis)
							cname=ChatColor.AQUA+name;
						else
							cname=ChatColor.RED+name;
					}
					if(j2.hasFlag(p, Flag.MUTED)){
						cname=ChatColor.YELLOW+name;
					}
					if(j2.hasFlag(p, Flag.NSA)){
						cname+=ChatColor.DARK_AQUA+"«»";
					}
					if(j2.hasFlag(p,Flag.THOR)){
						cname+=ChatColor.WHITE+"/";
					}
					if(j2.hasFlag(p,Flag.GODMODE)){
						cname+=ChatColor.DARK_RED+"⌂";
					}
					if(j2.hasFlag(p,Flag.TOOLS)){
						cname+=ChatColor.AQUA+"¬";
					}
					if(j2.hasFlag(p, Flag.JAILED)){
						cname+=ChatColor.GRAY+"[ø]";
					}
				}
				cname+=ChatColor.WHITE.toString();
				int thislen=0;
				for(char ch:name.toCharArray()){
					thislen+=Chats.characterWidths[(int)ch];
				}
				if(thislen+1+curlen>maxlen){
					this.send(sender,msg);
					msg=cname;
				}
				else{
					msg+=" "+cname;
				}
				pc++;
			}
		}
		this.send(sender, msg);
	}
	public void send(CommandSender player,String message){
		if(player!=null){
			player.sendMessage(message);
		}
		else{
			this.j2.log(message);
		}
	}
	public boolean qualified(CommandSender sender){
		if(sender!=null&&sender instanceof Player){
			return this.j2.hasFlag((Player)sender, Flag.ADMIN);
		}
		else{
			return true;
		}
	}

	public List<Player> matchPlayer(String name,boolean isAdmin){
		List<Player> players=j2.getServer().matchPlayer(name);
		if(!isAdmin){
			ArrayList<Player> toremove=new ArrayList<Player>();
			for(Player p:players){
				if(p!=null&&this.invisible(p)){
					toremove.add(p);
				}
			}
			players.removeAll(toremove);
		}
		return players;
	}

	public Player[] getOnlinePlayers(){
		Player[] players=j2.getServer().getOnlinePlayers();
		Player[] toreturn=new Player[players.length-this.numinvis()];
		int cur=0;
		for(Player p:players){
			if(!this.invisible(p)){
				toreturn[cur++]=p;
			}
		}
		return toreturn;
	}
}
