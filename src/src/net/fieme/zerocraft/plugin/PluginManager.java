package net.fieme.zerocraft.plugin;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Properties;

import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.logging.Logging;

public class PluginManager {
	public static final String PLUGINS_DIR = "plugins";
	private final ArrayList<Plugin> loadedPlugins = new ArrayList<Plugin>();
	
	/**
	 * Loads a plugin
	 * 
	 * @param plugin the plugin to load
	 */
	public void load(Plugin plugin) {
		loadedPlugins.add(plugin);
		
		try {
			plugin.onLoad();
		} catch (Exception ex) {
			Logging.logError("Failed to call plugin callback (onLoad()): " + Utils.getExceptionStackTraceAsStr(ex));
		}
		
		Logging.logInfo("Loaded plugin \"" + plugin.getName() + 
				"\" v" + String.format("%.1f", plugin.getVersion()) + 
				" by \"" + plugin.getAuthor() + "\"");
	}
	
	/**
	 * Unloads a plugin
	 * 
	 * @param plugin the plugin to unload
	 */
	public void unload(Plugin plugin) {
		loadedPlugins.remove(plugin);

		try {
			plugin.onUnload();
		} catch (Exception ex) {
			Logging.logError("Failed to call plugin callback (onUnload()): " + Utils.getExceptionStackTraceAsStr(ex));
		}
		
		Logging.logInfo("Unloaded plugin \"" + plugin.getName() + 
				"\" v" + String.format("%.1f", plugin.getVersion()) + 
				" by \"" + plugin.getAuthor() + "\"");
	}
	
	/**
	 * Unloads all plugins
	 */
	public void unloadAll() {
		for (Plugin plugin : this.loadedPlugins.toArray(new Plugin[0])) {
			this.unload(plugin);
		}
	}
	
	/**
	 * Gets a plugin from a plugin JAR file
	 * 
	 * @param file the plugin file
	 * @return the plugin or null
	 */
	public Plugin getPluginFromFile(File file) {
		try {
			ClassLoader classLoader = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() });
			InputStream pluginPropertiesStream = classLoader.getResourceAsStream("plugin.properties");

			Properties pluginProperties = new Properties();
			if (pluginPropertiesStream != null)
				pluginProperties.load(pluginPropertiesStream);
			String mainClass = pluginProperties.getProperty("main", null);
			
			if (pluginPropertiesStream == null) {
				classLoader = null;
				Logging.logWarn("Unable to find plugin properties for the plugin \"" + file.getName() + "\"!");
				return null;
			} else if (mainClass == null) {
				classLoader = null;
				Logging.logWarn("Unable to find the main class for the plugin \"" + file.getName() + "\"!");
				return null;
			}
			
			return (Plugin) classLoader.loadClass(mainClass).newInstance();
		} catch (Exception ex) {
			Logging.logError("Unable to get \"" + 
					file.getName() + "\" as a plugin: " + 
					Utils.getExceptionStackTraceAsStr(ex));
			return null;
		}
	}
	
	/**
	 * Loads all plugins in the plugins directory
	 */
	public void loadPluginsInDir() {
		File pluginsDir = new File(PLUGINS_DIR); 
		if (!pluginsDir.isDirectory()) pluginsDir.delete();
		if (!pluginsDir.exists()) pluginsDir.mkdir();
		
		Logging.logVerbose("FIXME: Plugin system initialized, re-write needed");
		Logging.logWarn(Utils.getConsoleSeparatorWithHeader("IMPORTANT"));
		Logging.logWarn("ZeroCraft plugin system initialized!");
		Logging.logWarn("This system has not been updated in a long time and needs to be re-written!");
		Logging.logWarn("You will NOT RECEIVE support for any issues that occur due to this system!");
		Logging.logWarn(Utils.getConsoleSeparatorWithHeader("IMPORTANT"));
		
		for (File file : pluginsDir.listFiles()) {
			Plugin plugin = this.getPluginFromFile(file);
			if (plugin != null) {
				this.load(plugin);
			}
		}
	}
}
