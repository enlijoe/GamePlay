package com.jgr.game.vac.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.exceptions.AbortException;
import com.jgr.game.vac.interfaces.FillRestTimePoller;
import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.LightOffPoller;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.PressureDevice;
import com.jgr.game.vac.interfaces.PumpOnPoller;
import com.jgr.game.vac.interfaces.SealCompletePoller;
import com.jgr.game.vac.interfaces.StartTimePoller;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.operations.Operation;
import com.jgr.game.vac.operations.TimedOperation;
import com.jgr.game.vac.stereotype.InjectDevice;
import com.jgr.game.vac.thread.MaintainVacuumThreadImpl;

public class MainLine {
	private Logger logger = LoggerFactory.getLogger(MainLine.class);
	
	@Autowired private RemoteWatchDog[] remoteWatchDogs;
	@Autowired private SealCompletePoller sealCompletePoller;
	@Autowired private SystemTime systemTime;
	@Autowired private PumpOnPoller pumpOnPoller;
	@Autowired private LightOffPoller lightOffPoller;
	@Autowired private MaintainVacuumThreadImpl mintainVacuumRunable;
	@Autowired private Operation waterFillOperation;
	@Autowired private StartTimePoller startTimePoller;
	@Autowired private FillRestTimePoller fillRestTimePoller;
	@Autowired private WatchDog watchDog;
	@Autowired private TimedOperation stimOperation;
	@Autowired private Operation selfTestOperation;
	
	@Value("${game.simulate}") private boolean simulate;
	@Value("${game.use.waterHeater}") private boolean useWaterHeater;
	@Value("${game.useVaccume}") private boolean useVaccume;
	@Value("${game.doSelfTest}") private boolean doSelfTest;
	@Value("${game.delayed}") private boolean delayedStart;
	@Value("${game.timeMutiple}") private long timeMutiple;
	@Value("${game.waterFillRest}") private long fillRestTime;
	@Value("${game.waterFillOn}") private boolean waterFillOn;
	
	@InjectDevice("${deviceUrl.status}") private InputDevice statusDevice;
	@InjectDevice("${deviceUrl.pumpCheck}") private OutputDevice pumpCheck;
	@InjectDevice("${deviceUrl.waterHeater}") private OutputDevice waterHeater;
	@InjectDevice("${deviceUrl.statusLightCheck}") private OutputDevice statusLightCheck;
	@InjectDevice("${deviceUrl.vaccumPressure}") private PressureDevice pressureDevice;
	
	private Thread maintainVacuumThread = null;

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

		try {
			setupInitalState();
			if(waitForStartSignal()) return true;
			if(startVacuum()) return true;
			if(selfTest()) return true;
			if(delayStart()) return true;
			if(waterFill()) return true;
			if(stimeCycle()) return true;
			
		} catch(AbortException abortEx) {
			logger.info("Abort Received");
		} catch(InterruptedException iex) {
			logger.info("Interrupt Received");
		} finally {
			pressureDevice.getOwner().setDeviceLogging(false);
			watchDog.removeTimer(timer);
			shutdown();
		}

		return statusDevice.isOn();
	}

	private void shutdown() throws Exception {
		logger.info("All done");

		if(useVaccume) {
			// stop children
			if(maintainVacuumThread != null && maintainVacuumThread.isAlive()) {
				mintainVacuumRunable.shutdown();
			}
			maintainVacuumThread = null;

		}
		for(RemoteWatchDog remoteWatchDog:remoteWatchDogs) {
			remoteWatchDog.reset();
			remoteWatchDog.disable();
		}
	}

	private boolean selfTest() throws InterruptedException {
		if(doSelfTest) {
			if(selfTestOperation.run(timer)) return true;
		}
		return false;
	}

	private boolean startVacuum() throws InterruptedException {
		if(useVaccume) {
			pressureDevice.getOwner().setDeviceLogging(true);
			logger.info("Waitting for vaccume seal");

			// now engage the pump until sealed
			sealCompletePoller.run(timer);
			logger.info("Completed vaccume seal");
			if(statusDevice.isOn()) return true;

			// spawn task to cycle pump and maintain vacuum
			if(!simulate) {
				maintainVacuumThread =  new Thread(mintainVacuumRunable);
				maintainVacuumThread.setDaemon(true);
				maintainVacuumThread.start();
			}
		} else {
			pumpCheck.setOff();
			if(systemTime.safeSleep(timer, 5*timeMutiple)) return true;
			
		}
		return false;
	}
	
	private boolean waitForStartSignal() throws InterruptedException {
		lightOffPoller.run(timer);
		logger.info("Light has been turned off");
		if(statusDevice.isOn()) return true;
		
		if(!simulate && useWaterHeater) {
			waterHeater.setOn();
		}
		
		// wait for the pump to turn on
		pumpOnPoller.run(timer);
		if(statusDevice.isOn()) return true;
		return false;
	}
	
	private void setupInitalState() {
		if(remoteWatchDogs == null || remoteWatchDogs.length == 0) {
			logger.error("There are no remote watchdogs registered.");
		}
		
		for(RemoteWatchDog remoteWatchDog:remoteWatchDogs) {
			logger.info("Callling reset for watch dog " + remoteWatchDog.getDescription());
			remoteWatchDog.reset();
			remoteWatchDog.enable();
		}
		
		statusLightCheck.setOn();
	}
	
	private boolean delayStart() throws InterruptedException {
		if(delayedStart) {
			logger.info("Waiting to start"); 
			startTimePoller.displayStartTime();
			startTimePoller.run(timer);
			if(statusDevice.isOn()) return true;
		} else {
			// now wait for 15 seconds to make sure everything was ok
			if(systemTime.safeSleep(timer, 15*timeMutiple)) return true;
		}

		return false;
	}
	
	private boolean waterFill() throws InterruptedException {
		if(waterFillOn) {
			try {
				pressureDevice.getOwner().setDeviceLogging(true);
				if(waterFillOperation.run(timer)) return true;
				
				if(fillRestTime != 0) {
					logger.info("Resting after fill for " + fillRestTime);
					fillRestTimePoller.run(timer);
				}
	
				logger.info("Rest Complete");
	
				if(statusDevice.isOn()) {
					return true;
				}
			} finally {
				if(!simulate) waterHeater.setOff();
			}
		}
		return false;
	}
	
	private boolean stimeCycle() throws InterruptedException {
		long maxRunTime = stimOperation.getMaxRunTime();
		WatchDog.MaxTime maxTimer = null;
		try {
			if(maxRunTime != 0) {
				maxTimer = watchDog.creatMaxTimer(maxRunTime, new WatchDogTimerExpired("Stim max time expired"), "Max Stim");
			}
			logger.info("Starting stim cycles");
			stimOperation.run(timer);
		} finally {
			if(maxTimer != null) {
				watchDog.removeMaxTimer(maxTimer);
			}
		}
		return false;
	}
}
