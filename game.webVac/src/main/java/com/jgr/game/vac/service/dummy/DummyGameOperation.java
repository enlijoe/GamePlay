package com.jgr.game.vac.service.dummy;

import org.springframework.stereotype.Service;

import com.jgr.game.vac.service.AbstractGameOperation;
import com.jgr.game.vac.service.AbstractGameState;
import com.jgr.game.vac.service.GameStatus;

@Service("DummyGameOperation")
public class DummyGameOperation extends AbstractGameOperation<AbstractGameState> {

	@Override
	public GameStatus run(AbstractGameState gameState) {
		return GameStatus.good;
	}
	
	@Override
	public Class<AbstractGameState> getStateClass() {
		return AbstractGameState.class;
	}

}
