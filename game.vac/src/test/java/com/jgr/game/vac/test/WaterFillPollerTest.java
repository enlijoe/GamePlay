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
import com.jgr.game.vac.interfaces.WaterFillPoller;
import com.jgr.game.vac.poller.WaterFillPollerImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WaterFillPollerTest.TestConfig.class, BaseTestConfig.class}, loader = AnnotationConfigContextLoader.class)
@TestPropertySource("/application.properties")
public class WaterFillPollerTest {
	@Value("${smartthings.bedroom.light}") private String bedRoomLight;
	@Value("${smartthings.switch.nipples}") private String eStimSwitch;
	@Value ("${game.waterFillTime}") private int waterFillTime;

	@Autowired WaterFillPollerImpl waterFillPoller;
	@Autowired SystemTimeTestMock systemTime;	
	@Autowired SmartThingsTestMock smartThings;
	
	@Configuration
	static class TestConfig {
		@Bean
		public WaterFillPoller waterFillPoller() {
			return new WaterFillPollerImpl();
		}
	}
	
	@Before
	public void setup() {
		smartThings.reset();
		systemTime.reset();
	}
	
	@Test
	public void initalManualStateTest() throws Exception {
		smartThings.setDeviceValue(bedRoomLight, 0);
		smartThings.setDeviceValue(eStimSwitch, 0);
		
		waterFillPoller.setManualControl(true);
		waterFillPoller.init();
		assertFalse(waterFillPoller.doCheck());
	}

	@Test
	public void manualCompleteTest() throws Exception {
		smartThings.setDeviceValue(bedRoomLight, 0);
		smartThings.setDeviceValue(eStimSwitch, 100);
		
		
		waterFillPoller.setManualControl(true);
		waterFillPoller.init();
		assertTrue(waterFillPoller.doCheck());
	}

	@Test
	public void autoCompleteTest() throws Exception {
		smartThings.setDeviceValue(bedRoomLight, 0);
		smartThings.setDeviceValue(eStimSwitch, 0);
		
		
		waterFillPoller.setManualControl(false);
		systemTime.setCurrentTime(0);
		waterFillPoller.init();
		systemTime.setCurrentTime(waterFillTime*1000+1);
		assertTrue(waterFillPoller.doCheck());
	}

	@Test
	public void initalAutoStateTest() throws Exception {
		smartThings.setDeviceValue(bedRoomLight, 0);
		smartThings.setDeviceValue(eStimSwitch, 0);
		
		waterFillPoller.setManualControl(false);
		systemTime.setCurrentTime(0);
		waterFillPoller.init();
		systemTime.setCurrentTime(waterFillTime*1000);
		assertFalse(waterFillPoller.doCheck());
	}

	
	
	@Test
	public void lightOnAndContinue() throws Exception {
		smartThings.setDeviceValue(bedRoomLight, 74);
		smartThings.setDeviceValue(eStimSwitch, 0);
		
		waterFillPoller.setManualControl(false);
		waterFillPoller.init();
		assertTrue(waterFillPoller.doCheck());

		waterFillPoller.setManualControl(true);
		waterFillPoller.init();
		assertTrue(waterFillPoller.doCheck());
	}
	
	@Test
	public void abortTest() throws Exception {
		smartThings.setDeviceValue(bedRoomLight, 75);
		smartThings.setDeviceValue(eStimSwitch, 0);
		
		waterFillPoller.setManualControl(false);
		waterFillPoller.init();
		try {
			assertFalse(waterFillPoller.doCheck());
			fail("Should receive AbortException");
		} catch(AbortException ex) {
			// this is good
		}

		waterFillPoller.setManualControl(true);
		waterFillPoller.init();
		try {
			assertFalse(waterFillPoller.doCheck());
			fail("Should receive AbortException");
		} catch(AbortException ex) {
			// this is good
		}
	}

}
