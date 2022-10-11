package com.jgr.game.vac.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.exceptions.AbortException;
import com.jgr.game.vac.interfaces.DoStimThread;
import com.jgr.game.vac.interfaces.FillRestTimePoller;
import com.jgr.game.vac.interfaces.LightOffPoller;
import com.jgr.game.vac.interfaces.PumpOnPoller;
import com.jgr.game.vac.interfaces.SealCompletePoller;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.StartTimePoller;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.interfaces.WaterFillPoller;
import com.jgr.game.vac.operations.Operation;
import com.jgr.game.vac.thread.MaintainVacuumThreadImpl;
import com.jgr.game.vac.thread.TheTeaseThreadImpl;

public class MainLine  {
	private Logger logger = LoggerFactory.getLogger(MainLine.class);
	
	@Autowired private DeviceNames deviceNames;
	@Autowired private PropertyService propertyService;
	@Autowired private SmartThings smartThings;
	@Autowired private SealCompletePoller sealCompletePoller;
	@Autowired private WaterFillPoller waterFillPoller;
	@Autowired private SystemTime systemTime;
	@Autowired private PumpOnPoller pumpOnPoller;
	@Autowired private LightOffPoller lightOffPoller;
	@Autowired private MaintainVacuumThreadImpl mintainVacuumRunable;
//	@Autowired private DoStimThread doStimRunable;
//	@Autowired private DoStimThread randomStimThread;
	@Autowired private StartTimePoller startTimePoller;
	@Autowired private FillRestTimePoller fillRestTimePoller;
	@Autowired private WatchDog watchDog;
	@Autowired private TheTeaseThreadImpl theTeaseThread;
	@Autowired private Operation selfTestOperation;
	
	private Thread maintainVacuumThread = null;
	private DoStimThread doStimThread = null;

	private WatchDog.WatchTimer timer;
	
	class WatchDogTimerExpired implements Runnable {
		private Thread myThread;
		private String expiredMessage;
		
		WatchDogTimerExpired(String expiredMessage) {
			myThread = Thread.currentThread();
			this.expiredMessage = expiredMessage;
		}
		
		@Override
		public void run() {
			logger.info(expiredMessage + " shutting down system.");
			if(maintainVacuumThread != null) {
				maintainVacuumThread.interrupt();
			}
			myThread.interrupt();
		}
	}
	
	
	public boolean runProgram() throws Exception {
		timer = watchDog.addTimer(new WatchDogTimerExpired("Watch Dog expired for Running program"), "Main process");

		// insure a known state
		try {
			watchDog.setSaftyValveState(false);
			smartThings.setDeviceState(deviceNames.getPumpSwitch(), false);
			// smartThings.setDeviceState(statusLight, false);
			smartThings.setDeviceState(deviceNames.getPumpCheck(), false);
			smartThings.setDeviceState(deviceNames.getNipplesSwitch(), false);
			smartThings.setDeviceState(deviceNames.getProbeSwitch(), false);
			smartThings.setDeviceState(deviceNames.getVibeSwitch(), false);
			smartThings.setDeviceState(deviceNames.getWaterHeater(), false);
			
			smartThings.setDeviceState(deviceNames.getStatusLight(), true);
			
			// now wait for it to turn off
			lightOffPoller.run(timer);
			if(restart()) return true;
			
			if(!propertyService.isSimulate() && propertyService.isUserWaterHeater()) {
				smartThings.setDeviceState(deviceNames.getWaterHeater(), true);
			}
			
			//smartThings.setDeviceState(statusLight, true);
			// wait for the pump to turn on
			pumpOnPoller.run(timer);
			if(restart()) return true;
			
			if(propertyService.isUseVaccume()) {
				logger.info("Waitting for vaccume seal");

				// now engage the pump until sealed
				// TODO have to fix so it will check in with the watch dog
				sealCompletePoller.run(timer);
				logger.info("Completed vaccume seal");
				if(restart()) return true;
	
				// spawn task to cycle pump and maintain vacuum
				if(!propertyService.isSimulate()) {
					maintainVacuumThread =  new Thread(mintainVacuumRunable);
					maintainVacuumThread.setDaemon(true);
					maintainVacuumThread.start();
				}
			} else {
				smartThings.setDeviceState(deviceNames.getPumpCheck(), false);
				if(safeSleep(5*propertyService.getTimeMutiple())) return true;
				
			}

//			smartThings.setDeviceState(statusLight, false);
			
			if(!propertyService.isSimulate() && propertyService.isDoSelfTest() && selfTestOperation.run()) return true;
			
			// now wait until we time to start
			if(propertyService.isDelayedStart()) {
				logger.info("Waiting to start"); 
				startTimePoller.displayStartTime();
				startTimePoller.run(timer);
				if(restart()) return true;
			} else {
				// now wait for 15 seconds to make sure everything was ok
				if(safeSleep(15*propertyService.getTimeMutiple())) return true;
			}
			
			WatchDog.MaxTime fillMaxTime = null;
			try {
				if(propertyService.isControledFill()) {
					// the 300000 = 7 mins leayway
					fillMaxTime = watchDog.creatMaxTimer(390000 + (propertyService.getTimeMutiple() + propertyService.getFillRestFullFlow())  * propertyService.getFlowTime() , new WatchDogTimerExpired("Controled fill max time expired"), "Water Fill");
					if(controlledFill()) return true;
					if(propertyService.getFillRestFullFlow() != 0) {
						logger.info("On Full Flow for " + propertyService.getFillRestFullFlow() );
						if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getWaterValve(), true);
						safeSleep(propertyService.getFillRestFullFlow() * propertyService.getTimeMutiple());
						if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getWaterValve(), false);
					}
				} else {
					if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getWaterValve(), true);
					waterFillPoller.run(timer);
				}
				
			} finally {
				if(fillMaxTime != null) {
					watchDog.removeMaxTimer(fillMaxTime);
					fillMaxTime = null;
				}
				if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getWaterValve(), false);
			}
			
			if(propertyService.getFillRestTime() != 0) {
				logger.info("Completed water fill");
				logger.info("Resting after fill for " + propertyService.getFillRestTime());
				fillRestTimePoller.run(timer);
			}

			logger.info("Rest Complete");
			if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getWaterHeater(), false);

			logger.info("before test Here");

			if(restart()) {
				logger.info("Restarting");
				return true;
			}
			logger.info("Here");
			
			// do stim add 2 extra mins for leaway
			// WatchDog.MaxTime maxTimer = watchDog.creatMaxTimer(120000 + timeMutiple * runTime, new WatchDogTimerExpired("Stim max time expired"), "Max Stim");
			try {
//				if(randomTime) {
//					doStimThread =  randomStimThread;
//				} else {
//					doStimThread =  doStimRunable;
//				}
				
				logger.info("Starting stim cycles");
				
				doStimThread = theTeaseThread;
				doStimThread.run(timer);
			} finally {
				// watchDog.removeMaxTimer(maxTimer);
			}
			
		} catch(AbortException abortEx) {
			logger.info("Abort Received");
		} catch(InterruptedException iex) {
			logger.info("Interrupt Received");
		} finally {
			watchDog.removeTimer(timer);
			shutdown();
		}
		return restart();
	}

	public boolean controlledFill() throws InterruptedException {
		// sequence sum
		// t = n/2[2a + (n - 1)d]
		// n = number, d = slowDown, a = 1, t = total
		// t = total - flow - n*flowOffTIme 
		// n = flow/flowOnTime
		// d = (2t/n-2)/(n-1)
		
		if(propertyService.getTotalOnTime() == 0 || propertyService.getFlowOnTime() == 0) return false;
		
		long restTime = propertyService.getFlowOffTime()*propertyService.getTimeMutiple();
		long totalRunTime = propertyService.getInitalOnTime()*propertyService.getTimeMutiple();
		long totalTime = totalRunTime;

		long numberOfSegs = (propertyService.getTotalOnTime())/propertyService.getFlowOnTime();
		long totalSlowDown = (propertyService.getFlowTime() - propertyService.getTotalOnTime() - numberOfSegs*propertyService.getFlowOffTime())*propertyService.getTimeMutiple(); 
		long flowSlowDown = (2*totalSlowDown/numberOfSegs-2)/(numberOfSegs-1) ;
		numberOfSegs--;
		
		logger.info("Segs "+ numberOfSegs + " with slow down of " + ((float )flowSlowDown)/(float)propertyService.getTimeMutiple() + "s");
		
		try {
			logger.info("Doing fill.");
			
			if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getWaterValve(), true);
			if(!propertyService.isSimulate() && safeSleep(propertyService.getInitalOnTime()*propertyService.getTimeMutiple())) {
				logger.info("Restarting during a fill");
				if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getWaterValve(), false);
				if(restart()) return true;
			}
			restTime = propertyService.getFlowOffTime()*propertyService.getTimeMutiple() + flowSlowDown*numberOfSegs;
			logger.info("Resting (" + restTime + "ms)");
			if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getWaterValve(), false);

			while(totalRunTime < propertyService.getTotalOnTime()*propertyService.getTimeMutiple()) {
				if(!propertyService.isSimulate() && safeSleep(restTime)) {
					logger.info("Restarting during a fill");
					if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getWaterValve(), false);
					if(restart()) return true;
				}
				
				if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getWaterValve(), true);
				
				if(!propertyService.isSimulate() && safeSleep(propertyService.getFlowOnTime()*propertyService.getTimeMutiple())) {
					logger.info("Restarting during a fill");
					if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getWaterValve(), false);
					if(restart()) return true;
				}
				
				if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getWaterValve(), false);
				totalRunTime += propertyService.getFlowOnTime()*propertyService.getTimeMutiple();
				totalTime += propertyService.getFlowOnTime()*propertyService.getTimeMutiple() + restTime;
				restTime-=flowSlowDown;
				logger.info("Resting (" + ((float) restTime)/(float)propertyService.getTimeMutiple() + "s) total time " + ((float)totalTime)/(float)propertyService.getTimeMutiple() + "s with Run " + ((float)totalRunTime)/(float)propertyService.getTimeMutiple() + "s time");
			}
		} finally {
			logger.info("Fill Completed");
			if(!propertyService.isSimulate()) smartThings.setDeviceState(deviceNames.getWaterValve(), false);
		}
		return false;
	}
	
	private boolean restart() {
		int value = smartThings.getSwitchState(deviceNames.getStatusLight()); 
		return value != 0;
	}
	
	private void shutdown() throws Exception {
		logger.info("All done");
		boolean error;
		do {
			doStimThread = null;

			error = false;
			logger.info("Turning everything off retry");
			if(propertyService.isUseVaccume()) {
				// stop children
				if(maintainVacuumThread != null && maintainVacuumThread.isAlive()) {
					mintainVacuumRunable.shutdown();
				}
				maintainVacuumThread = null;

				try {
					smartThings.setDeviceState(deviceNames.getPumpSwitch(), false);
				} catch(Exception ex) {
					logger.error("Unable to turn off pump", ex);
					error = true;
				}

				if(propertyService.isUserWaterHeater()) {
					try {
						smartThings.setDeviceState(deviceNames.getWaterHeater(), false);
					} catch(Exception ex) {
						logger.error("Unable to turn off water heater", ex);
						error = true;
					}
				}

				try {
					watchDog.setSaftyValveState(false);
				} catch(Exception ex) {
					logger.error("Unable to turn off valve", ex);
					error = true;
				}
				
			}
			try {
				smartThings.setDeviceState(deviceNames.getNipplesSwitch(), false);
			} catch(Exception ex) {
				logger.error("Unable to turn off estim", ex);
				error = true;
			}
			try {
				smartThings.setDeviceState(deviceNames.getVibeSwitch(), false);
			} catch(Exception ex) {
				logger.error("Unable to turn off vibe", ex);
				error = true;
			}
			try {
				smartThings.setDeviceState(deviceNames.getProbeSwitch(), false);
			} catch(Exception ex) {
				logger.error("Unable to turn off probe", ex);
				error = true;
			}
			try {
				if(error) {
					systemTime.sleep(1000);
				}
			} catch(Exception ex) {
				logger.error("was not able to sleep for 1 second", ex);
				// if no other error then this does not matter
			}
		} while(error);
	}
	
	boolean safeSleep(long time) throws InterruptedException {
		long curTime = systemTime.currentTime();
		long endTIme =  curTime+ time;
		long timeLeft = endTIme-curTime; 
		
		while(timeLeft > 0) {
			timer.checkin();
			if(timeLeft > 5000) {
				systemTime.sleep(5000);
			} else {
				systemTime.sleep(timeLeft);
			}
			curTime = systemTime.currentTime();
			timeLeft = endTIme-curTime;  
			
			int value = smartThings.getSwitchState(deviceNames.getStatusLight());
			if(smartThings.isOn(value)) {
//				if(value >= 75) {
//					logger.info("Aborting");
//					throw new AbortException();
//				}
				logger.info("Restarting");
				return true;
			}
		}
		return false;
	}

}
