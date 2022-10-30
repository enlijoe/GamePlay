package com.jgr.game.vac.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.stereotype.InjectDevice;

public class SystemTimeImpl implements SystemTime {
	private Logger logger = LoggerFactory.getLogger(MainLine.class);

	@Value("${game.timeMutiple}") private long timeMutiple;

	@InjectDevice("${deviceUrl.status}") private InputDevice statusDevice;
	
	@Override
	public long currentTime() {
		return System.currentTimeMillis();
	}
	
	@Override
	public void sleep(long time) throws InterruptedException {
		Thread.sleep(time);
	}

	@Override
	public void sleep(WatchDog.WatchTimer timer, long time) throws InterruptedException {
		long curTime = currentTime();
		long endTIme =  curTime + time;
		long timeLeft = endTIme-curTime; 
		
		while(timeLeft > 0) {
			timer.checkin();
			if(timeLeft > 5 * timeMutiple) {
				sleep(5 * timeMutiple);
			} else {
				sleep(timeLeft);
			}
			curTime = currentTime();
			timeLeft = endTIme-curTime;  
		}
	}
	
	/**
	 * Sleeps for time in ms checking in with the watch dog timer while monitoring the status input
	 * 
	 * Returns true if sleep interrupted by status change
	 * false if normal 
	 */
	public boolean safeSleep(WatchDog.WatchTimer timer, long time) throws InterruptedException {
		long curTime = currentTime();
		long endTIme =  curTime+ time;
		long timeLeft = endTIme-curTime; 
		
		while(timeLeft > 0) {
			timer.checkin();
			if(timeLeft > 1000) {
				sleep(1000);
			} else {
				sleep(timeLeft);
			}
			curTime = currentTime();
			timeLeft = endTIme-curTime;  
			
			if(statusDevice.isOn()) {
				logger.info("Restarting");
				return true;
			}
		}
		return false;
	}
	
}
