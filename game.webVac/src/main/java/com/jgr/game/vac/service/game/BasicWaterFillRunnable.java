package com.jgr.game.vac.service.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.jgr.game.vac.service.AbstractGameState;
import com.jgr.game.vac.service.AbstractGameTimedRunnable;
import com.jgr.game.vac.service.GamePoller;
import com.jgr.game.vac.service.GameStatus;
import com.jgr.game.vac.service.OutputDevice;
import com.jgr.game.vac.service.stereotype.GameSetting;

@Service("BasicWaterFill")
@Lazy(false)
public class BasicWaterFillRunnable extends AbstractGameTimedRunnable<BasicWaterFillRunnable.GameState> {
	private Logger logger = LoggerFactory.getLogger(ControledFillRunnable.class);

	protected static class GameState extends AbstractGameState {
		@GameSetting(name="game.fillOperation") private GamePoller waterFillPoller;
		
		@GameSetting(name="device.fillOperation") private OutputDevice waterValve;
		@GameSetting(name="device.fillOperation") private long timerMutipiler;

	}
	
	@Override
	public GameStatus run(GameState gameState) {
		try {
			logger.info("Starting fill.");
			gameState.waterValve.setValue(1);
			return gameState.waterFillPoller.run();
		} finally {
			logger.info("Fill Complete.");
			gameState.waterValve.setValue(0);
		}
	}

	@Override
	public String getOperationName(GameState gameState) {
		return "Basic Water Filler";
	}
	
	@Override
	public long getMaxAllowedRunTime(GameState gameState) {
		return 30 * 60 * gameState.timerMutipiler;
	}
	
	@Override
	public Class<GameState> getStateClass() {
		return GameState.class;
	}
}
