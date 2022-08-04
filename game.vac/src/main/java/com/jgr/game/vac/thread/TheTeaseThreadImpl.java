package com.jgr.game.vac.thread;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.DoStimThread;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceNames;
import com.jgr.game.vac.service.PropertyService;
import com.jgr.game.vac.service.WatchDog;

public class TheTeaseThreadImpl implements DoStimThread {
	@Autowired private DeviceNames deviceNames;
	@Autowired private PropertyService propertyService;
	@Autowired private SmartThings smartThings;
	@Autowired private SystemTime systemTime;
	
	private Logger logger = LoggerFactory.getLogger(TheTeaseThreadImpl.class);

	
	/*
	 *  Part 1 - 
	 *  		Each eStim is on a random time from 5 seconds to 300 seconds 
	 *  		Vibe on a random time of 2-10 seconds
	 *  		All have a randome off time from 30-300 seconds off
	 *  Part 2 - 
	 *  	starting with short on times long off times moving to short off times to long on times
	 *  	eStim start at 5 seconds increase by .2t each time
	 *  	vibe start at 1 seconds increase by 1+.1t each time
	 *  	Off Time is 300 Seconds decrease by .1t each time never less then 30
	 *  	After each session 
	 *  
	 */

	Random rnd = new Random(System.currentTimeMillis());
	
	@Override
	public void run(WatchDog.WatchTimer timer) {
		try {
			long now = systemTime.currentTime();
			long partEndTime = now + 60 * 60 * propertyService.getTimeMutiple();
			boolean onStim1Rest = true;
			boolean onStim2Rest = true;
			boolean onVibeRest = true;
			long stim1Time = nextTime(30, 150);
			long stim2Time = nextTime(30, 150);
			long vibeTime = nextTime(30, 150);
			long nextStatusCheck = 0;
			
			logger.info("Part 1 for " + (partEndTime-now) / propertyService.getTimeMutiple());
			logger.info("Nip " + !onStim1Rest + " for " + (stim1Time-now) / propertyService.getTimeMutiple());
			logger.info("Probe " + !onStim2Rest + " for " + (stim2Time-now) / propertyService.getTimeMutiple());
			logger.info("Vibe " + !onVibeRest + " for " + (vibeTime-now) / propertyService.getTimeMutiple());
			

			smartThings.setDeviceState(deviceNames.getNipplesSwitch(), false);
			smartThings.setDeviceState(deviceNames.getProbeSwitch(), false);
			smartThings.setDeviceState(deviceNames.getVibeSwitch(), false);
			
			
			while(partEndTime > systemTime.currentTime()) {
				now = systemTime.currentTime();
				
				if(stim1Time < now) {
					if(onStim1Rest) {
						stim1Time = nextTime(5, 150);
					} else {
						stim1Time = nextTime(30, 150);
					}
					if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getNipplesSwitch(), onStim1Rest);
					logger.info("Nip " + onStim1Rest + " for " + (stim1Time-now) / propertyService.getTimeMutiple());
					onStim1Rest = !onStim1Rest;
					
				}
				
				if(stim2Time < now) {
					if(onStim2Rest) {
						stim2Time = nextTime(5, 150);
					} else {
						stim2Time = nextTime(30, 150);
					}
					if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getProbeSwitch(), onStim2Rest);
					logger.info("Probe " + onStim2Rest + " for " + (stim2Time-now) / propertyService.getTimeMutiple());
					onStim2Rest = !onStim2Rest;
				}
				
				if(vibeTime < now) {
					if(onVibeRest) {
						vibeTime = nextTime(2, 10);
					} else {
						vibeTime = nextTime(30, 150);
					}
					if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getVibeSwitch(), onVibeRest);
					logger.info("Vibe " + onVibeRest + " for " + (vibeTime-now) / propertyService.getTimeMutiple());
					onVibeRest = !onVibeRest;
				}

				if(nextStatusCheck < now) {
					nextStatusCheck = now + 5 * propertyService.getTimeMutiple();
					if(smartThings.isOn(smartThings.getSwitchState(deviceNames.getStatusLight()))) {
						logger.info("Abort Received");
						return;
					}
				}

				systemTime.sleep(timer, propertyService.getTimeMutiple());
			}
			
			logger.info("Part 2");

			stim1Time = 5 * propertyService.getTimeMutiple();
			stim2Time = 5 * propertyService.getTimeMutiple();
			vibeTime = 1 * propertyService.getTimeMutiple();
			long restTime = 300 * propertyService.getTimeMutiple();
			
			while(smartThings.isOff(smartThings.getSwitchState(deviceNames.getStatusLight()))) {
				logger.info("Nip " + stim1Time + " Probe " + stim2Time + " Vibe " + vibeTime + " rest " + restTime);
				if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getNipplesSwitch(), true);
				if(systemTime.safeSleep(timer, stim1Time)) return;

				if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getProbeSwitch(), true);
				if(systemTime.safeSleep(timer, stim2Time)) return;
				
				if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getVibeSwitch(), true);
				if(systemTime.safeSleep(timer, vibeTime)) return;

				if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getNipplesSwitch(), false);
				if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getProbeSwitch(), false);
				if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getVibeSwitch(), false);
				if(systemTime.safeSleep(timer, restTime)) return;


				stim1Time = stim1Time + (long) ((float)stim1Time*0.2);			
				stim2Time = stim2Time + (long) ((float)stim2Time*0.2);
				vibeTime =  (long) ((float)stim1Time*0.1) + stim1Time + (long) ((float)vibeTime*0.1);
				restTime = restTime - (long) ((float)restTime*0.2);

				if(stim1Time >= 120*propertyService.getTimeMutiple()) {
					stim1Time = 300*propertyService.getTimeMutiple();
				}

				if(stim2Time >= 120*propertyService.getTimeMutiple()) {
					stim2Time = 300*propertyService.getTimeMutiple();
				}

				if(vibeTime >= 45*propertyService.getTimeMutiple()) {
					vibeTime = 45*propertyService.getTimeMutiple();
				}

				if(restTime <= 15*propertyService.getTimeMutiple()) {
					restTime = 15*propertyService.getTimeMutiple();
				}
			}
				
			logger.info("Stim all done");
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
		
	}
	
	private long nextTime(long min, long max) {
		long length = (long) (min + (rnd.nextFloat() * (max-min)));
		return length * propertyService.getTimeMutiple() + systemTime.currentTime();
	}
}
