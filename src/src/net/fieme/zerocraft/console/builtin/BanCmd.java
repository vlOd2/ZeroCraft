package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.InlineForloop;
import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.BannedConfig;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.EntityPlayer;

public class BanCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "ban";
	}

	@Override
	public String getDescription() {
		return "Bans the specified player/ip";
	}

	@Override
	public String getRequiredPermission() {
		return "zerocraft.admin.ban";
	}
	
	@Override
	public String getUsage() {
		return "ban <player/ip> <reason> [forcebanip (true to use)]";
	}

	@Override
	public int getMinArgsCount() {
		return 2;
	}

	@Override
	public int getMaxArgsCount() {
		return 3;
	}

	@Override
	public void execute(ConsoleCaller caller, String[] args) throws Exception {
		String name = args[0];
		String reason = args[1];
		boolean forceBanIP = args.length >= 3 ? args[2].equalsIgnoreCase("true") : false;
		
		caller.sendMessage("&aBanning &e" + name + "&b...");
		
		if (Utils.isIPv4Address(name)) {
			BannedConfig.instance.ipaddresses.put(name, reason);
			ZeroCraft.instance.saveConfigs();
			
			ZeroCraft.instance.foreachPlayer(new InlineForloop<EntityPlayer>() {
				@Override
				public void onEntry(EntityPlayer entry) {
					if (entry.client.identifier.networkAddress.equals(name)) {
						entry.kick("Banned: " + reason);
					}
				}
			});
		} else if (forceBanIP) {
			EntityPlayer player = ZeroCraft.instance.getPlayerByName(name);
			
			if (player == null) {
				caller.sendMessage("&cUnable to find the player &e" + name + " &c!");
				return;
			} else {
				String playerIP = player.client.identifier.networkAddress;
				BannedConfig.instance.ipaddresses.put(playerIP, reason);
				ZeroCraft.instance.saveConfigs();
				
				ZeroCraft.instance.foreachPlayer(new InlineForloop<EntityPlayer>() {
					@Override
					public void onEntry(EntityPlayer entry) {
						if (entry.client.identifier.networkAddress.equals(playerIP)) {
							entry.kick("Banned: " + reason);
						}
					}
				});
				
				caller.sendMessage("&aBanned &e" + playerIP);
				return;
			}
		} else {
			EntityPlayer player = ZeroCraft.instance.getPlayerByName(name);
			BannedConfig.instance.users.put(name, reason);
			ZeroCraft.instance.saveConfigs();
			
			if (player == null) {
				caller.sendMessage("&cThe player &e" + name + " &cis currently not online!");
			} else {
				player.kick("Banned: " + reason);
			}
		}
		
		caller.sendMessage("&aBanned &e" + name);
	}
}
