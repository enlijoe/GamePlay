package com.jgr.game.vac.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

import com.jgr.game.vac.exceptions.AbortException;
import com.jgr.game.vac.poller.SealCompletePollerImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SealCompletePollerTest.TestConfig.class, BaseTestConfig.class}, loader = AnnotationConfigContextLoader.class)
@TestPropertySource("/application.properties")
public class SealCompletePollerTest {
	@Value("${smartthings.switch.pump}") private String pumpSwitch;
	@Value("${smartthings.bedroom.light}") private String bedRoomLight;
	
	@Autowired SealCompletePollerImpl sealCompletePoller;
	@Autowired SmartThingsTestMock smartThings;
	@Autowired Esp32TestMock esp32;
	
	@Configuration
	public static class TestConfig {
		@Bean
		public SealCompletePollerImpl sealCompletePoller() {
			return new SealCompletePollerImpl();
		}
	}
	
	@Before
	public void setup() {
		esp32.reset();
		smartThings.reset();
	}
	
	@Test
	public void testInitalState() throws Exception {
		smartThings.setDeviceValue(pumpSwitch, 0);
		smartThings.setDeviceValue(bedRoomLight, 0);
		sealCompletePoller.init();
		assertTrue(esp32.readValveStatus());
		// assertTrue(smartThings.getSwitchState(pumpSwitch) != 0);
		assertFalse(sealCompletePoller.doCheck());
	}
	
	@Test
	public void testPumpOff() throws Exception {
		smartThings.setDeviceValue(pumpSwitch, 0);
		smartThings.setDeviceValue(bedRoomLight, 0);
		sealCompletePoller.init();
		assertTrue(esp32.readValveStatus());
		// assertTrue(smartThings.getSwitchState(pumpSwitch) != 0);
		smartThings.setDeviceValue(pumpSwitch, 0);
		assertTrue(sealCompletePoller.doCheck());
	}
	
	@Test
	public void testRoomLightOn() throws Exception {
		smartThings.setDeviceValue(pumpSwitch, 0);
		smartThings.setDeviceValue(bedRoomLight, 74);
		sealCompletePoller.init();
		// assertTrue(smartThings.getSwitchState(pumpSwitch) != 0);
		assertTrue(esp32.readValveStatus());
		assertTrue(sealCompletePoller.doCheck());
	}
	
	@Test
	public void testAbort() throws Exception {
		smartThings.setDeviceValue(pumpSwitch, 0);
		smartThings.setDeviceValue(bedRoomLight, 75);
		sealCompletePoller.init();
		// assertTrue(smartThings.getSwitchState(pumpSwitch) != 0);
		assertTrue(esp32.readValveStatus());
		
		try {
			sealCompletePoller.doCheck();
			fail();
		} catch(AbortException ex) {
			
		}
	}
	
}
