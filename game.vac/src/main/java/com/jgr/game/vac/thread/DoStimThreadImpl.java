package com.jgr.game.vac.thread;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.DoStimThread;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;
import com.jgr.game.vac.service.WatchDog;

public class DoStimThreadImpl implements DoStimThread {
	@Autowired private SystemTime systemTime;
	@Autowired private DeviceMapperService deviceMapperService;
	
	boolean cumAllowed;
	
	@Value("${game.numCycles}") int numCycles;
	@Value("${game.nippleOnTime}") long nippleOnTime;
	@Value("${game.vibe2OnTime}") long vibe2OnTime;
	@Value("${game.probeOnTime}") long probeOnTime;
	@Value("${game.vibeOnTime}") long vibeOnTime;
	@Value("${game.timeMutiple}") long timeMutiple;
	@Value("${game.stimRestTime}") long stimRestTime;
	
	@Value("${deviceUrl.nipplesSwitch}") String nipplesSwitchUrl;
	@Value("${deviceUrl.vibe2Switch}") String vibe2SwitchUrl;
	@Value("${deviceUrl.probeSwitch}") String probeSwitchUrl;
	@Value("${deviceUrl.vibeSwitch}") String vibeSwitchUrl;

	private OutputDevice nipplesSwitch;
	private OutputDevice vibe2Switch;
	private OutputDevice probeSwitch;
	private OutputDevice vibeSwitch;

	private Logger logger = LoggerFactory.getLogger(DoStimThreadImpl.class);

	@PostConstruct
	public void afterPropsSet() {
		nipplesSwitch = deviceMapperService.getDevice(new DeviceUrl(nipplesSwitchUrl));
		vibe2Switch = deviceMapperService.getDevice(new DeviceUrl(vibe2SwitchUrl));
		probeSwitch = deviceMapperService.getDevice(new DeviceUrl(probeSwitchUrl));
		vibeSwitch = deviceMapperService.getDevice(new DeviceUrl(vibeSwitchUrl));
	}
	
	public void run(WatchDog.WatchTimer timer) throws InterruptedException {
		int endCycle = numCycles;

		
		logger.info("Cycling Stim Started");
		int onCycle = 0;
		while(true) {
			// turn on nipples
			logger.info("Stim started");

			changeState(timer, nipplesSwitch, nippleOnTime);
			changeState(timer, vibe2Switch, vibe2OnTime);
			changeState(timer, probeSwitch, probeOnTime);
			changeState(timer, vibeSwitch, vibeOnTime);
			
			// turn off everything
			turnOff(nipplesSwitch, nippleOnTime);
			turnOff(probeSwitch, probeOnTime);
			turnOff(vibeSwitch, vibeOnTime);
			turnOff(vibe2Switch, vibe2OnTime);

			onCycle++;

			logger.info("Stim Cycle " + onCycle + " Copmplete");
			if(onCycle >= endCycle) break;
			
			// time to rest
			systemTime.sleep(timer, stimRestTime * timeMutiple);
		}
		logger.info("Cycling Stim Ended");
	}
	
	private void turnOff(OutputDevice device, long onTime) {
		if(onTime != 0 && device != null) device.setOff();
	}

	private void changeState(WatchDog.WatchTimer timer, OutputDevice device, long time) throws InterruptedException {
		if(time != 0 && device != null) {
			device.setOn();
			systemTime.sleep(timer, time * timeMutiple);
		}
	}
}
