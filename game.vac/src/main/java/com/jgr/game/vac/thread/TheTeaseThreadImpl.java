package com.jgr.game.vac.thread;

import java.util.Random;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.DoStimThread;
import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;
import com.jgr.game.vac.service.WatchDog;

public class TheTeaseThreadImpl implements DoStimThread {
	private Logger logger = LoggerFactory.getLogger(TheTeaseThreadImpl.class);

	@Autowired private SystemTime systemTime;
	@Autowired private DeviceMapperService deviceMapperService;
	
	@Value("${game.timeMutiple}") private long timeMutiple;
	@Value("${game.simulate}") private boolean simulate;
	
	@Value("${deviceUrl.status}") private String statusDeviceUrl;
	@Value("${deviceUrl.nipplesSwitch}") private String nipplesSwitchUrl;
	@Value("${deviceUrl.probeSwitch}") private String probeSwitchUrl;
	@Value("${deviceUrl.vibeSwitch}") private String vibeSwitchUrl;
	
	private InputDevice statusDevice;
	private OutputDevice nipplesSwitch;
	private OutputDevice probeSwitch;
	private OutputDevice vibeSwitch;
	
	
	
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
	
	@PostConstruct
	public void afterPropsSet() {
		statusDevice = deviceMapperService.getDevice(new DeviceUrl(statusDeviceUrl));
		nipplesSwitch = deviceMapperService.getDevice(new DeviceUrl(nipplesSwitchUrl));
		probeSwitch = deviceMapperService.getDevice(new DeviceUrl(probeSwitchUrl));
		vibeSwitch = deviceMapperService.getDevice(new DeviceUrl(vibeSwitchUrl));
	}
	
	@Override
	public void run(WatchDog.WatchTimer timer) {
		try {
			long now = systemTime.currentTime();
			long partEndTime = now + 60 * 60 * timeMutiple;
			boolean onStim1Rest = true;
			boolean onStim2Rest = true;
			boolean onVibeRest = true;
			long stim1Time = nextTime(30, 150);
			long stim2Time = nextTime(30, 150);
			long vibeTime = nextTime(30, 150);
			long nextStatusCheck = 0;
			
			logger.info("Part 1 for " + (partEndTime-now) / timeMutiple);
			logger.info("Nip " + !onStim1Rest + " for " + (stim1Time-now) / timeMutiple);
			logger.info("Probe " + !onStim2Rest + " for " + (stim2Time-now) / timeMutiple);
			logger.info("Vibe " + !onVibeRest + " for " + (vibeTime-now) / timeMutiple);
			

			nipplesSwitch.setOff();
			probeSwitch.setOff();
			vibeSwitch.setOff();
			
			while(partEndTime > systemTime.currentTime()) {
				now = systemTime.currentTime();
				
				if(stim1Time < now) {
					if(onStim1Rest) {
						stim1Time = nextTime(5, 150);
					} else {
						stim1Time = nextTime(30, 150);
					}
					if(!simulate) if(onStim1Rest) {
						nipplesSwitch.setOn();
					} else {
						nipplesSwitch.setOff();
					}
					logger.info("Nip " + onStim1Rest + " for " + (stim1Time-now) / timeMutiple);
					onStim1Rest = !onStim1Rest;
					
				}
				
				if(stim2Time < now) {
					if(onStim2Rest) {
						stim2Time = nextTime(5, 150);
					} else {
						stim2Time = nextTime(30, 150);
					}
					if(!simulate) if(onStim2Rest) {
						probeSwitch.setOn();
					} else {
						probeSwitch.setOff();
					}
					logger.info("Probe " + onStim2Rest + " for " + (stim2Time-now) / timeMutiple);
					onStim2Rest = !onStim2Rest;
				}
				
				if(vibeTime < now) {
					if(onVibeRest) {
						vibeTime = nextTime(2, 10);
					} else {
						vibeTime = nextTime(30, 150);
					}
					if(!simulate) if(onVibeRest) {
						vibeSwitch.setOn();
					} else {
						vibeSwitch.setOff();
					}
					logger.info("Vibe " + onVibeRest + " for " + (vibeTime-now) / timeMutiple);
					onVibeRest = !onVibeRest;
				}

				if(nextStatusCheck < now) {
					nextStatusCheck = now + 5 * timeMutiple;
					if(statusDevice.isOn()) {
						logger.info("Abort Received");
						return;
					}
				}

				systemTime.sleep(timer, timeMutiple);
			}
			
			logger.info("Part 2");

			stim1Time = 5 * timeMutiple;
			stim2Time = 5 * timeMutiple;
			vibeTime = 1 * timeMutiple;
			long restTime = 300 * timeMutiple;
			
			while(statusDevice.isOff()) {
				logger.info("Nip " + stim1Time + " Probe " + stim2Time + " Vibe " + vibeTime + " rest " + restTime);
				if(!simulate) nipplesSwitch.setOn();
				if(systemTime.safeSleep(timer, stim1Time)) return;

				if(!simulate) probeSwitch.setOn();
				if(systemTime.safeSleep(timer, stim2Time)) return;
				
				if(!simulate) vibeSwitch.setOn();
				if(systemTime.safeSleep(timer, vibeTime)) return;

				if(!simulate) nipplesSwitch.setOff();
				if(!simulate) probeSwitch.setOff();
				if(!simulate) vibeSwitch.setOff();
				if(systemTime.safeSleep(timer, restTime)) return;


				stim1Time = stim1Time + (long) ((float)stim1Time*0.2);			
				stim2Time = stim2Time + (long) ((float)stim2Time*0.2);
				vibeTime =  (long) ((float)stim1Time*0.1) + stim1Time + (long) ((float)vibeTime*0.1);
				restTime = restTime - (long) ((float)restTime*0.2);

				if(stim1Time >= 120*timeMutiple) {
					stim1Time = 300*timeMutiple;
				}

				if(stim2Time >= 120*timeMutiple) {
					stim2Time = 300*timeMutiple;
				}

				if(vibeTime >= 45*timeMutiple) {
					vibeTime = 45*timeMutiple;
				}

				if(restTime <= 15*timeMutiple) {
					restTime = 15*timeMutiple;
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
		return length * timeMutiple + systemTime.currentTime();
	}
}
