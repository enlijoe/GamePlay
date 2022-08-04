package com.jgr.game.vac.service;

public abstract class AbstractGameTestValue<T extends AbstractGameState> extends AbstractGameObject<T> {
	public abstract boolean runTest(T gameState);
}
