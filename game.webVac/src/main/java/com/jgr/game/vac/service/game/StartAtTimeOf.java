package com.jgr.game.vac.service.game;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.jgr.game.vac.service.AbstractGamePoller;
import com.jgr.game.vac.service.AbstractGameState;
import com.jgr.game.vac.service.stereotype.GameSetting;

@Service("StartTime")
@Lazy(false)
public class StartAtTimeOf extends AbstractGamePoller<StartAtTimeOf.GameState> {
	private Logger logger = LoggerFactory.getLogger(StartAtTimeOf.class);

	SimpleDateFormat formatter = new SimpleDateFormat();

	protected class GameState extends AbstractGameState {
		@GameSetting(name="game.startTime") private Calendar startTime;
		
		@PostConstruct void fixStart() {
			Calendar now = GregorianCalendar.getInstance();
			
			if(startTime.before(now)) {
				int fieldsToCopy[] = {Calendar.DAY_OF_MONTH, Calendar.MONTH, Calendar.YEAR};
				for(int field:fieldsToCopy) {
					startTime.set(field, now.get(field));
				}
			}
			if(startTime.before(now)) {
				startTime.add(Calendar.DAY_OF_YEAR, 1);
			}
		}
	}

	@Override
	public boolean doCheck(GameState gameState) {
		Calendar now = GregorianCalendar.getInstance();
		return gameState.startTime.before(now);
	}

	@Override
	public long getTimeOut(GameState gameState) {
		return 0;
	}

	@Override
	public Class<GameState> getStateClass() {
		return GameState.class;
	}
	
	@Override
	public void init(GameState gameState) {
		logger.info("Start time set to: " + formatter.format(gameState.startTime.getTime()));
	}
	
}
