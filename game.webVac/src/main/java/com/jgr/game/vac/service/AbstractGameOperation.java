package com.jgr.game.vac.service;

public abstract class AbstractGameOperation<T extends AbstractGameState> extends AbstractGameObject<T> {
	public abstract GameStatus run(T gameState);

}
