package com.jgr.game.vac.service.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.jgr.game.vac.service.AbstractGameRunnable;
import com.jgr.game.vac.service.AbstractGameState;
import com.jgr.game.vac.service.GameStatus;
import com.jgr.game.vac.service.InputDevice;
import com.jgr.game.vac.service.OutputDevice;
import com.jgr.game.vac.service.stereotype.GameSetting;

@Service("ControledWaterFill")
@Lazy(false)
public class ControledFillRunnable extends AbstractGameRunnable<ControledFillRunnable.GameState> {
	private Logger logger = LoggerFactory.getLogger(ControledFillRunnable.class);

	protected static class GameState extends AbstractGameState {
		@GameSetting(name="device.fillOperation") private OutputDevice waterValve;

		@GameSetting(name="device.fillOperation") private InputDevice armSignal;

		@GameSetting(name="waterFill.fillOperation") private long totalOnTime;
		@GameSetting(name="waterFill.fillOperation") private long flowOnTime;
		@GameSetting(name="waterFill.fillOperation") private long flowOffTime;
		@GameSetting(name="waterFill.fillOperation") private long initalOnTime;
		@GameSetting(name="waterFill.fillOperation") private long flowTime;
		@GameSetting(name="waterFill.fillOperation") private long fillRestFullFlow;
		
	}
	
	@Override
	/**
	 * sequence sum
	 * t = n/2[2a + (n - 1)d]
	 * n = number, d = slowDown, a = 1, t = total
	 * t = total - flow - n*flowOffTIme 
	 * n = flow/flowOnTime
	 * d = (2t/n-2)/(n-1)
	 * 
	 */
	public GameStatus run(GameState gameState) {
		GameStatus status = GameStatus.good;
		
		if(gameState.totalOnTime == 0 || gameState.flowOnTime == 0) return GameStatus.good;
		
		long restTime = gameState.flowOffTime*gameState.getTimerMutipiler();
		long totalRunTime = gameState.initalOnTime*gameState.getTimerMutipiler();
		long totalTime = totalRunTime;

		long numberOfSegs = (gameState.totalOnTime)/gameState.flowOnTime;
		long totalSlowDown = (gameState.flowTime - gameState.totalOnTime - numberOfSegs*gameState.flowOffTime)*gameState.getTimerMutipiler(); 
		long flowSlowDown = (2*totalSlowDown/numberOfSegs-2)/(numberOfSegs-1) ;
		numberOfSegs--;
		
		logger.info("Segs "+ numberOfSegs + " with slow down of " + ((float )flowSlowDown)/(float)gameState.getTimerMutipiler() + "s");
		
		try {
			logger.info("Doing fill.");
			
			gameState.waterValve.setValue(1);
			if((status = gameState.safeSleep(gameState.initalOnTime*gameState.getTimerMutipiler())) != GameStatus.good) {
				logger.info("Restarting during a fill");
				return status;
			}
			restTime = gameState.flowOffTime*gameState.getTimerMutipiler() + flowSlowDown*numberOfSegs;
			logger.info("Resting (" + restTime + "ms)");
			gameState.waterValve.setValue(0);

			while(totalRunTime < gameState.totalOnTime*gameState.getTimerMutipiler()) {
				if((status = gameState.safeSleep(restTime)) != GameStatus.good) {
					return status;
				}
				
				gameState.waterValve.setValue(1);
				
				if((status = gameState.safeSleep(gameState.flowOnTime*gameState.getTimerMutipiler())) != GameStatus.good) {
					return status;
				}
				
				gameState.waterValve.setValue(0);
				totalRunTime += gameState.flowOnTime*gameState.getTimerMutipiler();
				totalTime += gameState.flowOnTime*gameState.getTimerMutipiler() + restTime;
				restTime-=flowSlowDown;
				logger.info("Resting (" + ((float) restTime)/(float)gameState.getTimerMutipiler() + "s) total time " + ((float)totalTime)/(float)gameState.getTimerMutipiler() + "s with Run " + ((float)totalRunTime)/(float)gameState.getTimerMutipiler() + "s time");
			}
		} finally {
			logger.info("Fill Completed");
			gameState.waterValve.setValue(0);
		}
		
		if(gameState.fillRestFullFlow != 0) {
			logger.info("On Full Flow for " + gameState.fillRestFullFlow);
			try {
				gameState.waterValve.setValue(1);
				if((status = gameState.safeSleep(gameState.fillRestFullFlow * gameState.getTimerMutipiler())) != GameStatus.good) return status;
			} finally {
				gameState.waterValve.setValue(0);
			}
		}
		return GameStatus.good;
	}

	@Override
	public Class<GameState> getStateClass() {
		return GameState.class;
	}

}
