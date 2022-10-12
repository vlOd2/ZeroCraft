package net.fieme.zerocraft.plugins;

public interface Plugin {
	/**
	 * Gets the name
	 * 
	 * @return the name
	 */
	public String getName();
	/**
	 * Gets the description
	 * 
	 * @deprecated function deprecated because plugin descriptions aren't used
	 * @implNote default implementation: returns null
	 * @return the description
	 */
	@Deprecated
	public default String getDescription() { return null; }
	/**
	 * Gets the author
	 * 
	 * @return the author
	 */
	public String getAuthor();
	/**
	 * Gets the version of this plugin
	 * 
	 * @return the version
	 */
	public double getVersion();
	/**
	 * Function called when the plugin is loaded
	 */
	public void onLoad();
	/**
	 * Function called when the plugin is unloaded
	 */
	public void onUnload();
}
