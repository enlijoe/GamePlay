package com.jgr.game.vac.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.jgr.game.vac.poller.LightOffPollerImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {LightOffPollerTest.TestConfig.class, BaseTestConfig.class}, loader = AnnotationConfigContextLoader.class)
@TestPropertySource("/application.properties")
public class LightOffPollerTest {
	@Value("${smartthings.bedroom.light}") private String bedRoomLight;

	@Autowired SmartThingsTestMock smartThings;
	@Autowired LightOffPollerImpl lightOffPoller;
	
	@Configuration
	public static class TestConfig {
		
		@Bean
		public LightOffPollerImpl lightOffPoller() {
			return new LightOffPollerImpl();
		}
	}

	@Before
	public void setup() {
		smartThings.reset();
	}
	
	@Test
	public void lightOff() throws Exception {
		smartThings.setDeviceValue(bedRoomLight, 0);
		lightOffPoller.init();
		assertTrue(lightOffPoller.doCheck());
	}

	@Test
	public void lightOn() throws Exception {
		smartThings.setDeviceValue(bedRoomLight, 100);
		lightOffPoller.init();
		assertFalse(lightOffPoller.doCheck());
	}
}
