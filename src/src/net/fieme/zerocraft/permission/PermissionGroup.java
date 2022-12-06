package net.fieme.zerocraft.permission;

import java.util.ArrayList;

import net.fieme.zerocraft.configuration.Config;

/**
 * A permission group
 */
public class PermissionGroup implements Config {
	public final ArrayList<Permission> permissions = new ArrayList<Permission>();
	public String name;
	public String chatPrefix;
	public String chatSuffix;
	
	/**
	 * Checks if the group has a permission and if it is allowed
	 * 
	 * @param name the permission name
	 * @see Permission#allowed
	 * @return true if the permission is present and is granted, false if otherwise
	 */
	public boolean hasPermission(String name) {
		for (Permission permission : this.permissions.toArray(new Permission[0])) {
			if (permission.name.equals(name)) {
				return permission.allowed;
			} else if (permission.name.equals("*")) {
				return true;
			}
		}

		return false;
	}
}
