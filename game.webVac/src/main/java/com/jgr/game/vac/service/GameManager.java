package com.jgr.game.vac.service;

public interface GameManager {
	void getNeededGameProps(long gameId);
	void runGame(long gameId) throws Exception;
}
