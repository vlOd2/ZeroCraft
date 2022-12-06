package net.fieme.zerocraft.permission;

/**
 * A permission for permission related stuff
 */
public class Permission {
	/**
	 * The name of this permission<br>
	 * Example: myprogram.mypermission
	 */
	public String name;
	/**
	 * Value determining if this permission is allowed
	 */
	public boolean allowed;
	
	/**
	 * A permission for permission related stuff
	 */
	public Permission() {
	}
	
	/**
	 * A permission for permission related stuff
	 * 
	 * @param name the name
	 * @param allowed allowed state
	 */
	public Permission(String name, boolean allowed) {
		this.name = name;
		this.allowed = allowed;
	}
}
