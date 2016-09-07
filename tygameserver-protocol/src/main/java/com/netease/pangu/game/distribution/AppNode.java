package com.netease.pangu.game.distribution;

import java.util.Map;

public class AppNode {
	private String ip;
	private int port;
	private String name;
	private int count;
	private Map<String, String> sys;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public Map<String, String> getSys() {
		return sys;
	}
	public void setSys(Map<String, String> sys) {
		this.sys = sys;
	}
}
