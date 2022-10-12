package net.fieme.zerocraft;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import net.fieme.zerocraft.logging.Logging;
import net.fieme.zerocraft.networking.ClientNetworkIdentifier;

public class Utils {
	public static final double NANOSECONDS_TO_SECONDS = 0.000000001; 
	
    /**
     * NOTE: Not intended to be used function
     * 
     * @param arg0 unknown
     * @return unknown
     */
	public static ClientNetworkIdentifier getIdentifierFromClientSocket(Socket arg0) {
		String clientIP = arg0.getInetAddress().getHostAddress();
		int clientPort = arg0.getPort();
		ClientNetworkIdentifier clientIdentifier = 
				ClientNetworkIdentifier.getIdentifier(clientIP, clientPort);
		return clientIdentifier;
	}
	
	/** 
	 * Converts a byte array to a hex string<br>
	 * From https://stackoverflow.com/a/9855338
	 * 
	 * @param bytes the byte array
	 * @return the hex string or "DEADBEEF" on invalid input
	 */
	public static String bytesToHex(byte[] bytes) {
		if (bytes == null || bytes.length < 1) return "DEADBEEF";
		
		char[] hexArray = "0123456789ABCDEF".toCharArray();
	    char[] hexChars = new char[bytes.length * 2];
	    
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    
	    return new String(hexChars);
	}
	
	/**
	 * Gets an exception stacktrace as a string
	 * 
	 * @param ex the exception
	 * @return the exception's stacktrace or null
	 */
	public static String getExceptionStackTraceAsStr(Exception ex) {
		if (ex == null) return null;
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		ex.printStackTrace(printWriter);
		return stringWriter.toString();
	}
	
	/**
	 * Checks if the specified string is a numeric value
	 * 
	 * @param str the string
	 * @param checkForDouble check if the string is a valid double value
	 * @return the check status
	 */
	public static boolean isNumeric(String str, boolean checkForDouble) {
	    if (str == null || 
	    	(!checkForDouble && str.contains(".")) || 
	    	(checkForDouble && !str.contains(".")))
	        return false;
	    
	    try {
	    	if (!checkForDouble)
	    		Integer.valueOf(str);
	    	else
	    		Double.valueOf(str);
	    } catch (NumberFormatException ex) {
	        return false;
	    }
	    
	    return true;
	}
	
	/**
	 * Checks if the specified string is a boolean value
	 * 
	 * @param str the string
	 * @return the check status
	 */
	public static boolean isBoolean(String str) {
	    if (str == null) 
	        return false;   
	    else if (str.equalsIgnoreCase("true") || 
	    	str.equalsIgnoreCase("false"))
	    	return true;
	    else
	    	return false;
	}
	
	/**
	 * Replaces the last match in the specified string
	 * 
	 * @param str the string
	 * @param match the match
	 * @param replaceWith what to the replace the match with
	 * @return the replaced string or the string if no match
	 */
	public static String replaceLast(String str, String match, String replaceWith) {
		return new StringBuilder(
				new StringBuilder(str)
					.reverse()
					.toString()
					.replaceFirst(match, replaceWith))
				.reverse()
				.toString();
	}
	
	/** 
	 * Splits a string by spaces, ignoring quotes<br>
	 * From <a href="https://stackoverflow.com/a/366532">https://stackoverflow.com/a/366532</a>
	 * 
	 * @param str the string to split
	 * @return the result or empty array
	 */
	public static String[] splitBySpace(String str) {
		try {
			List<String> matchList = new ArrayList<String>();
			Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
			Matcher regexMatcher = regex.matcher(str);
			
			while (regexMatcher.find()) {
			    if (regexMatcher.group(1) != null) {
			        // Add double-quoted string without the quotes
			        matchList.add(regexMatcher.group(1));
			    } else if (regexMatcher.group(2) != null) {
			        // Add single-quoted string without the quotes
			        matchList.add(regexMatcher.group(2));
			    } else {
			        // Add unquoted word
			        matchList.add(regexMatcher.group());
			    }
			}	
			
			return matchList.toArray(new String[0]);
		} catch (Exception ex) {
			ex.printStackTrace();
			return new String[0];
		}
	}
	
	/**
	 * Compresses the specified data to GZip
	 * 
	 * @param data the data
	 * @return the compressed data or empty array
	 */
	public static byte[] compressToGZip(byte[] data) {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream dataOutputStream;
			
			dataOutputStream = new DataOutputStream(new GZIPOutputStream(byteArrayOutputStream));
			dataOutputStream.writeInt(data.length);
			dataOutputStream.write(data);
			dataOutputStream.close();
			
			return byteArrayOutputStream.toByteArray();
		} catch (Exception ex) {
			Logging.logError("Unable to compress data: " + 
					getExceptionStackTraceAsStr(ex));
			return new byte[0];
		}
	}
	
	/**
	 * Gets a salted MD5 hash from the specified string
	 * 
	 * @param salt the salt to use
	 * @param content the string to hash
	 * @return the MD5 hash or empty on error
	 */
	public static String getMD5HashFromStr(String salt, String content) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update((salt + content).getBytes());
			return (new BigInteger(1, messageDigest.digest())).toString(16);	
		} catch (Exception ex) {
			return "";
		}
	}
	
	/**
	 * Splits the specified string into chunks<br>
	 * From <a href="https://stackoverflow.com/a/3760193">https://stackoverflow.com/a/3760193</a>
	 * 
	 * @param str the string to split
	 * @param chunkSize the chunk size
	 * @return the chunks
	 */
	public static String[] splitStringIntoChunks(String str, int chunkSize) {
	    List<String> chunks = new ArrayList<String>((str.length() + chunkSize - 1) / chunkSize);

	    for (int chunkIndex = 0; chunkIndex < str.length(); chunkIndex += chunkSize) {
	        chunks.add(str.substring(chunkIndex, Math.min(str.length(), chunkIndex + chunkSize)));
	    }
	    
	    return chunks.toArray(new String[0]);
	}
}
