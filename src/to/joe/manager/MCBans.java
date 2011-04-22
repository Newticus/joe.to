package to.joe.manager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

import to.joe.J2Plugin;
import to.joe.util.Flag;

public class MCBans {
	private J2Plugin j2;
	private String version="1.3.3.7";
	public Hashtable<String, JSONObject> player_jsonobj = new Hashtable<String, JSONObject>();
	public MCBans(J2Plugin j2){
		this.j2=j2;
	}

	public void processJoin(String name){
		String[] mcbans=j2.mcbans.checkBansOnline(name);
		Double rep=Double.valueOf(mcbans[1]);
		if(rep<10.0){
			j2.chat.msgByFlag(Flag.ADMIN, ChatColor.LIGHT_PURPLE+"User"+ChatColor.WHITE+name+ChatColor.LIGHT_PURPLE+" has a lowered mcbans reputation of "+rep+"/10");
			j2.chat.msgByFlag(Flag.ADMIN, ChatColor.LIGHT_PURPLE+"To see the bans: /lookup "+name);
			j2.irc.ircAdminMsg("User "+name+" has a lowered reputation of "+rep+"/10 on mcbans");
		}
	}

	public void lookup(String PlayerName, Player player){
		if (j2.hasFlag(player,Flag.ADMIN)) {
			HashMap<String,String> url_items = new HashMap<String,String>();
			url_items.put("player", PlayerName);
			url_items.put("exec", "lookup_user");
			JSONObject result = hdl_jobj(url_items);
			try {
				player.sendMessage(PlayerName + " has " + result.getString("ban_num") + " ban(s) .:. " + result.getString("ban_rep") + "/10 Reputation");

				for (int v = 0; v < result.getJSONArray("ban_reasons_local").length(); ++v) {
					player.sendMessage("[Local] " + ChatColor.AQUA + result.getJSONArray("ban_reasons_local").getString(v));
				}
				for (int v = 0; v < result.getJSONArray("ban_reasons_global").length(); ++v) {
					player.sendMessage("[Global] " + ChatColor.DARK_RED + result.getJSONArray("ban_reasons_global").getString(v));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else {
			player.sendMessage(ChatColor.RED + " Insufficient permissions!");
		}
	}
	public String[] checkBansOnline(String player) {
		HashMap<String, String> url_items=new HashMap<String, String>();
		player = player.toLowerCase();
		String[] toReturn=new String[2];
		url_items.put("player", player);
		url_items.put("version", version);
		url_items.put("exec", "user_connect");
		JSONObject result = hdl_jobj(url_items);
		player_jsonobj.put(player, result);
		try {
			toReturn[0]=result.getString("ban_status");
			toReturn[1]=result.getString("reputation");
		} catch (JSONException e) {
			toReturn[0]="";
			toReturn[1]="";
		}
		return toReturn;
	}
	private JSONObject hdl_jobj(HashMap<String, String> items) {
		String url_req = urlparse(items);
		String json_text = request_from_api(url_req);
		return get_data(json_text);
	}
	private String urlparse(HashMap<String, String> items) {
		String data = "";
		try {
			for (Map.Entry<String,String> entry : items.entrySet()) {
				String key = (String)entry.getKey();
				String val = (String)entry.getValue();
				if (data.equals(""))
					data = URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(val, "UTF-8");
				else
					data = data + "&" + URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(val, "UTF-8");
			}
		}
		catch (UnsupportedEncodingException e) {
		}
		return data;
	}
	private String request_from_api(String data) {
		try {
			URL url = new URL("http://72.10.39.172/" + j2.mcbansapi);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(8000);
			conn.setReadTimeout(15000);
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();
			StringBuilder buf = new StringBuilder();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null)
			{
				buf.append(line);
			}
			String result = buf.toString();
			wr.close();
			rd.close();
			return result;
		} catch (Exception e) {
			System.out.println("mcbans error");
		}return "";
	}
	private JSONObject get_data(String json_text) {
		try {
			JSONObject json = new JSONObject(json_text);
			return json;
		} catch (JSONException e) {
			j2.log.info("MCBANS: Retrieval of data failed.");
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, String> hdl_com(HashMap<String, String> items) {
		HashMap<String, String> out = new HashMap<String, String>();
		String url_req = urlparse(items);
		String json_text = request_from_api(url_req);
		JSONObject output = get_data(json_text);
		if (output != null)
		{
			Iterator i = output.keys();
			if (i != null) {
				while (i.hasNext())
				{
					String next = (String)i.next();
					try {
						out.put(next, output.getString(next));
					} catch (JSONException e) {
						System.out.println("mcbans error");
					}
				}
			}
		}
		return out;
	}

	public void processUnban(String PlayerName){
		this.unban(PlayerName);
	}

	private boolean unban(String PlayerName){
		HashMap<String,String> url_items = new HashMap<String,String>();
		url_items.put("player", PlayerName);
		url_items.put("exec", "unban_user");
		HashMap<String,String> result = hdl_com(url_items);
		if ((result.get("result")).equalsIgnoreCase("y")){
			j2.log.info("[mcbans] Unbanned "+PlayerName);
			return true;
		}
		j2.log.info("[mcbans] Failed to unban "+PlayerName);
		return false;
	}

	public void processBan(String PlayerName, String Sender, String Reason){
		String Type="l";
		String lreason=Reason.toLowerCase();
		if((lreason.contains("grief")||lreason.contains("hack"))
				&&!(lreason.contains("fuck")||lreason.contains("shit")||lreason.contains("bitch")
						||lreason.contains("ray"))){
			Type="g";
		}
		this.ban(PlayerName,Sender,Reason,Type);
	}

	private boolean ban(String PlayerName, String Sender, String Reason, String Type) {
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put("player", PlayerName);
		url_items.put("admin", Sender);
		url_items.put("reason", Reason);
		String ip = j2.mysql.IPGetLast(PlayerName);
		url_items.put("playerip", ip);
		url_items.put("duration", "0");
		if (Type.equalsIgnoreCase("g"))
			url_items.put("exec", "ban_user");
		else {
			url_items.put("exec", "ban_local_user");
		}
		HashMap<String, String> result = hdl_com(url_items);
		if (((String)result.get("result")).equalsIgnoreCase("y")) {
			j2.log.info("[mcbans] Added "+PlayerName);
		}
		else if (((String)result.get("result")).equalsIgnoreCase("a")) {
			j2.log.info("[mcbans] Player "+PlayerName+" already on list");
		} else if (((String)result.get("result")).equalsIgnoreCase("n")) {
			j2.log.info("[mcbans] Could not add "+PlayerName);
		}
		return false;
	}
}