package com.iyan.support.utils;

import java.util.regex.Pattern;

/**
 * zhangxianjiang
 * 变量相互进行转换工具
 */
public class VarUtil {
	private static Pattern pattern_isInt = Pattern.compile("^(\\-*\\d+)$");
	private static Pattern pattern_isFloat = Pattern.compile("^(\\-*(\\d+\\.{1}\\d+|\\d+))$");
	private static Pattern p_clearPath = Pattern.compile("[\\\\/]+");
	/**
	 * 字符串转换为整数
	 * @param str 字符串
	 * @return int(如果不是整数返回0)
	 */
	public static int intval(String str, int def) {
		if (!isInt(str))
			return def;
		try {
			return new Integer(str);
		} catch (Exception e) {
			return def;
		}
	}

	public static int intval(String str) {
		return intval(str, 0);
	}

	/**
	 * 字符串转换为long
	 * @param str 字符串
	 * @return long (如果不是long 返回0)
	 */
	public static long longval(String str, int def) {
		return longval(str, Long.valueOf(def));
	}
	public static long longval(String str, long def) {
		if (!isInt(str))
			return def;
		try {
			return new Long(str);
		} catch (Exception e) {
			return def;
		}
	}
	public static long longval(String str) {
		return longval(str, 0);
	}

	/**
	 * 字符串转换为double
	 * @param str  字符串
	 * @return long (如果不是double返回0)
	 */
	public static double doubleval(String str, float def) {
		return doubleval(str, Double.valueOf(def));
	}
	public static double doubleval(String str, double def) {
		if (!isFloat(str))
			return def;
		try {
			return new Double(str);
		} catch (Exception e) {
			return def;
		}
	}
	public static double doubleval(String str) {
		return doubleval(str, 0f);
	}

	/**
	 * 字符串转换为float
	 * @param str  字符串
	 * @return long (如果不是float返回0)
	 */
	public static float floatval(String str, float def) {
		if (!isFloat(str))
			return def;
		try {
			return new Float(str);
		} catch (Exception e) {
			return def;
		}
	}
	public static float floatval(String str) {
		return floatval(str, 0f);
	}

	/**
	 * 字符串处理，如果为null返回空字符串
	 * @param str  字符串
	 * @return 字符串
	 */
	public static String strval(String str) {
		if (str == null)
			return "";
		return str;
	}
	/**
	 * 清除多余路径
	 * @param str
	 * @return
	 */

	public static String clearPath(String str) {
		if(str == null)return str;
		return p_clearPath.matcher(str).replaceAll("/");
	}
	
	/**
	 * 返回字符串的文件名，过滤目录字符
	 * @param str 字符串
	 * @return 字符串
	 */
	public static String basename(String str) {
		int pos = str.lastIndexOf('/');
		int pos2 = str.lastIndexOf('\\');
		if (str.length() == 0)
			return "";
		if (pos > pos2) {
			return str.substring(pos + 1);
		} else {
			return str.substring(pos2 + 1);
		}
	}

	/**
	 * 返回字符串的目录，过滤文件名字符
	 * @param str 字符串
	 * @return 字符串
	 */
	public static String dirname(String str) {
		int pos = str.lastIndexOf('/');
		int pos2 = str.lastIndexOf('\\');
		if (str.length() == 0)
			return "";
		if (pos > pos2) {
			return str.substring(0, pos + 1);
		} else {
			return str.substring(0, pos2 + 1);
		}
	}
	/**
	 * 判断字符串是否为整数
	 * @param str 字符串
	 * @return true/false
	 */
	public static boolean isInt(String str) {
		return str != null && pattern_isInt.matcher(str).matches();
	}
	/**
	 * 判断字符串是否为浮点类型
	 * @param str 字符串
	 * @return true/false
	 */
	public static boolean isFloat(String str) {
		return str != null && pattern_isFloat.matcher(str).matches();
	}
}
