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

import com.jgr.game.vac.poller.VaccumPollerImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {VaccumPollerTest.TestConfig.class, BaseTestConfig.class}, loader = AnnotationConfigContextLoader.class)
@TestPropertySource("/application.properties")
public class VaccumPollerTest {
	@Value("${smartthings.switch.pump}") private String pumpSwitch;
	@Value ("${game.timeMutiple}") private long timeMutiple;
	@Value ("${game.pumpRestTime}") private int pumpRestTime;

	@Autowired VaccumPollerImpl vaccumPoller;
	@Autowired SmartThingsTestMock smartThings;
	@Autowired SystemTimeTestMock systemTime;	

	@Configuration
	public static class TestConfig {
		@Bean
		VaccumPollerImpl vaccumPoller() {
			return new VaccumPollerImpl();
		}
	}

	@Before
	public void setup() {
		smartThings.reset();
		systemTime.reset();
		//vaccumPoller.reset(pumpRestTime);
	}
	
	@Test
	public void initalState() {
		smartThings.setDeviceValue(pumpSwitch, 0);
		vaccumPoller.init();
		assertFalse(vaccumPoller.doCheck());
	}
	
	@Test
	public void normalCycle() {
		smartThings.setDeviceValue(pumpSwitch, 0);
		vaccumPoller.init();
		systemTime.setCurrentTime(pumpRestTime * timeMutiple + 1);
		assertTrue(vaccumPoller.doCheck());
	}
	
	@Test
	public void pumpManualOn() {
		smartThings.setDeviceValue(pumpSwitch, 0);
		vaccumPoller.init();
		smartThings.setDeviceValue(pumpSwitch, 100);
		assertTrue(vaccumPoller.doCheck());
		//assertEquals(pumpRestTime, vaccumPoller.getPumpRestTime());
		
	}
	
	@Test
	public void adjustRestTime() {
		smartThings.setDeviceValue(pumpSwitch, 0);
		vaccumPoller.init();
		smartThings.setDeviceValue(pumpSwitch, 100);
		assertTrue(vaccumPoller.doCheck());
		smartThings.setDeviceValue(pumpSwitch, 0);
		vaccumPoller.init();
		smartThings.setDeviceValue(pumpSwitch, 100);
		systemTime.setCurrentTime(pumpRestTime * timeMutiple / 2);
		assertTrue(vaccumPoller.doCheck());
		//assertEquals(pumpRestTime/2 + 1, vaccumPoller.getPumpRestTime());
	}
}
