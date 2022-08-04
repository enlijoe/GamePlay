package com.jgr.game.vac.service.dummy;

import org.springframework.stereotype.Service;

import com.jgr.game.vac.service.AbstractGameState;
import com.jgr.game.vac.service.AbstractGameTimedRunnable;
import com.jgr.game.vac.service.GameStatus;

@Service("DummyGameTimedRunnable")
public class DummyGameTimedRunnable extends AbstractGameTimedRunnable<AbstractGameState> {

	@Override
	public long getMaxAllowedRunTime(AbstractGameState state) {
		return 0;
	}
	
	@Override
	public Class<AbstractGameState> getStateClass() {
		return null;
	}

	@Override
	public String getOperationName(AbstractGameState gameState) {
		return "Dummy operation";
	}
	
	@Override
	public GameStatus run(AbstractGameState gameState) {
		return GameStatus.good;
	}
}
