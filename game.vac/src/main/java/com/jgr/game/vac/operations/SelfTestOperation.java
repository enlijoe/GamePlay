package com.jgr.game.vac.operations;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;
import com.jgr.game.vac.service.WatchDog.WatchTimer;

public class SelfTestOperation implements Operation {
	private Logger logger = LoggerFactory.getLogger(SelfTestOperation.class);

	@Autowired private DeviceMapperService deviceMapperService;
	@Autowired private SystemTime systemTime;

	@Value("${game.timeMutiple}") long timeMutiple;
	
	@Value("${deviceUrl.nipplesSwitch}") private String nipplesSwitchUrl;
	@Value("${deviceUrl.probeSwitch}") private String probeSwitchUrl;
	@Value("${deviceUrl.vibeSwitch}") private String vibeSwitchUrl;
	
	private OutputDevice nipplesSwitch;
	private OutputDevice probeSwitch;
	private OutputDevice vibeSwitch;
	
	@PostConstruct
	public void afterPropsSet() {
		nipplesSwitch = deviceMapperService.getDevice(new DeviceUrl(nipplesSwitchUrl));
		probeSwitch = deviceMapperService.getDevice(new DeviceUrl(probeSwitchUrl));
		vibeSwitch = deviceMapperService.getDevice(new DeviceUrl(vibeSwitchUrl));
	}
	
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
