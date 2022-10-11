package com.jgr.game.vac.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.exceptions.AbortException;
import com.jgr.game.vac.interfaces.DoStimThread;
import com.jgr.game.vac.interfaces.FillRestTimePoller;
import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.LightOffPoller;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.PumpOnPoller;
import com.jgr.game.vac.interfaces.SealCompletePoller;
import com.jgr.game.vac.interfaces.StartTimePoller;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.operations.Operation;
import com.jgr.game.vac.thread.MaintainVacuumThreadImpl;
import com.jgr.game.vac.thread.TheTeaseThreadImpl;

public class MainLine {
	private Logger logger = LoggerFactory.getLogger(MainLine.class);
	
	@Autowired private DeviceMapperService deviceMapperService;
	@Autowired private SealCompletePoller sealCompletePoller;
	@Autowired private SystemTime systemTime;
	@Autowired private PumpOnPoller pumpOnPoller;
	@Autowired private LightOffPoller lightOffPoller;
	@Autowired private MaintainVacuumThreadImpl mintainVacuumRunable;
//	@Autowired private DoStimThread doStimRunable;
//	@Autowired private DoStimThread randomStimThread;
	@Autowired private Operation waterFillOperation;
	@Autowired private StartTimePoller startTimePoller;
	@Autowired private FillRestTimePoller fillRestTimePoller;
	@Autowired private WatchDog watchDog;
	@Autowired private TheTeaseThreadImpl theTeaseThread;
	@Autowired private Operation selfTestOperation;
	
	@Value("${game.simulate}") private boolean simulate;
	@Value("${game.use.waterHeater}") private boolean useWaterHeater;
	@Value("${game.useVaccume}") private boolean useVaccume;
	@Value("${game.doSelfTest}") private boolean doSelfTest;
	@Value("${game.delayed}") private boolean delayedStart;
	@Value("${game.timeMutiple}") private long timeMutiple;
	@Value("${game.waterFillRest}") private long fillRestTime;
	
	@Value("${deviceUrl.status}") private String statusDeviceUrl;
	@Value("${deviceUrl.pumpSwitch}") private String pumpSwitchUrl;
	@Value("${deviceUrl.pumpCheck}") private String pumpCheckUrl;
	@Value("${deviceUrl.nipplesSwitch}") private String nipplesSwitchUrl;
	@Value("${deviceUrl.probeSwitch}") private String probeSwitchUrl;
	@Value("${deviceUrl.vibeSwitch}") private String vibeSwitchUrl;
	@Value("${deviceUrl.waterHeater}") private String waterHeaterUrl;
	@Value("${deviceUrl.statusLightCheck}") private String statusLightCheckUrl;

	private InputDevice statusDevice;
	private OutputDevice pumpSwitch;
	private OutputDevice pumpCheck;
	private OutputDevice nipplesSwitch;
	private OutputDevice probeSwitch;
	private OutputDevice vibeSwitch;
	private OutputDevice waterHeater;
	private OutputDevice statusLightCheck;
	
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
	
	@PostConstruct
	public void afterPropsSet() {
		statusDevice = deviceMapperService.getDevice(new DeviceUrl(statusDeviceUrl));
		pumpSwitch = deviceMapperService.getDevice(new DeviceUrl(pumpSwitchUrl));
		pumpCheck = deviceMapperService.getDevice(new DeviceUrl(pumpCheckUrl));
		nipplesSwitch = deviceMapperService.getDevice(new DeviceUrl(nipplesSwitchUrl));
		probeSwitch = deviceMapperService.getDevice(new DeviceUrl(probeSwitchUrl));
		vibeSwitch = deviceMapperService.getDevice(new DeviceUrl(vibeSwitchUrl));
		waterHeater = deviceMapperService.getDevice(new DeviceUrl(waterHeaterUrl));
		statusLightCheck = deviceMapperService.getDevice(new DeviceUrl(statusLightCheckUrl));
	}
	
	public boolean runProgram() throws Exception {
		timer = watchDog.addTimer(new WatchDogTimerExpired("Watch Dog expired for Running program"), "Main process");

		// insure a known state
		try {
			pumpSwitch.setOff();
			pumpCheck.setOff();
			nipplesSwitch.setOff();
			probeSwitch.setOff();
			vibeSwitch.setOff();
			waterHeater.setOff();
			
			statusLightCheck.setOn();
			
			// now wait for it to turn off
			lightOffPoller.run(timer);
			logger.info("Light has been turned off");
			if(statusDevice.isOn()) return true;
			
			if(!simulate && useWaterHeater) {
				waterHeater.setOn();
			}
			
			// wait for the pump to turn on
			pumpOnPoller.run(timer);
			if(statusDevice.isOn()) return true;
			
			if(useVaccume) {
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

			
			if(!simulate && doSelfTest && selfTestOperation.run(timer)) return true;
			
			// now wait until we time to start
			if(delayedStart) {
				logger.info("Waiting to start"); 
				startTimePoller.displayStartTime();
				startTimePoller.run(timer);
				if(statusDevice.isOn()) return true;
			} else {
				// now wait for 15 seconds to make sure everything was ok
				if(systemTime.safeSleep(timer, 15*timeMutiple)) return true;
			}
			
			if(waterFillOperation.run(timer)) return true;
			
			if(fillRestTime != 0) {
				logger.info("Resting after fill for " + fillRestTime);
				fillRestTimePoller.run(timer);
			}

			logger.info("Rest Complete");
			if(!simulate) waterHeater.setOff();

			if(statusDevice.isOn()) {
				logger.info("statusDevice.isOning");
				return true;
			}
			logger.info("Here");
			
			// do stim add 2 extra mins for leaway
			// TODO - create these as operations and have it we don't decide which one to do here.  Also move the maxTimer into the operation
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
		return statusDevice.isOn();
	}

	private void shutdown() throws Exception {
		logger.info("All done");
		boolean error;
		do {
			doStimThread = null;

			error = false;
			logger.info("Turning everything off retry");
			if(useVaccume) {
				// stop children
				if(maintainVacuumThread != null && maintainVacuumThread.isAlive()) {
					mintainVacuumRunable.shutdown();
				}
				maintainVacuumThread = null;

				try {
					pumpSwitch.setOff();
				} catch(Exception ex) {
					logger.error("Unable to turn off pump", ex);
					error = true;
				}

				if(useWaterHeater) {
					try {
						waterHeater.setOff();
					} catch(Exception ex) {
						logger.error("Unable to turn off water heater", ex);
						error = true;
					}
				}
			}
			try {
				nipplesSwitch.setOff();
			} catch(Exception ex) {
				logger.error("Unable to turn off estim", ex);
				error = true;
			}
			try {
				vibeSwitch.setOff();
			} catch(Exception ex) {
				logger.error("Unable to turn off vibe", ex);
				error = true;
			}
			try {
				probeSwitch.setOff();
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
}
