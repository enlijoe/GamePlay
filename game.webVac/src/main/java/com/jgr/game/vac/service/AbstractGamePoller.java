package com.jgr.game.vac.service;

public abstract class AbstractGamePoller<T extends AbstractGameState> extends AbstractGameObject<T> {
	private final long checkTime = 2000;

	public void init(T gameState) {};
	abstract public boolean doCheck(T gameState);
	abstract public long getTimeOut(T gameState);
	public void deInit(T gameState) {};

	public final GameStatus run(T gameState) {
		GameStatus status = GameStatus.good;
		init(gameState);
		long startTime = gameState.systemTime.currentTime();
		long timeOut = getTimeOut(gameState);
		try {
			while(!doCheck(gameState)) {
				if(timeOut != 0) {
					if(startTime + timeOut > gameState.systemTime.currentTime()) {
						return GameStatus.error;
					}
				}
				if((status = gameState.safeSleep(checkTime)) != GameStatus.good) return status;
			}
		} finally {
			deInit(gameState);
		}
		return status;
	}
	
}
