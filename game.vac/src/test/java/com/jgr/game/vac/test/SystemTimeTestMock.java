package com.jgr.game.vac.test;

import java.util.ArrayList;

import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.WatchDog.WatchTimer;

public class SystemTimeTestMock implements SystemTime {
	long currentTime = 0;
	
	ArrayList<Thread> sleeping = new ArrayList<Thread>();
	
	public void setCurrentTime(long currentTime) {
		synchronized (this) {
			this.currentTime = currentTime;
			notifyAll();
		}
	}
	
	public void reset() {
		synchronized (this) {
			currentTime = 0;
			if(!sleeping.isEmpty()) {
				for(Thread thread:sleeping) {
					thread.interrupt();
				}
			}
			sleeping.clear();
		}
	}
	
	@Override
	public long currentTime() {
		return currentTime;
	}

	@Override
	public void sleep(WatchTimer timer, long time) throws InterruptedException {
		long curTime = currentTime();
		long endTIme =  curTime+ time;
		long timeLeft = endTIme-curTime; 
		
		while(timeLeft > 0) {
			timer.checkin();
			if(timeLeft > 5000) {
				sleep(5000);
			} else {
				sleep(timeLeft);
			}
			curTime = currentTime();
			timeLeft = endTIme-curTime;  
		}
	}
	
	@Override
	public void sleep(long time) throws InterruptedException {
		synchronized (this) {
			Thread thread = Thread.currentThread();
			try {
				long waitStarted = currentTime;
				sleeping.add(thread); 
				while(waitStarted + time < currentTime) {
					wait();
				}
			} finally {
				sleeping.remove(thread);
			}
		}
	}

	@Override
	public boolean safeSleep(WatchTimer timer, long time) throws InterruptedException {
		sleep(time);
		return false;
	}
}
