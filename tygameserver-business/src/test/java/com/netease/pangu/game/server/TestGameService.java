package com.netease.pangu.game.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-tygameapp-service.xml")
public class TestGameService extends AbstractJUnit4SpringContextTests {
	@Test
	public void Test(){
		
	}
}
