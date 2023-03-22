package me.gorgeousone.superpaintball.util;

public abstract class StringUtil {

	private StringUtil() {}

	public static String pad(int n) {
		return pad(n, ' ');
	}
	
	public static String pad(int n, char c) {
		return new String(new char[n]).replace('\0', c);
	}
}
