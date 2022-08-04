package com.jgr.game.vac.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.SmartThings;

public class WatchDog extends Thread implements SmartThings {
	@Autowired private Esp32Impl esp32;
	@Autowired private SmartThingsImpl smartThings;
	@Autowired private DeviceNames deviceNames;

	public class MaxTime {
		private long endTime;
		private Runnable expiredTimerAction;
		private String reason;
		
		public MaxTime(Runnable expiredTimerAction, long maxTime, String reason) {
			this.expiredTimerAction = expiredTimerAction;
			endTime = System.currentTimeMillis() + maxTime;
			this.reason = reason;
		}
		
		public boolean isExpired() {
			return System.currentTimeMillis() > endTime;
		}
	}
	
	public class WatchTimer {
		private boolean checkedin = true;
		private Runnable expiredTimerAction;
		private String reason;
		
		public WatchTimer(Runnable expiredTimerAction, String reason) {
			this.expiredTimerAction = expiredTimerAction;
			this.reason = reason;
			checkedin = true;
		}
		
		public void checkin() {
			checkedin = true;
		}
	}
	
	private Logger logger = LoggerFactory.getLogger(WatchDog.class);

	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
	boolean saftyValveState = false;
	boolean knownSaftyValveState = false;
	List<WatchTimer> timerList = new ArrayList<>();
	List<MaxTime> maxTimerList = new ArrayList<>();
	boolean running = false;
	long timeToCheckIn = 0;
	long espCheckTime = 0;
	long nextWatchDogCheck;
	long watchDogTickTime = 10000;		
	
	private boolean isAliveAndKicking() {
		boolean watchDogKicking = System.currentTimeMillis() < nextWatchDogCheck + 60000;
		
		if(!running || !watchDogKicking) {
			logger.error("The WatchDog died for unknown reason");
			enterErroredState();
		}
		return running && watchDogKicking; 
	}
	
	public void init() throws IOException {
		if(!running) {
			logger.info("Starting the watch dog manager");
			timeToCheckIn = 5 * 60 * 1000;
			espCheckTime = 1 * 60 * 1000;
			start();
			
			try {
				for(int count = 0; count < 60 && !running; count++) sleep(1000);
			} catch(InterruptedException iex) {
				logger.warn("Inturrpted while waiting for watch dog thread to start");
			}
		}
	}
	
	public void setSaftyValveState(boolean saftyValveState) {
		logger.info("Safety Valve set to " + saftyValveState);
		if(isAliveAndKicking()) {
			this.saftyValveState = saftyValveState;
			this.interrupt();
		} else {
			throw new IllegalStateException("Not allowed while the Watch Dog is not running");
		}
	}
	
	public WatchDog() throws IOException {
		running = false;
		setName("WatchDog");
		smartThings = new SmartThingsImpl();
		esp32 = new Esp32Impl();
	}
	
	public void removeTimer(WatchTimer timer) {
		synchronized(this) {
			timerList.remove(timer);
		}
		logger.info("Watch dog removed for " + timer.reason);
	}
	
	public void setTimeToCheckIn(long timeToCheckIn) {
		this.timeToCheckIn = timeToCheckIn;
	}
	
	public WatchTimer addTimer(Runnable expiredTimerAction, String reason) {
		WatchTimer timer = new WatchTimer(expiredTimerAction, reason);
		synchronized(this) {
			timerList.add(timer);
		}
		logger.info("Watch dog create for " + reason);
		return timer;
	}
	
	public MaxTime creatMaxTimer(long maxTime, Runnable expiredTimerAction, String reason) {
		MaxTime timer = new MaxTime(expiredTimerAction, maxTime, reason);
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(timer.endTime);
		logger.info("Max time created for " + reason + " to last " + maxTime + "ms to expire at " + formatter.format(cal.getTime()));
		synchronized (this) {
			maxTimerList.add(timer);
		}
		return timer;
	}
	
	public void removeMaxTimer(MaxTime timer) {
		synchronized (this) {
			maxTimerList.remove(timer);
		}
		logger.info("Max time removed for " + timer.reason);
	}
	
	public void listAllDevices() {
		if(isAliveAndKicking()) {
			smartThings.listAllDevices();
		} else {
			throw new IllegalStateException("Not allowed while the Watch Dog is not running");
		}
	}

	public int getSwitchState(String id) {
		if(isAliveAndKicking()) {
			return smartThings.getSwitchState(id);
		} else {
			throw new IllegalStateException("Not allowed while the Watch Dog is not running");
		}
	}

	public void setDeviceState(String id, boolean state) {
		if(isAliveAndKicking()) {
			smartThings.setDeviceState(id, state);
		} else {
			throw new IllegalStateException("Monitor thread not running");
		}
	}

	public boolean isOn(int value) {
		if(isAliveAndKicking()) {
			return smartThings.isOn(value);
		} else {
			throw new IllegalStateException("Not allowed while the Watch Dog is not running");
		}
	}

	public boolean isOff(int value) {
		if(isAliveAndKicking()) {
			return smartThings.isOff(value);
		} else {
			throw new IllegalStateException("Not allowed while the Watch Dog is not running");
		}
	}

	@Override
	public void run() {
		nextWatchDogCheck = System.currentTimeMillis() + timeToCheckIn;
		long nextEspCheck = System.currentTimeMillis() + espCheckTime;
		
		logger.info("Checking the connection to safety value");
		esp32.getValveStatus();

		running = true;
		logger.info("The watch dog is up and running.");
		while(running) {
			try {
				synchronized(this) {
					// are timers are fatal errors and if even one happens the entire application is shutdown
					//  and all access to the smart things API is disabled
					if(System.currentTimeMillis() > nextWatchDogCheck) {
						for(WatchTimer timer:timerList) {
							if(timer.checkedin) {
								timer.checkedin = false;
							} else {
								logger.info("A check in has been missed, calling the expired action method");
								enterErroredState();
								timer.expiredTimerAction.run();
							}
						}
						nextWatchDogCheck = System.currentTimeMillis() + timeToCheckIn;
					}
					
					for(Iterator<MaxTime> itr = maxTimerList.iterator(); itr.hasNext();) {
						MaxTime timer = itr.next();
						if(timer.isExpired()) {
							logger.info("A timer has expired, calling the expired action method");
							enterErroredState();
							timer.expiredTimerAction.run();
							itr.remove();
						}
					}
				}
				if(!running || System.currentTimeMillis() > nextEspCheck || (knownSaftyValveState != saftyValveState)) {
					nextEspCheck = System.currentTimeMillis() + espCheckTime;
					if(knownSaftyValveState || saftyValveState) {
						knownSaftyValveState = esp32.getValveStatus();
						
						if(knownSaftyValveState != saftyValveState) {
							if(saftyValveState) {
								esp32.turnOnValve();
							} else {
								esp32.turnOffValve();
							}
							knownSaftyValveState = saftyValveState;
						}
					}
				}
				try {
					Thread.sleep(watchDogTickTime);
				} catch (InterruptedException e) {
					logger.info("The watchdog has been worken up");
					// we have just been woken up
				}
			} catch (RuntimeException rex) {
				logger.error("Runtime Exception received in the WatchDog thread.", rex);
			} 
		}
		// hay we were told to monitor the application and we are no longer doing that 
		// so the application is no longer in a valid state therefore we need to exit
		logger.info("Watchdog in final shutodwn");
		enterErroredState();
		System.exit(0);
	}
	
	private void enterErroredState() {
		running = false;
		logger.info("Sutting everything down.");
		esp32.turnOffValve();	
		saftyValveState = false;	// this may cause the application to turn off the safety value again before exiting
		turnDeviceOffNoThrow(deviceNames.getProbeSwitch());
		turnDeviceOffNoThrow(deviceNames.getNipplesSwitch());
		turnDeviceOffNoThrow(deviceNames.getVibeSwitch());
		turnDeviceOffNoThrow(deviceNames.getVibe2Switch());
		turnDeviceOffNoThrow(deviceNames.getPumpSwitch());
		turnDeviceOffNoThrow(deviceNames.getPumpCheck());
		turnDeviceOffNoThrow(deviceNames.getWaterValve());
		turnDeviceOffNoThrow(deviceNames.getStatusLight());
		turnDeviceOffNoThrow(deviceNames.getWaterHeater());
	}
	
	private void turnDeviceOffNoThrow(String device) {
		try {
			smartThings.setDeviceState(device, false);
		} catch(RuntimeException rex) {
			logger.error("Error received while entering default state", rex);
		}
	}

	public void shutdown() {
		running = false;
	}
}
