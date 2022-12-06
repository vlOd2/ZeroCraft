package net.fieme.zerocraft.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.fieme.zerocraft.permission.Permission;
import net.fieme.zerocraft.permission.PermissionGroup;
import net.fieme.zerocraft.permission.PermissionUser;

public class PermissionsConfig implements Config {
	public static PermissionsConfig instance;
	public String defaultGroup = "";
	public Map<String, PermissionUser> users = new HashMap<String, PermissionUser>();
	public ArrayList<PermissionGroup> groups = new ArrayList<PermissionGroup>();
	
	public PermissionsConfig() {
		PermissionGroup permissionGroupGuest = new PermissionGroup();
		permissionGroupGuest.name = "guest";
		permissionGroupGuest.chatPrefix = "&7Guest";
		permissionGroupGuest.permissions.add(new Permission("zerocraft.move", true));
		permissionGroupGuest.permissions.add(new Permission("zerocraft.chat", true));
		permissionGroupGuest.permissions.add(new Permission("zerocraft.chat.command", true));
		permissionGroupGuest.permissions.add(new Permission("zerocraft.world.break", true));
		permissionGroupGuest.permissions.add(new Permission("zerocraft.world.place", true));
		PermissionGroup permissionGroupModerator = new PermissionGroup();
		permissionGroupModerator.name = "moderator";
		permissionGroupModerator.chatPrefix = "&aMod";
		permissionGroupModerator.permissions.add(new Permission("zerocraft.move", true));
		permissionGroupModerator.permissions.add(new Permission("zerocraft.chat", true));
		permissionGroupModerator.permissions.add(new Permission("zerocraft.chat.command", true));
		permissionGroupModerator.permissions.add(new Permission("zerocraft.world.break", true));
		permissionGroupModerator.permissions.add(new Permission("zerocraft.world.break.special.water", true));
		permissionGroupModerator.permissions.add(new Permission("zerocraft.world.break.special.lava", true));
		permissionGroupModerator.permissions.add(new Permission("zerocraft.world.place", true));
		permissionGroupModerator.permissions.add(new Permission("zerocraft.world.place.special.water", true));
		permissionGroupModerator.permissions.add(new Permission("zerocraft.world.place.special.lava", true));
		permissionGroupModerator.permissions.add(new Permission("zerocraft.admin.kick", true));
		permissionGroupModerator.permissions.add(new Permission("zerocraft.admin.mute", true));
		permissionGroupModerator.permissions.add(new Permission("zerocraft.admin.unmute", true));
		PermissionGroup permissionGroupAdministrator = new PermissionGroup();
		permissionGroupAdministrator.name = "administrator";
		permissionGroupAdministrator.chatPrefix = "&cAdmin";
		permissionGroupAdministrator.permissions.add(new Permission("*", true));
		
		this.groups.add(permissionGroupGuest);
		this.groups.add(permissionGroupModerator);
		this.groups.add(permissionGroupAdministrator);

		this.defaultGroup = "guest";
	}
		
	public PermissionGroup getGroup(String name) {
		for (PermissionGroup group : this.groups) {
			if (group.name.equals(name)) {
				return group;
			}
		}
		
		return null;
	}
	
	public PermissionGroup createGroupWithDefaultPerms(String name) {
		PermissionGroup permissionGroup = new PermissionGroup();
		permissionGroup.name = name;
		permissionGroup.chatPrefix = "&f" + String.valueOf(name.charAt(0)).toUpperCase() + 
				name.substring(1);
		permissionGroup.permissions.add(new Permission("zerocraft.move", true));
		permissionGroup.permissions.add(new Permission("zerocraft.chat", true));
		permissionGroup.permissions.add(new Permission("zerocraft.chat.command", true));
		permissionGroup.permissions.add(new Permission("zerocraft.world.break", true));
		permissionGroup.permissions.add(new Permission("zerocraft.world.place", true));
		return permissionGroup;
	}
}
