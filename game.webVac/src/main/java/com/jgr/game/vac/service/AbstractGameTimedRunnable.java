package com.jgr.game.vac.service;

public abstract class AbstractGameTimedRunnable<T extends AbstractGameState> extends AbstractGameRunnable<T> {
	abstract public long getMaxAllowedRunTime(T gameState);
	abstract public String getOperationName(T gameState);
}
