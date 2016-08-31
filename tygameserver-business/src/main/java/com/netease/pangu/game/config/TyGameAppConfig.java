package com.netease.pangu.game.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:/bean/tygameserver-beans.xml")
public class TyGameAppConfig {
	public static String APP_NAME = "appName"; 
}
