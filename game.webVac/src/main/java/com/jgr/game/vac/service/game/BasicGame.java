package com.jgr.game.vac.service.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.jgr.game.vac.service.AbstractGameRunnable;
import com.jgr.game.vac.service.AbstractGameState;
import com.jgr.game.vac.service.GameOperation;
import com.jgr.game.vac.service.GamePoller;
import com.jgr.game.vac.service.GameRunnable;
import com.jgr.game.vac.service.GameStatus;
import com.jgr.game.vac.service.GameThread;
import com.jgr.game.vac.service.GameTimedRunnable;
import com.jgr.game.vac.service.LocalWatchDog.WatchDogTimer;
import com.jgr.game.vac.service.OutputDevice;
import com.jgr.game.vac.service.stereotype.GameSetting;

@Service("BasicGame")
@Lazy(false)
public class BasicGame extends AbstractGameRunnable<BasicGame.GameState> {
	private static Logger logger = LoggerFactory.getLogger(BasicGame.class);
	
	protected static class GameState extends AbstractGameState {
		@GameSetting(name="game.fillOperation") private GameTimedRunnable fillOperation;
		@GameSetting(name="game.stimThread") private GameTimedRunnable stimThread;
		@GameSetting(name="game.startTimePoller") private GamePoller startTimePoller;
		@GameSetting(name="game.startSignalPooler") private GamePoller startSignalPooler;
		@GameSetting(name="game.armSignalPoller") private GamePoller armSignalPoller;
		@GameSetting(name="game.sealCompletePoller") private GamePoller sealCompletePoller;
		@GameSetting(name="game.mintainVacuumRunable") private GameRunnable mintainVacuumRunable;
		@GameSetting(name="game.fillRestTimePoller") private GamePoller fillRestTimePoller;
		@GameSetting(name="game.selfTest") private GameOperation selfTest;
		
		@GameSetting(name="game.maintainVacuumThread") private GameThread maintainVacuumThread = null;

		@GameSetting(name="devices.waterHeater") private OutputDevice waterHeater;
		@GameSetting(name="devices.statusLight") private OutputDevice statusLight;
		
	}
	
	public static class WatchDogTimerExpired implements GameOperation {
		BasicGame.GameState gameState;
		String message;
		GameStatus retVal;
		
		WatchDogTimerExpired(BasicGame.GameState gameState, String message) {
			this.gameState = gameState;
			this.message = message;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
		}
		
		@Override
		public GameStatus getReturnValue() {
			return retVal;
		}
		
		@Override
		public GameStatus runWithReturn() {
			run();
			return retVal;
		}
	}
	
	public GameStatus run(GameState gameState) {
		GameStatus status = GameStatus.good;
		
		WatchDogTimer timer = gameState.addTimer(new WatchDogTimerExpired(gameState, "Watch Dog expired for Running program"), "Main process");
		if(gameState.resetAllOutputDevices()) {
			throw new RuntimeException("Error setting up default state.");
		}
		
		gameState.statusLight.setValue(1);

		try {
			if((status = gameState.armSignalPoller.run()) != GameStatus.good) return status;

			gameState.statusLight.setValue(0);

			gameState.waterHeater.setValue(1);
			
			if((status = gameState.startSignalPooler.run()) != GameStatus.good) return status;
			
			// now engage the pump until sealed
			if((status = gameState.sealCompletePoller.run()) != GameStatus.good) return status;
			
			// spawn task to cycle pump and maintain vacuum
			gameState.maintainVacuumThread.start();

			if((status = gameState.selfTest.runWithReturn()) != GameStatus.good) return status;
			
			logger.info("Waiting to start"); 
			if((status = gameState.startTimePoller.run()) != GameStatus.good) return status;
			
			if((status = gameState.fillOperation.runWithReturn()) != GameStatus.good) return status;
			
			if((status = gameState.fillRestTimePoller.run()) != GameStatus.good) return status;

			logger.info("Rest Complete");
			gameState.waterHeater.setValue(0);

			logger.info("Starting stim cycles");
			gameState.stimThread.run();
			if((status = gameState.stimThread.getReturnValue()) != GameStatus.good) return status;

		} finally {
			gameState.removeTimer(timer);
		}
		
		return GameStatus.good;
	}

	public void shutdown(GameState gameState) {
		logger.info("All done");
		boolean error;
		int retryCount = 0;
		
		do {
			error = false;
			logger.info("Turning everything off retry");
			// stop children
			if(gameState.maintainVacuumThread.isAlive()) {
				gameState.mintainVacuumRunable.shutdown();
			}
			gameState.maintainVacuumThread = null;

			error = gameState.resetAllOutputDevices();
			try {
				if(error) {
					gameState.safeSleep(1000);
					retryCount ++;
				}
			} catch(Exception ex) {
				logger.error("was not able to sleep for 1 second", ex);
				// if no other error then this does not matter
			}
		} while(error && retryCount < 5);
		
	}
	
	@Override
	public Class<GameState> getStateClass() {
		return GameState.class;
	}

}
