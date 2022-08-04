package com.jgr.game.vac.service;

public abstract class AbstractGameRunnable<T extends AbstractGameState> extends AbstractGameObject<T> {
	public void init(T gameState){}

	public void shutdown(T gameState) {}

	public abstract GameStatus run(T gameState);
}
