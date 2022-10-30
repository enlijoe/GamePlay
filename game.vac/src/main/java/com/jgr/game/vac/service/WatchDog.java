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

public class WatchDog extends Thread {
	@Autowired private RemoteWatchDog[] remoteWatchDogs;
	
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
	boolean watchDogState = false;
	boolean remoteStatus = false;
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
	
	// start the watchdog thread and wait for it to get started
	public void init() throws IOException {
		if(!running) {
			logger.info("Starting the watch dog manager.");
			timeToCheckIn = 5 * 60 * 1000;
			espCheckTime = 1 * 60 * 1000;
			start();
			
			try {
				for(int count = 0; count < 60 && !running; count++) sleep(1000);
				logger.info("Watch dog manager has started.");
			} catch(InterruptedException iex) {
				logger.warn("Inturrpted while waiting for watch dog thread to start");
			}
		}
	}
	
	public void setEnabled(boolean state) {
		logger.info("Watchgod set to " + state);
		if(isAliveAndKicking()) {
			this.watchDogState = state;
			this.interrupt();
		} else {
			throw new IllegalStateException("Not allowed while the Watch Dog is not running");
		}
	}
	
	public WatchDog() throws IOException {
		running = false;
		setName("WatchDog");
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
	

	@Override
	public void run() {
		nextWatchDogCheck = System.currentTimeMillis() + timeToCheckIn;
		long nextRemoteCheckin = System.currentTimeMillis() + espCheckTime;
		
		logger.info("Checking the connection to safety value");
		internalReset();
		internalEnable();
		internalCheckIn();
		
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
								internalErrorState();
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
							internalErrorState();
							enterErroredState();
							timer.expiredTimerAction.run();
							itr.remove();
						}
					}
				}
				if(!running || System.currentTimeMillis() > nextRemoteCheckin || (remoteStatus != watchDogState)) {
					nextRemoteCheckin = System.currentTimeMillis() + espCheckTime;
					internalCheckIn();
					if(internalGetStatus()) {
						logger.info("something is wrong on a remote");
						enterErroredState();
						internalErrorState();
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
		logger.info("Entering error state, sutting everything down.");
		watchDogState = false;	// this may cause the application to turn off the safety value again before exiting
		System.exit(-1);
	}
	
	public void shutdown() {
		running = false;
	}
	
	private void internalCheckIn() {
		for(RemoteWatchDog remoteWatchDog:remoteWatchDogs) {
			remoteWatchDog.checkIn();
		}
	}
	
	private boolean internalGetStatus() {
		for(RemoteWatchDog remoteWatchDog:remoteWatchDogs) {
			if(remoteWatchDog.getStatus()) {
				logger.info("Remote " + remoteWatchDog.getDescription() + " reported an error");
				return true;
			}
		}
		return false;
	}
	
	private void internalReset() {
		for(RemoteWatchDog remoteWatchDog:remoteWatchDogs) {
			remoteWatchDog.reset();
		}
	}
	
	private void internalErrorState() {
		for(RemoteWatchDog remoteWatchDog:remoteWatchDogs) {
			remoteWatchDog.errorState();
		}
	}
	
	private void internalEnable() {
		for(RemoteWatchDog remoteWatchDog:remoteWatchDogs) {
			remoteWatchDog.enable();
		}
	}
}
