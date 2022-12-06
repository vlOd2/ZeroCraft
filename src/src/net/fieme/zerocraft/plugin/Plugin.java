package net.fieme.zerocraft.plugin;

public interface Plugin {
	/**
	 * Gets the name
	 * 
	 * @return the name
	 */
	public String getName();
	
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
