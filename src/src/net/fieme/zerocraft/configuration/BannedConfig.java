package net.fieme.zerocraft.configuration;

import java.util.HashMap;
import java.util.Map;

public class BannedConfig implements Config {
	public static BannedConfig instance;
	public Map<String, String> users = new HashMap<String, String>();
	public Map<String, String> ipaddresses = new HashMap<String, String>();
}
