package com.jgr.game.vac.service.game;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jgr.game.vac.service.GameOperation;
import com.jgr.game.vac.service.LocalWatchDog;
import com.jgr.game.vac.service.OutputDevice;
import com.jgr.game.vac.service.RemoteWatchDog;

@Service
public class LocalWatchDogImpl extends Thread implements LocalWatchDog {
	@Autowired private OutputDevice[] outputDevices;
	@Autowired private RemoteWatchDog[] remoteWatchDogs;
	
	public class MaxTime implements WatchDogMaxTime {
		private long endTime;
		private GameOperation expiredTimerAction;
		private String reason;
		
		public MaxTime(GameOperation expiredTimerAction, long maxTime, String reason) {
			this.expiredTimerAction = expiredTimerAction;
			endTime = System.currentTimeMillis() + maxTime;
			this.reason = reason;
		}

		public boolean isExpired() {
			return System.currentTimeMillis() > endTime;
		}
	}
	
	public class WatchTimer implements WatchDogTimer {
		private boolean checkedin = true;
		private GameOperation expiredTimerAction;
		private String reason;
		
		public WatchTimer(GameOperation expiredTimerAction, String reason) {
			this.expiredTimerAction = expiredTimerAction;
			this.reason = reason;
			checkedin = true;
		}
		
		public void checkin() {
			checkedin = true;
		}
	}
	
	private Logger logger = LoggerFactory.getLogger(LocalWatchDogImpl.class);

	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
	List<WatchTimer> timerList = new ArrayList<>();
	List<MaxTime> maxTimerList = new ArrayList<>();
	boolean running = false;
	long timeToCheckIn = 0;
	long remoteCheckInFrequency = Long.MAX_VALUE;
	long nextWatchDogCheck;
	long watchDogTickTime = 10000;		
	
	@PostConstruct
	public void init() throws IOException {
		if(!running) {
			logger.info("Starting the watch dog manager");
			long minCheckInFrequency = Long.MAX_VALUE;

			timeToCheckIn = 5 * 60 * 1000;
			for(RemoteWatchDog remoteWatchDog:remoteWatchDogs) {
				long curCheckInFrequency = remoteWatchDog.getCheckInFrequency(); 
				if(curCheckInFrequency < minCheckInFrequency) {
					minCheckInFrequency = curCheckInFrequency;
				}
			}
			remoteCheckInFrequency = minCheckInFrequency/2;
			start();
			
			try {
				for(int count = 0; count < 60 && !running; count++) sleep(1000);
			} catch(InterruptedException iex) {
				logger.warn("Inturrpted while waiting for watch dog thread to start");
			}
		}
	}
	
	public LocalWatchDogImpl() {
		running = false;
		setName("WatchDog");
	}
	
	public void removeTimer(WatchDogTimer timerIn) {
		if(timerIn instanceof WatchTimer) {
			WatchTimer timer = (WatchTimer) timerIn;
			synchronized(this) {
				timerList.remove(timer);
			}
			logger.info("Watch dog removed for " + timer.reason);
		}
	}
	
	public void setTimeToCheckIn(long timeToCheckIn) {
		this.timeToCheckIn = timeToCheckIn;
	}
	
	public WatchDogTimer addTimer(GameOperation expiredTimerAction, String reason) {
		WatchTimer timer = new WatchTimer(expiredTimerAction, reason);
		synchronized(this) {
			timerList.add(timer);
		}
		logger.info("Watch dog create for " + reason);
		return timer;
	}
	
	public WatchDogMaxTime creatMaxTimer(long maxTime, GameOperation expiredTimerAction, String reason) {
		MaxTime timer = new MaxTime(expiredTimerAction, maxTime, reason);
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(timer.endTime);
		logger.info("Max time created for " + reason + " to last " + maxTime + "ms to expire at " + formatter.format(cal.getTime()));
		synchronized (this) {
			maxTimerList.add(timer);
		}
		return timer;
	}
	
	public void removeMaxTimer(WatchDogMaxTime timerIn) {
		if(timerIn instanceof MaxTime) {
			MaxTime timer = (MaxTime) timerIn;
			synchronized (this) {
				maxTimerList.remove(timer);
			}
			logger.info("Max time removed for " + timer.reason);
		}
	}

	@Override
	public void run() {
		nextWatchDogCheck = System.currentTimeMillis() + timeToCheckIn;
		long nextRemoteCheckIn = System.currentTimeMillis() + remoteCheckInFrequency;
		
		running = true;
		logger.info("The watch dog is up and running.");
		for(RemoteWatchDog remoteWatchDog:remoteWatchDogs) {
			remoteWatchDog.resetState();
			remoteWatchDog.enable();
		}
		
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
				if(!running || System.currentTimeMillis() > nextRemoteCheckIn) {
					nextRemoteCheckIn = System.currentTimeMillis() + remoteCheckInFrequency;
					for(RemoteWatchDog remoteWatchDog:remoteWatchDogs) {
						remoteWatchDog.checkIn();
					}
				}
				try {
					Thread.sleep(watchDogTickTime);
				} catch (InterruptedException e) {
					logger.info("The watchdog has been worken up");
					// we have just been woken up
				}
			} catch (Throwable throwable) {
				logger.error("Runtime Exception received in the WatchDog thread.", throwable);
				enterErroredState();
			} 
		}
		// hay we were told to monitor the application and we are no longer doing that 
		// so the application is no longer in a valid state therefore we need to exit
		logger.info("Watchdog in final shutodwn");
		for(RemoteWatchDog remoteWatchDog:remoteWatchDogs) {
			remoteWatchDog.disable();
		}
	}
	
	private void enterErroredState() {
		running = false;
		logger.info("Entering errored state.");
		for(RemoteWatchDog remoteWatchDog:remoteWatchDogs) {
			remoteWatchDog.enterErroredState();
		}
		
		for(OutputDevice outputDevice:outputDevices) {
			if(!outputDevice.isWatchDogControled()) {
				outputDevice.setDefaultState();
			}
		}
	}
	
	public void shutdown() {
		running = false;
	}
}
