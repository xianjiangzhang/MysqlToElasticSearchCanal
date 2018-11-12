package com.iyan.support.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
* @ClassName: ReadMysqlToESResource
* @Description: 读取配置信息
* @author zhangxj
* @date 2018年11月12日 下午5:04:03
* @version V3.0.1
 */
public class ReadMysqlToESResource {
	protected final static Logger logger = LoggerFactory.getLogger(ReadMysqlToESResource.class);
	// 配置信息
	public Properties ht = new Properties();
	public static ReadMysqlToESResource reson = new ReadMysqlToESResource();
	/**
	 * 读取配置文件，存放到Hashtable中
	 */
	private ReadMysqlToESResource() {
		long t = System.currentTimeMillis();
		logger.debug("----加载配置信息.---");
		try {
			InputStream in = this.getClass().getResourceAsStream("/conf/iyan-mysql-es.properties"); 
			try {
				ht.load(in);
			} catch (FileNotFoundException ex) {
				logger.error("读取配置文件时没有找到：" + ex.getMessage());
				ex.printStackTrace();
			} catch (IOException ex) {
				logger.error("读取配置文件时错误：" + ex.getMessage());
				ex.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("----重新加载配置信息结束:" + (System.currentTimeMillis() - t));
	}
	/**
	 * 读取配置信息
	 * @param name 信息名称
	 * @return 配置信息
	 */
	public static String get(String name) {
		if (reson.ht == null ) {
			reson = new ReadMysqlToESResource();
		}
		String value = reson.ht.getProperty(name);
		if (value != null) {
			value = value.trim();
		}
		return value;
	}

	/**
	 * 读取配置信息
	 * @param name  信息名称
	 * @param defaultValue  为空时的默认值
	 * @return 配置信息
	 */
	public static String get(String name, String defaultValue) {
		String value = get(name);
		if (value == null || value.length() == 0) {
			value = defaultValue;
		}
		return value;
	}
	
	
	public int getInt(String name) {
		return VarUtil.intval(get(name));
	}
	public static int getInt(String name, int defaultValue) {
		String value = get(name);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return VarUtil.intval(value);
	}
	public long getLong(String name) {
		return VarUtil.longval(get(name));
	}
	public static long getLong(String name, long defaultValue) {
		String value = get(name);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return VarUtil.longval(value);
	}
	
	public float getFloat(String name) {
		return VarUtil.floatval(get(name));
	}
	
	public float getFloat(String name, float defaultValue) {
		String value = get(name);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return VarUtil.floatval(value);
	}
	
	public double getDouble(String name) {
		return VarUtil.doubleval(get(name));
	}
	public double getDouble(String name, double defaultValue) {
		String value = get(name);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return VarUtil.doubleval(value);
	}
	
	public boolean getBoolean(String name) {
		String value = get(name);
		return value != null && (value.toLowerCase().equals("true") || value.equals("1"));
	}
	public boolean getBoolean(String name, boolean defaultValue) {
		String value = get(name);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}
		return value.toLowerCase().equals("true") || value.equals("1");
	}
	public static void main(String[] args){
		String ss = ReadMysqlToESResource.get("w2cx.mysql_to_es.esname", "http://172.16.12.177:9980/upload");
		String ss1 = ReadMysqlToESResource.get("w2cx.mysql_to_es.esname", "http://172.16.12.177:9980/upload");
		String ss2 = ReadMysqlToESResource.get("w2cx.mysql_to_es.esname", "http://172.16.12.177:9980/upload");
		String ss3 = ReadMysqlToESResource.get("w2cx.mysql_to_es.esname", "http://172.16.12.177:9980/upload");
		System.out.println(ss);
		System.out.println(ss1);
		System.out.println(ss2);
		System.out.println(ss3);
	}
}