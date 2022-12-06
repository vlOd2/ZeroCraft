package net.fieme.zerocraft;

/**
 * Allowed characters for things like chat messages and usernames
 */
public class AllowedCharacters {
	public static final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,.:-_\\'*!\\\\\\\"#%/()=+?[]{}<>@|$;";

	/**
	 * Checks if the specified character is valid
	 * 
	 * @param character the character
	 * @return true if valid, false if otherwise
	 */
	public static boolean isCharacterValid(char character) {
		if (AllowedCharacters.characters.contains(String.valueOf(character))) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if the specified string is valid
	 * 
	 * @param str the string
	 * @return true if valid, false if otherwise
	 */
	public static boolean isStringValid(String str) {
		for (int strCharIndex = 0; strCharIndex < str.length(); strCharIndex++) {
			char strChar = str.charAt(strCharIndex);
			if (!AllowedCharacters.isCharacterValid(strChar))
				return false;
		}
		
		return true;
	}
}
