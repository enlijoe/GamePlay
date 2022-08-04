package com.jgr.game.vac.service.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.jgr.game.vac.service.AbstractGameState;
import com.jgr.game.vac.service.AbstractGameTimedRunnable;
import com.jgr.game.vac.service.GameStatus;
import com.jgr.game.vac.service.OutputDevice;
import com.jgr.game.vac.service.stereotype.GameSetting;

@Service("BasicStim")
@Lazy(false)
public class BasicStim extends AbstractGameTimedRunnable<BasicStim.GameState> {
	private Logger logger = LoggerFactory.getLogger(BasicStim.class);

	protected static class GameState extends AbstractGameState {
		@GameSetting(name="devices.vibeBeanName") private OutputDevice vibeSwitch;
		@GameSetting(name="devices.vibe2BeanName") private OutputDevice vibe2Switch;
		@GameSetting(name="devices.nippleSwitch")  private OutputDevice nippleSwitch;
		@GameSetting(name="devices.probeSwitch")  private OutputDevice probeSwitch;

		//private boolean cumAllowed;
		@GameSetting(name="stim.numCycles") private int endCycle;
		@GameSetting(name="stim.nippleOnTime") private long nippleOnTime;
		@GameSetting(name="stim.vibe2OnTime") private long vibe2OnTime;
		@GameSetting(name="stim.probeOnTime") private long probeOnTime;
		@GameSetting(name="stim.vibeOnTime") private long vibeOnTime;
		@GameSetting(name="stim.restTime") private long restTime;
		
	}

	@Override
	public String getOperationName(GameState gameState) {
		return "Bastic Stim";
	}
	
	@Override
	public long getMaxAllowedRunTime(GameState gameState) {
		return (gameState.nippleOnTime + gameState.vibe2OnTime + gameState.probeOnTime + gameState.vibeOnTime + gameState.restTime) * (long) gameState.endCycle ;
	}
	
	public GameStatus run(GameState gameState) {
		logger.info("Cycling Stim Started");
		GameStatus status = GameStatus.good;
		int onCycle = 0;
		while(true) {
			// turn on nipples
			logger.info("Stim started");

			try {
				if((status = changeState(gameState, gameState.nippleSwitch, gameState.nippleOnTime)) != GameStatus.good) return status;
				if((status = changeState(gameState, gameState.vibe2Switch, gameState.vibe2OnTime)) != GameStatus.good) return status;
				if((status = changeState(gameState, gameState.probeSwitch, gameState.probeOnTime)) != GameStatus.good) return status;
				if((status = changeState(gameState, gameState.vibeSwitch, gameState.vibeOnTime)) != GameStatus.good) return status;
			} finally {
				// turn off everything
				turnOff(gameState.nippleSwitch, gameState.nippleOnTime);
				turnOff(gameState.probeSwitch, gameState.probeOnTime);
				turnOff(gameState.vibeSwitch, gameState.vibeOnTime);
				turnOff(gameState.vibe2Switch, gameState.vibe2OnTime);
			}
			onCycle++;

			logger.info("Stim Cycle " + onCycle + " Copmplete");
			if(onCycle >= gameState.endCycle) break;
			
			// time to rest
			if((status = gameState.safeSleep(gameState.restTime * gameState.getTimerMutipiler())) != GameStatus.good) return status;
		}
		logger.info("Cycling Stim Ended");
		return status;
	}
	
	private void turnOff(OutputDevice device, long onTime) {
		if(onTime != 0) device.setValue(0);
	}

	private GameStatus changeState(GameState gameState, OutputDevice device, long time) {
		if(time != 0) {
			device.setValue(1);
			return gameState.safeSleep(time * gameState.getTimerMutipiler());  
		}
		return GameStatus.good;
	}

	@Override
	public void shutdown(GameState gameState) {
		turnOff(gameState.nippleSwitch, gameState.nippleOnTime);
		turnOff(gameState.probeSwitch, gameState.probeOnTime);
		turnOff(gameState.vibeSwitch, gameState.vibeOnTime);
		turnOff(gameState.vibe2Switch, gameState.vibe2OnTime);
	}
	
	@Override
	public Class<GameState> getStateClass() {
		return GameState.class;
	}
}
