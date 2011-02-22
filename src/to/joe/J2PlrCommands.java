package to.joe;

import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

public class J2PlrCommands extends PlayerListener {
	
	private final J2Plugin j2;

	public J2PlrCommands(J2Plugin instance) {
		j2 = instance;
	}
	
	@Override
	public void onPlayerCommand(PlayerChatEvent event) {
		String[] split = event.getMessage().split(" ");
		Player player = event.getPlayer();

		if(j2.hasFlag(player,Flag.JAILED)){
			if(split[0].equalsIgnoreCase("/confess")){
				j2.users.getOnlineUser(player).dropFlag(Flag.JAILED);
			}
			event.setCancelled(true);
			return;
		}
		
		if (split[0].equalsIgnoreCase("/rules")){
			for(String line : j2.rules){
				player.sendMessage(line);
			}
			event.setCancelled(true);
			return;
		}
		if (split[0].equalsIgnoreCase("/blacklist")){
			for(String line : j2.blacklist){
				player.sendMessage(line);
			}
			event.setCancelled(true);
			return;
		}
		if (split[0].equalsIgnoreCase("/intro")){
			for(String line : j2.intro){
				player.sendMessage(line);
			}
			event.setCancelled(true);
			return;
		}
		if(split[0].equalsIgnoreCase("/protectme") && j2.hasFlag(player, Flag.TRUSTED)){
			String playerName = player.getName().toLowerCase();
			if(j2.tpProtect.getBoolean(playerName,false)){
				j2.tpProtect.setBoolean(playerName, false);
				player.sendMessage(ChatColor.RED + "You are now no longer protected from teleportation");
			}
			else{
				j2.tpProtect.setBoolean(playerName, true);
				player.sendMessage(ChatColor.RED + "You are protected from teleportation");
			}
			event.setCancelled(true);
			return;
		}

		if(split[0].equalsIgnoreCase("/tp") && (j2.hasFlag(player, Flag.FUN))){
			List<Player> inquest = j2.getServer().matchPlayer(split[1]);
			if(inquest.size()==1){
				Player inquestion=inquest.get(0);
				if(!j2.hasFlag(player, Flag.ADMIN) && inquestion!=null && (j2.hasFlag(inquestion, Flag.TRUSTED)) && j2.tpProtect.getBoolean(inquestion.getName().toLowerCase(), false)){
					player.sendMessage(ChatColor.RED + "Cannot teleport to protected player.");
				}
				else if(inquestion.getName().equalsIgnoreCase(player.getName())){
					player.sendMessage(ChatColor.RED+"Can't teleport to yourself");
				}
				else {
					player.teleportTo(inquestion.getLocation());
					j2.log.info("Teleport: " + player.getName() + " teleported to "+inquestion.getName());
				}
			}
			else{
				player.sendMessage(ChatColor.RED+"No such player, or matches multiple");
			}
			event.setCancelled(true);
			return;
		}

		if(split[0].equalsIgnoreCase("/tphere") && j2.hasFlag(player, Flag.ADMIN)){
			List<Player> inquest = j2.getServer().matchPlayer(split[1]);
			if(inquest.size()==1){
				Player inquestion=inquest.get(0);

				if(inquestion.getName().equalsIgnoreCase(player.getName())){
					player.sendMessage(ChatColor.RED+"Can't teleport yourself to yourself. Derp.");
				}
				else {
					player.teleportTo(inquestion.getLocation());
					inquestion.sendMessage("You've been teleported");
					player.sendMessage("Grabbing "+inquestion.getName());
					j2.log.info("Teleport: " + player.getName() + " pulled "+inquestion.getName()+" to self");
				}
			}
			else{
				player.sendMessage(ChatColor.RED+"No such player, or matches multiple");
			}
			event.setCancelled(true);
			return;
		}

		if(split[0].equalsIgnoreCase("/msg")){
			if(split.length<3){
				player.sendMessage(ChatColor.RED+"Correct usage: /msg player message");
				event.setCancelled(true);
				return;
			}
			List<Player> inquest = j2.getServer().matchPlayer(split[1]);
			if(inquest.size()==1){
				Player inquestion=inquest.get(0);
				player.sendMessage("(MSG) <"+player.getName()+"> "+j2.combineSplit(2, split, " "));
				inquestion.sendMessage("(MSG) <"+player.getName()+"> "+j2.combineSplit(2, split, " "));
			}
			else{
				player.sendMessage(ChatColor.RED+"Could not find player");
			}
			event.setCancelled(true);
			return;
		}

		if((split[0].equalsIgnoreCase("/item") || split[0].equalsIgnoreCase("/i")) && j2.hasFlag(player, Flag.FUN)){
			if (split.length < 2) {
				player.sendMessage(ChatColor.RED+"Correct usage is: /i [item] (amount)");
				event.setCancelled(true);
				return;
			}
			int item = 0;
			int amount = 1;
			int dataType = -1;
			try {
				if(split[1].contains(":")) {
					String[] data = split[1].split(":");

					try {
						dataType = Integer.valueOf(data[1]);
					} catch (NumberFormatException e) {
						dataType = -1;
					}

					item = Integer.valueOf(data[0]);
				} else {
					item = Integer.valueOf(split[1]);
				}
				if(split.length>2){
					amount = Integer.valueOf(split[2]);
				}
				else{
					amount = 1;
				}
			} catch(NumberFormatException e) {
				player.sendMessage(ChatColor.RED+"Command fail.");
				return;
			}
			if((new ItemStack(item)).getType() == null || item == 0) {
				player.sendMessage(ChatColor.RED+"Invalid item.");
				event.setCancelled(true);
				return;
			}
			if(dataType != -1) {
				player.getWorld().dropItem(player.getLocation(), new ItemStack(item, amount, ((byte)dataType)));
			} else {
				player.getWorld().dropItem(player.getLocation(), new ItemStack(item, amount));
			}
			player.sendMessage(ChatColor.RED+"Here you go!");
			event.setCancelled(true);
			return;
		}

		if(split[0].equalsIgnoreCase("/time") && j2.hasFlag(player, Flag.ADMIN)){
			if(split.length!=2){
				player.sendMessage(ChatColor.RED+"Usage: /time day|night");
				event.setCancelled(true);
				return;
			}
			long desired;
			if(split[1].equalsIgnoreCase("day")){
				desired=0;
			}
			else if(split[1].equalsIgnoreCase("night")){
				desired=13000;
			}
			else{
				player.sendMessage(ChatColor.RED+"Usage: /time day|night");
				event.setCancelled(true);
				return;
			}

			long curTime=j2.getServer().getWorlds().get(0).getTime();
			long margin = (desired-curTime) % 24000;
			if (margin < 0) {
				margin += 24000;
			}
			j2.getServer().getWorlds().get(0).setTime(curTime+margin);
			player.sendMessage(ChatColor.RED+"Time changed");
			event.setCancelled(true);
			return;
		}

		if(split[0].equalsIgnoreCase("/who") || split[0].equalsIgnoreCase("/playerlist")){
			Player[] players=j2.getServer().getOnlinePlayers();
			String msg="Players ("+players.length+"):";
			for(Player p: players){
				msg+=" "+p.getName();
			}
			player.sendMessage(msg);
			event.setCancelled(true);
			return;
		}

		if(split[0].equalsIgnoreCase("/a") && j2.hasFlag(player, Flag.ADMIN)){
			if(split.length<2){
				event.getPlayer().sendMessage(ChatColor.RED+"Usage: /a Message");
				event.setCancelled(true);
				return;
			}
			String playerName = player.getName();
			String message=j2.combineSplit(1, split, " ");
			j2.getChat().aMsg(playerName,message);
			event.setCancelled(true);
			return;
		}

		if(split[0].equalsIgnoreCase("/report")){
			if(split.length>1){
				String playerName = player.getName();
				String report=j2.combineSplit(1, split, " ");
				String message="Report: <§d"+playerName+"§f>"+report;
				String ircmessage="Report from "+playerName+": "+report;
				j2.getChat().msgByFlag(Flag.ADMIN, message);
				j2.getIRC().ircAdminMsg(ircmessage);
				player.sendMessage(ChatColor.RED+"Report transmitted. Thanks! :)");
			}
			else {
				player.sendMessage(ChatColor.RED+"To report to the admins, say /report MESSAGE");
				player.sendMessage(ChatColor.RED+"Where MESSAGE is what you want to tell them");
			}
			event.setCancelled(true);	
		}
		if(split[0].equalsIgnoreCase("/g") && j2.hasFlag(player, Flag.ADMIN)){
			if(split.length<2){
				event.getPlayer().sendMessage(ChatColor.RED+"Usage: /g Message");
				event.setCancelled(true);
				return;
			}
			String playerName = player.getName();
			String text = "";
			text+=j2.combineSplit(1, split, " ");
			j2.getChat().gMsg(playerName,text);
			event.setCancelled(true);	
			return;
		}
		if(split[0].equalsIgnoreCase("/ban") && j2.hasFlag(player, Flag.ADMIN)){
			if(split.length < 3){
				player.sendMessage(ChatColor.RED+"Usage: /ban playername reason");
				player.sendMessage(ChatColor.RED+"       reason can have spaces in it");
				event.setCancelled(true);
				return;
			}
			String adminName = player.getName();
			j2.getKickBan().callBan(adminName,split);
			event.setCancelled(true);
			return;
		}
		if(split[0].equalsIgnoreCase("/kick") && j2.hasFlag(player, Flag.ADMIN)){
			if(split.length < 3){
				player.sendMessage(ChatColor.RED+"Usage: /kick playername reason");
				event.setCancelled(true);
				return;
			}
			String adminName = player.getName();
			j2.getKickBan().callKick(split[1],adminName,j2.combineSplit(2, split, " "));
			event.setCancelled(true);
			return;
		}
		if(split[0].equalsIgnoreCase("/addban") && j2.hasFlag(player, Flag.ADMIN)){
			if(split.length < 3){
				player.sendMessage(ChatColor.RED+"Usage: /addban playername reason");
				player.sendMessage(ChatColor.RED+"        reason can have spaces in it");
				event.setCancelled(true);
				return;
			}
			String adminName = player.getName();
			j2.getKickBan().callAddBan(adminName,split);
			event.setCancelled(true);
			return;
		}

		if((split[0].equalsIgnoreCase("/unban") || split[0].equalsIgnoreCase("/pardon")) && j2.hasFlag(player, Flag.ADMIN)){
			if(split.length < 2){
				player.sendMessage(ChatColor.RED+"Usage: /unban playername");
				event.setCancelled(true);
				return;
			}
			String name=split[1];
			String adminName=player.getName();
			j2.mysql.unban(name);
			j2.log.log(Level.INFO, "Unbanning " + name + " by " + adminName);
			j2.getChat().msgByFlag(Flag.ADMIN,ChatColor.RED + "Unbanning " + name + " by " + adminName);
			event.setCancelled(true);
			return;
		}


		/*if(split[0].equalsIgnoreCase("/trust") && player.canUseCommand("/trust")){
			if(split.length < 2){
				player.sendMessage(ChatColor.RED+"Usage: /trust playername");
				return true;
			}
			String playername=split[1];
			String adminName=player.getName();
			j2.trust(playername);
			j2.log.log(Level.INFO, "Trusting " + playername + " by " + adminName);
            j2.msgByCmd("/trust",ChatColor.RED + "Trusting " + playername + " by " + adminName);
			return true;
		}*/

		if(split[0].equalsIgnoreCase("/getgroup") && j2.hasFlag(player, Flag.ADMIN)){
			if(split.length==1){
				player.sendMessage("/getgroup playername");
				event.setCancelled(true);
				return;
			}
			List<Player> match = j2.getServer().matchPlayer(split[1]);
			if(match.size()!=1 || match.get(0)==null){
				player.sendMessage("Player not found");
				event.setCancelled(true);
				return;
			}
			
			String message="Player "+match.get(0).getName()+": ";
			for(Flag f: j2.users.getAllFlags(match.get(0))){
				message+=f.getDescription()+", ";
			}
			player.sendMessage(message);
			event.setCancelled(true);
			return;
		}

		if (split[0].equalsIgnoreCase("/me") && split.length>1)
		{
			String message = "";
			message+=j2.combineSplit(1, split, " ");
			j2.getChat().addChat(player.getName(), message);
			j2.getIRC().ircMsg("* "+ player.getName()+" "+message);
			//don't cancel this after reading it. 
			//TODO: /ignore code will also be here
		}

		if (split[0].equalsIgnoreCase("/forcekick") && j2.hasFlag(player, Flag.ADMIN)){
			if(split.length==1){
				player.sendMessage(ChatColor.RED+"Usage: /forcekick playername");
				player.sendMessage(ChatColor.RED+"       Requires full name");
				event.setCancelled(true);
				return;
			}
			String name=split[1];
			String reason="";
			String admin=player.getName();
			if(split.length>2)
				reason=j2.combineSplit(2, split, " ");
			j2.getKickBan().forceKick(name,reason);
			j2.log.log(Level.INFO, "Kicking " + name + " by " + admin + ": " + reason);
			j2.getChat().msgByFlag(Flag.ADMIN,ChatColor.RED + "Kicking " + name + " by " + admin + ": " + reason);
			j2.getChat().msgByFlagless(Flag.ADMIN,ChatColor.RED + name+" kicked ("+reason+")");
			event.setCancelled(true);
			return;
		}
		/*if(split[0].equalsIgnoreCase("/invasion") && j2.getPerm().isAtOrAbove(2, player)){
			if(split.length==1){
				player.sendMessage(ChatColor.RED + "Usage: /homeinvasion playername");
				return true;
			}
			Warp home = etc.getDataSource().getHome(split[1]);
			if (home != null) {
				player.teleportTo(home.Location);
				player.sendMessage(ChatColor.RED + "Wheeee!");
			} else {
				player.sendMessage(ChatColor.RED + "That player home does not exist");
			}
			return true;
		}*/
		if(split[0].equalsIgnoreCase("/ircrefresh") && j2.hasFlag(player, Flag.SRSTAFF)){
			j2.getIRC().loadIRCAdmins();
			player.sendMessage(ChatColor.RED+"IRC admins reloaded");
			event.setCancelled(true);
			return;
		}

		if(split[0].equalsIgnoreCase("/j2reload") && j2.hasFlag(player, Flag.SRSTAFF)){
			j2.loadData();
			j2.getChat().msgByFlag(Flag.SRSTAFF, "j2 data reloaded by "+player.getName());
			j2.log.info("j2 data reloaded by "+player.getName());
			event.setCancelled(true);
			return;
		}

		if(split[0].equalsIgnoreCase("/maintenance") && j2.hasFlag(player, Flag.SRSTAFF)){
			if(!j2.maintenance){
				j2.log.info(player.getName()+" has turned on maintenance mode");
				j2.maintenance=true;
				for (Player p : j2.getServer().getOnlinePlayers()) {
					if (p != null && !j2.hasFlag(player, Flag.ADMIN)) {
						p.sendMessage("Server entering maintenance mode");
						p.kickPlayer("Server entering maintenance mode");
					}
				}

			}
			else{
				j2.log.info(player.getName()+" has turned off maintenance mode");
				j2.maintenance=false;
			}
			event.setCancelled(true);
			return;
		}

		if(split[0].equalsIgnoreCase("/1x1") && j2.hasFlag(player, Flag.ADMIN)){
			player.sendMessage("Next block you break (not by stick), everything above it goes byebye");
			j2.OneByOne=player;
			event.setCancelled(true);
			return;
		}

	}
}
