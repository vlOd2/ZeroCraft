package net.fieme.zerocraft.configuration;

import java.util.HashMap;
import java.util.Map;

public class MutedConfig implements Config {
	public static MutedConfig instance;
	public Map<String, String> users = new HashMap<String, String>();
}
