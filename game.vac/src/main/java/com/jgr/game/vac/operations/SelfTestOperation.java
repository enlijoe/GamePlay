package com.jgr.game.vac.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.WatchDog.WatchTimer;
import com.jgr.game.vac.stereotype.InjectDevice;

public class SelfTestOperation implements Operation {
	private Logger logger = LoggerFactory.getLogger(SelfTestOperation.class);

	@Autowired private SystemTime systemTime;

	@Value("${game.timeMutiple}") long timeMutiple;
	
	@InjectDevice("${deviceUrl.nipplesSwitch}") private OutputDevice nipplesSwitch;
	@InjectDevice("${deviceUrl.probeSwitch}") private OutputDevice probeSwitch;
	@InjectDevice("${deviceUrl.vibeSwitch}") private OutputDevice vibeSwitch;
	
	@Override
	public boolean run(WatchTimer timer) throws InterruptedException {
		logger.info("Running self test");
		
		// run self test 2 secs each device
		nipplesSwitch.setOn();
		if(systemTime.safeSleep(timer, 2*timeMutiple)) return true;
		nipplesSwitch.setOff();

		probeSwitch.setOn();
		if(systemTime.safeSleep(timer, 2*timeMutiple)) return true;
		probeSwitch.setOff();

		vibeSwitch.setOn();
		if(systemTime.safeSleep(timer, 2*timeMutiple)) return true;
		vibeSwitch.setOff();
		
		return false;
	}
	
}
