package com.jgr.game.vac.service.dummy;

import org.springframework.stereotype.Service;

import com.jgr.game.vac.service.AbstractGameState;
import com.jgr.game.vac.service.AbstractGameTestValue;

@Service("DummyGameTestValue")
public class DummyGameTestValue extends AbstractGameTestValue<AbstractGameState> {
	@Override
	public boolean runTest(AbstractGameState gameState) {
		return true;
	}
	
	@Override
	public Class<AbstractGameState> getStateClass() {
		return AbstractGameState.class;
	}
}
