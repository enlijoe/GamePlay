package com.jgr.game.vac.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.operations.TimedOperation;
import com.jgr.game.vac.service.WatchDog;
import com.jgr.game.vac.stereotype.InjectDevice;

public class DoStimThreadImpl implements TimedOperation {
	@Autowired private SystemTime systemTime;
	
	boolean cumAllowed;
	
	@Value("${game.numCycles}") int numCycles;
	@Value("${game.allowStimAbort}") boolean allowAbort;
	@Value("${game.nippleOnTime}") long nippleOnTime;
	@Value("${game.vibe2OnTime}") long vibe2OnTime;
	@Value("${game.probeOnTime}") long probeOnTime;
	@Value("${game.vibeOnTime}") long vibeOnTime;
	@Value("${game.timeMutiple}") long timeMutiple;
	@Value("${game.stimRestTime}") long stimRestTime;
	
	@InjectDevice("${deviceUrl.nipplesSwitch}") private OutputDevice nipplesSwitch;
	@InjectDevice("${deviceUrl.vibe2Switch}") private OutputDevice vibe2Switch;
	@InjectDevice("${deviceUrl.probeSwitch}") private OutputDevice probeSwitch;
	@InjectDevice("${deviceUrl.vibeSwitch}") private OutputDevice vibeSwitch;

	private Logger logger = LoggerFactory.getLogger(DoStimThreadImpl.class);

	public boolean run(WatchDog.WatchTimer timer) throws InterruptedException {
		int endCycle = numCycles;

		
		logger.info("Cycling Stim Started");
		int onCycle = 0;
		while(true) {
			// turn on nipples
			logger.info("Stim started");
			try {
				if(changeState(timer, nipplesSwitch, nippleOnTime)) {
					return true;
				}
				if(changeState(timer, vibe2Switch, vibe2OnTime)) {
					return true;
				}
				if(changeState(timer, probeSwitch, probeOnTime)) {
					return true;
				}
				if(changeState(timer, vibeSwitch, vibeOnTime)) {
					return true;
				}
			} finally {
				// turn off everything
				turnOff(nipplesSwitch, nippleOnTime);
				turnOff(probeSwitch, probeOnTime);
				turnOff(vibeSwitch, vibeOnTime);
				turnOff(vibe2Switch, vibe2OnTime);
			}

			onCycle++;

			logger.info("Stim Cycle " + onCycle + " Copmplete");
			if(onCycle >= endCycle) break;
			
			// time to rest
			if(allowAbort) {
				if(systemTime.safeSleep(timer, stimRestTime * timeMutiple)) {
					return true;
				}
			} else {
				systemTime.sleep(timer, stimRestTime * timeMutiple);
			}
		}
		logger.info("Cycling Stim Ended");
		return false;
	}
	
	private void turnOff(OutputDevice device, long onTime) {
		if(onTime != 0 && device != null) device.setOff();
	}

	private boolean changeState(WatchDog.WatchTimer timer, OutputDevice device, long time) throws InterruptedException {
		if(time != 0 && device != null) {
			device.setOn();
			if(allowAbort) {
				if(systemTime.safeSleep(timer, time * timeMutiple)) {
					return true;
				}
			} else {
				systemTime.sleep(timer, time * timeMutiple);
			}
		}
		return false;
	}
	
	@Override
	public long getMaxRunTime() {
		long stimTime = nippleOnTime + vibe2OnTime + probeOnTime + vibeOnTime;
		
		// added two minutes of extra time for error
		return 120000 + timeMutiple * (stimTime*numCycles + stimRestTime*(numCycles-1));
	}
}
