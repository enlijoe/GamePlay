package com.jgr.game.vac.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.PressureSensor;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceNames;
import com.jgr.game.vac.service.PropertyService;

public class MaintainVacuumThreadImpl implements Runnable {
	@Autowired private DeviceNames deviceNames;
	@Autowired private PropertyService propertyService;
	@Autowired private SmartThings smartThings;
	@Autowired private SystemTime systemTime;
	@Autowired private PressureSensor externalPressure;
	
	Thread me;
	
	private Logger logger = LoggerFactory.getLogger(MaintainVacuumThreadImpl.class);

	boolean running = false;
	boolean paused = false;
	
	@Override
	public void run() {
		running = true;
		me = Thread.currentThread();
		me.setName("Do Seal");

		logger.info("Mataining vacuum Started");
		
		try {
			if(propertyService.getPumpRestTime() == 0 && !externalPressure.isAvailable()) {
				// 100% duty cycle no pressure sensor
				if(!paused)  {
					smartThings.setDeviceState(deviceNames.getPumpSwitch(), true);
				}
			}
			while(running) {
				if(externalPressure.isAvailable()) {
					// wait until pressure falls to low
					smartThings.setDeviceState(deviceNames.getPumpSwitch(), false);
					while(poolPressure(propertyService.getVacuumPoolTime()) >= propertyService.getMinVacuumPressure()) {
						if(!running) {
							break;
						}
					}
				} else {
					if(propertyService.getPumpRestTime() == 0) {
						// we are on a 100% duty cycle with no pressure sensor so short circuit this
						smartThings.setDeviceState(deviceNames.getPumpSwitch(), true);
						waitWithShutdown(propertyService.getPumpRunTime()*propertyService.getTimeMutiple());
						continue;
					} else {
						waitWithShutdown(propertyService.getPumpRestTime() * propertyService.getTimeMutiple());
					}
				}
				if(!running) {
					break;
				}
				logger.info("Resealing");
				
				if(externalPressure.isAvailable()) {
					// turn on pump until we get to the max pressure
					if(!paused) {
						smartThings.setDeviceState(deviceNames.getPumpSwitch(), true);
					}
					while(poolPressure(propertyService.getVacuumPoolTime()) <= propertyService.getMaxVacuumPressure()) {
						if(!running) {
							break;
						}
					}
				} else {
					if(!paused) {
						smartThings.setDeviceState(deviceNames.getPumpSwitch(), true);
					}
					waitWithShutdown(propertyService.getPumpRunTime() * propertyService.getTimeMutiple());
				}
				if(!running) {
					break;
				}
				smartThings.setDeviceState(deviceNames.getPumpSwitch(), false);
			}
		} catch (InterruptedException iex) {
			logger.info("Mataining vacuum Interrupted");
		} finally {
			logger.info("Mataining vacuum Ended");
			me = null;
		}
	}
	
	public long poolPressure(long time) throws InterruptedException {
		systemTime.sleep(time);
		return externalPressure.getPressure("vacuum");
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
				smartThings.setDeviceState(deviceNames.getPumpSwitch(), true);
			}
		} else {
			if(value) {
				paused = true;
				smartThings.setDeviceState(deviceNames.getPumpSwitch(), false);
			}
		}
	}
}
