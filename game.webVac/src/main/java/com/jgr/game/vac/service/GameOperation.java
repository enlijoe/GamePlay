package com.jgr.game.vac.service;

public interface GameOperation extends Runnable {
	public void run();
	public GameStatus runWithReturn(); 
	GameStatus getReturnValue();
}
