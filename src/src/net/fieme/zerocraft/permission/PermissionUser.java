package net.fieme.zerocraft.permission;

import java.util.ArrayList;

public class PermissionUser {
	public final ArrayList<Permission> permissions = new ArrayList<Permission>();
	public PermissionGroup group;
	public String chatPrefix;
	public String chatSuffix;

	/**
	 * Changes the group of this user<br>
	 * If the new group is null, no group is assigned<br>
	 * Changing the group with this function rather then direct 
	 * {@link PermissionUser#group} modification is better as this function
	 * removes this user from the group's members and adds the user to the new group's members
	 * 
	 * @param permissionGroup the new group to change to (or null)
	 */
	public void changeGroup(PermissionGroup permissionGroup) {
		if (this.group != null) {
			this.group.members.remove(this);
			this.group = null;
		}
		
		if (permissionGroup != null) {
			this.group = permissionGroup;
			permissionGroup.members.add(this);	
		}
	}
	
	/**
	 * Checks if the user has a permission and if it is allowed
	 * 
	 * @param name the permission name
	 * @param checkOnlySelf checks only self permissions and ignores group permissions
	 * @return true if the permission is present and is granted, false if otherwise
	 */
	public boolean hasPermission(String name, boolean checkOnlySelf) {
		for (Permission permission : this.permissions.toArray(new Permission[0])) {
			if (permission.name == name)
				return permission.allowed;
		}
		
		if (this.group != null && !checkOnlySelf) {
			for (Permission permission : this.group.permissions.toArray(new Permission[0])) {
				if (permission.name == name)
					return permission.allowed;
			}	
		}
		
		return false;
	}
	
	/**
	 * Gets the usable chat prefix<br>
	 * This function uses the own chat prefix, 
	 * if that is null or empty then the group prefix is used
	 * If there is no group then the own chat prefix is used anyway
	 * 
	 * @return the chat prefix or empty string
	 */
	public String getChatPrefix() {
		if (this.group == null) return this.chatPrefix == null ? "" : this.chatPrefix;
		return this.chatPrefix != null && !this.chatPrefix.isEmpty() ? 
				this.chatPrefix : 
					this.group.chatPrefix;
	}
	
	/**
	 * Gets the usable chat suffix<br>
	 * This function uses the own chat suffix, 
	 * if that is null or empty then the group suffix is used<br>
	 * If there is no group then the own chat suffix is used anyway
	 * 
	 * @return the chat suffix or empty string
	 */
	public String getChatSuffix() {
		if (this.group == null) return this.chatSuffix == null ? "" : this.chatSuffix;
		return this.chatSuffix != null && !this.chatSuffix.isEmpty() ? 
				this.chatSuffix : 
					this.group.chatSuffix;
	}
}
