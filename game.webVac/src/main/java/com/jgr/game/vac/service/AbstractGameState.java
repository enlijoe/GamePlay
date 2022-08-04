package com.jgr.game.vac.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.service.LocalWatchDog.WatchDogTimer;
import com.jgr.game.vac.service.stereotype.GameSetting;

public abstract class AbstractGameState {
	@Autowired public LocalWatchDog watchDog;
	@Autowired public SystemTime systemTime;
	private List<WatchDogTimer> timers = new ArrayList<WatchDogTimer>();
	public Thread thread = null;
	public boolean running = false;
	
	@GameSetting(name="basic.timerMutipiler") private long timerMutipiler;
	
	public void removeTimer(WatchDogTimer timer) {
		watchDog.removeTimer(timer);
		timers.remove(timer);
	}

	public WatchDogTimer addTimer(GameOperation expiredTimerAction, String reason) {
		WatchDogTimer timer = watchDog.addTimer(expiredTimerAction, reason);
		timers.add(timer);
		return timer;
	}

	public boolean resetAllOutputDevices() {
		// TODO finish this
		return true;
	}

	public GameStatus safeSleep(long time){
		return safeSleep(time, null);
	}

	public long getTimerMutipiler() {
		return timerMutipiler;
	}
	
	public void interrupt() {
		if(thread != null) {
			thread.interrupt();
		}
	}

	public void checkin() {
		for(WatchDogTimer timer:timers) {
			timer.checkin();
		}
	}

	public void addTimer(WatchDogTimer timer) {
		timers.add(timer);
	}

	public GameStatus safeSleep(long time, GameTestValue extraCheck) {
		long curTime = systemTime.currentTime();
		long endTIme =  curTime+ time;
		long timeLeft = endTIme-curTime; 
		try {
			while(timeLeft > 0) {
				for(WatchDogTimer timer:timers) {
					timer.checkin();
				}
				if(timeLeft > 5000) {
					systemTime.sleep(5000);
				} else {
					systemTime.sleep(timeLeft);
				}
				curTime = systemTime.currentTime();
				timeLeft = endTIme-curTime;  
				if(extraCheck != null && extraCheck.runTest()) {
				}
			}
		} catch(InterruptedException ex) {
			return GameStatus.interrupted;
		}
		return GameStatus.good;
	}
}
