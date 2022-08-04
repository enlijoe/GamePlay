package com.jgr.game.vac.service.poller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jgr.game.vac.service.AbstractGamePoller;
import com.jgr.game.vac.service.AbstractGameState;
import com.jgr.game.vac.service.InputDevice;
import com.jgr.game.vac.service.stereotype.GameSetting;

@Service
public class ArmPoller extends AbstractGamePoller<ArmPoller.GameState> {
	private Logger logger = LoggerFactory.getLogger(ArmPoller.class);

	protected static class GameState extends AbstractGameState {
		@GameSetting(name="devices.armDevice") private InputDevice armDevice;
	}

	@Override
	public boolean doCheck(GameState gameState) {
		return gameState.armDevice.readValue() != 0;
	}
	
	@Override
	public Class<GameState> getStateClass() {
		return GameState.class;
	}
	
	@Override
	public long getTimeOut(GameState gameState) {
		return 0;
	}
	
	@Override
	public void init(GameState gameState) {
		logger.info("Waitting for arm signal.");
	}
}
