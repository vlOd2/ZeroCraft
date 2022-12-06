package net.fieme.zerocraft;

import java.awt.Color;
import java.util.ArrayList;

/**
 * Utilities for working with color codes
 */
public class ColorUtil {
	/**
	 * Parses the color codes in the specified string 
	 * 
	 * @param str the string
	 * @param colorCodeSeparator the color code separator
	 * @return the parsed color codes
	 */
	@SuppressWarnings("unchecked")
	public static Tuple<Character, String>[] parseColorCodes(String str,
			Character colorCodeSeparator) {
	    ArrayList<Tuple<Character, String>> colorCodes = new ArrayList<Tuple<Character, String>>();
	    
	    str = "&f" + str;
	    for (int charIndex = 0; charIndex < str.length(); charIndex++) {
	        if (str.charAt(charIndex) == colorCodeSeparator && charIndex + 1 < str.length()) {
	            char colorCode = str.charAt(charIndex + 1);

	            if (str.substring(charIndex).substring(2).split("&").length < 1) continue;
	            String colorCodeStr = str.substring(charIndex).substring(2).split("&")[0];
	            
	            if ("0123456789abcdef".contains(String.valueOf(colorCode))) {
	                colorCodes.add(new Tuple<Character, String>(colorCode, 
	                		colorCodeStr));
	            } else {
	                colorCodes.add(new Tuple<Character, String>('f', 
	                		colorCodeStr));
	            }
	        }
	    }

	    return colorCodes.toArray(new Tuple[0]);
	}
	
	/**
	 * Cleans all color codes in the specified string<br>
	 * This function is meant for making sure that all color codes are valid
	 * and do not cause any given client to crash
	 * 
	 * @param str the string
	 * @param colorCodeSeparator the color code separator
	 * @return the cleaned string
	 */
	public static String cleanColorCodes(String str, char colorCodeSeparator) {
		Tuple<Character, String>[] colorCodes = parseColorCodes(str, colorCodeSeparator);
	    String finalStr = "";

	    for (int i = 0; i < colorCodes.length; i++) {
	    	Tuple<Character, String> colorCode = colorCodes[i];
	    	if (i + 1 >= colorCodes.length && colorCode.item2.trim().isEmpty()) continue;
	    	finalStr += "&" + colorCode.item1 + colorCode.item2;
	    }
	    finalStr = finalStr.replaceFirst("&f", "");
	    
	    return finalStr;
	}
	
	/**
	 * Strips the color codes from the specified string
	 * 
	 * @param str the string
	 * @param colorCodeSeparator the color code separator
	 * @return the stripped string
	 */
	public static String stripColorCodes(String str, char colorCodeSeparator) {
		Tuple<Character, String>[] colorCodes = parseColorCodes(str, colorCodeSeparator);
	    String clearedString = "";

	    for (Tuple<Character, String> colorCode : colorCodes) {
	        clearedString += colorCode.item2;
	    }

	    return clearedString;
	}

	/**
	 * Gets a java.awt.Color instance from the specified color code
	 * 
	 * @param colorCode the color code
	 * @return the java.awt.Color instance
	 */
	public static Color getColorFromColorCode(char colorCode) {
	    switch (colorCode) {
	        case '0':
	            return new Color(0, 0, 0);
	        case '1':
	            return new Color(0, 0, 191);
	        case '2':
	            return new Color(0, 191, 0);
	        case '3':
	            return new Color(0, 191, 191);
	        case '4':
	            return new Color(191, 0, 0);
	        case '5':
	            return new Color(191, 0, 191);
	        case '6':
	            return new Color(191, 191, 0);
	        case '7':
	            return new Color(191, 191, 191);
	        case '8':
	            return new Color(64, 64, 64);
	        case '9':
	            return new Color(64, 64, 255);
	        case 'a':
	            return new Color(64, 255, 64);
	        case 'b':
	            return new Color(64, 255, 255);
	        case 'c':
	            return new Color(255, 64, 64);
	        case 'd':
	            return new Color(255, 64, 255);
	        case 'e':
	            return new Color(255, 255, 64);
	        case 'f':
	            return new Color(255, 255, 255);
	        default:
	            return new Color(255, 255, 255);
	    }
	}
}
