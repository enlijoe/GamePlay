package com.jgr.game.vac.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jgr.game.vac.interfaces.Esp32;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.SystemTime;

@Configuration
public class BaseTestConfig {
	@Bean
	public SmartThings smartThings() {
		return new SmartThingsTestMock();
	}
	
	@Bean
	public SystemTime systemTime() {
		return new SystemTimeTestMock();
	}

	@Bean 
	public Esp32 esp32() {
		return new Esp32TestMock();
	}
}
