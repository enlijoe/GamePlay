package com.jgr.game.vac.service.dummy;

import org.springframework.stereotype.Service;

import com.jgr.game.vac.service.AbstractGamePoller;
import com.jgr.game.vac.service.AbstractGameState;

@Service("DummyGamePoller")
public class DummyGamePoller extends AbstractGamePoller<AbstractGameState> {
	@Override
	public long getTimeOut(AbstractGameState gameState) {
		return 0;
	}
	
	@Override
	public boolean doCheck(AbstractGameState gameState) {
		return true;
	}
	
	@Override
	public Class<AbstractGameState> getStateClass() {
		return AbstractGameState.class;
	}
}
