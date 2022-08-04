package com.jgr.game.vac.service.dummy;

import org.springframework.stereotype.Service;

import com.jgr.game.vac.service.AbstractGameRunnable;
import com.jgr.game.vac.service.AbstractGameState;
import com.jgr.game.vac.service.GameStatus;

@Service("DummyGameRunnable")
public class DummyGameRunnable extends AbstractGameRunnable<AbstractGameState>{
	@Override
	public void shutdown(AbstractGameState gameState) {
	}

	@Override
	public void init(AbstractGameState gameState) {
	}
	
	@Override
	public Class<AbstractGameState> getStateClass() {
		return AbstractGameState.class;
	}
	
	@Override
	public GameStatus run(AbstractGameState gameState) {
		return GameStatus.good;
	}
}
