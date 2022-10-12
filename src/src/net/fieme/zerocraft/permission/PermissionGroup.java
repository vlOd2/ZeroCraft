package net.fieme.zerocraft.permission;

import java.util.ArrayList;

/**
 * A permission group
 */
public class PermissionGroup {
	public final ArrayList<Permission> permissions = new ArrayList<Permission>();
	public final ArrayList<PermissionUser> members = new ArrayList<PermissionUser>();
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
			if (permission.name == name)
				return permission.allowed;
		}

		return false;
	}
}
