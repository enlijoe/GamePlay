package com.jgr.game.vac.service;

public interface LocalWatchDog extends Runnable {
	public interface WatchDogTimer {
		public void checkin();
	}
	
	public interface WatchDogMaxTime {
	}
	
	public void removeTimer(WatchDogTimer timer);
	public void setTimeToCheckIn(long timeToCheckIn);
	public WatchDogTimer addTimer(GameOperation expiredTimerAction, String reason);
	public WatchDogMaxTime creatMaxTimer(long maxTime, GameOperation expiredTimerAction, String reason);
	public void removeMaxTimer(WatchDogMaxTime timer);
	public void shutdown();
	public void start();
	public void interrupt();
}
