package com.jgr.game.vac.thread;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.PressureDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;

public class MaintainVacuumThreadImpl implements Runnable {
	@Autowired private SystemTime systemTime;
	@Autowired private DeviceMapperService deviceMapperService;
	
	@Value("${game.timeMutiple}") long timeMutiple;
	@Value("${game.antiStruggle:false}") boolean antiStruggle;
	@Value("${game.antiStruggleLength:60000}") long antiStruggleLength;
	@Value("${game.antiStruggleRest:0.0}") float antiStruggleEnd;
	
	@Value("${game.strugglePressure}") float strugglePressure;
	
	Thread me;
	
	private Logger logger = LoggerFactory.getLogger(MaintainVacuumThreadImpl.class);

	boolean running = false;
	boolean paused = false;
	
	@Value("${deviceUrl.pumpSwitch}") private String pumpSwitchUrl;
	@Value("${deviceUrl.pumpSwitch2}") private String pumpSwitch2Url;
	@Value("${deviceUrl.vaccumPressure}") private String vaccumPressureUrl;
	
	private OutputDevice pumpSwitch;
	private OutputDevice pumpSwitch2;
	private PressureDevice vacuumPressure;
	
	@PostConstruct
	public void afterPropsSet() {
		pumpSwitch = deviceMapperService.getDevice(new DeviceUrl(pumpSwitchUrl)); 
		pumpSwitch2 = deviceMapperService.getDevice(new DeviceUrl(pumpSwitch2Url)); 
		vacuumPressure = deviceMapperService.getDevice(new DeviceUrl(vaccumPressureUrl)); 
		
		if(antiStruggle && strugglePressure < antiStruggleEnd) {
			throw new RuntimeException("strugglePressure must be less then antiStruggleEnd");
		}
	}
	
	/*
	 * monitor the vacuum for spikes
	 * if there is a spike and the struggle control is on then turn on second pump for 1 min
	 * loop until stopped 
	 */
	
	boolean antiStruggleOn = false;
	boolean antiStruggleRest = false;
	long antiStruggleStart = 0;
	
	@Override
	public void run() {
		running = true;
		me = Thread.currentThread();
		me.setName("Do Seal");

		logger.info("Mataining vacuum Started");
		
		try {
			while(running) {
				if(antiStruggleOn) {
					if(antiStruggleLength < antiStruggleStart - systemTime.currentTime()) {
						pumpSwitch2.setOff();
						antiStruggleOn = false;
						antiStruggleRest = true;
					}
				}
				
				if(antiStruggleRest) {
					float curVacuum = vacuumPressure.readValue();
					if(curVacuum > antiStruggleEnd) {
						antiStruggleRest = false;
					}
				}
					
				if(antiStruggle && (!antiStruggleRest && !antiStruggleOn)) {
					float curVacuum = vacuumPressure.readValue();
					if(curVacuum < strugglePressure) {
						antiStruggleOn = true;
						antiStruggleStart = systemTime.currentTime();
						pumpSwitch2.setOn();
					}
				}
				systemTime.sleep(1000);
			}
		} catch (InterruptedException iex) {
			logger.info("Mataining vacuum Interrupted");
		} finally {
			logger.info("Mataining vacuum Ended");
			me = null;
		}
	}
	
	public float poolPressure(long time) throws InterruptedException {
		systemTime.sleep(time);
		return vacuumPressure.readValue();
	}

	public boolean waitWithShutdown(long time) throws InterruptedException {
		long end =  systemTime.currentTime() + time;
		
		while(end > systemTime.currentTime()) {
			if(!running) {
				return false;
			}
			systemTime.sleep(1000);
		}
		
		return true;
	}
	
	public void shutdown() throws InterruptedException {
		running = false;
		if(me != null) {
			me.join(5000);
		}
	}
	
	public void pause(boolean value) {
		if(paused) {
			if(!value) {
				paused = false;
				pumpSwitch.setOn();
			}
		} else {
			if(value) {
				paused = true;
				pumpSwitch.setOff();
			}
		}
	}
}
