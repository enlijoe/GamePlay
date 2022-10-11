package com.jgr.game.vac.operations;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;
import com.jgr.game.vac.service.WatchDog;
import com.jgr.game.vac.service.WatchDog.WatchTimer;

public class TimedControledWaterFill implements Operation {
	private Logger logger = LoggerFactory.getLogger(TimedControledWaterFill.class);

	@Autowired private DeviceMapperService deviceMapperService;
	@Autowired private SystemTime systemTime;
	@Autowired private WatchDog watchDog;
	
	@Value("${game.waterFillTime}") private long totalOnTime;
	@Value("${game.waterFill.onTime}") private long flowOnTime;
	@Value("${game.waterFill.offTIme}") private long flowOffTime;
	@Value("${game.timeMutiple}") private long timeMutiple;
	@Value("${game.waterFill.initalOnTime}") private long initalOnTime;
	@Value("${game.waterFill.flowTime}") private long flowTime;
	@Value("${game.simulate}") private boolean simulate;
	@Value("${game.fillRestFullFlow}") private long fillRestFullFlow;
	
	@Value("${deviceUrl.status}") private String statusDeviceUrl;
	@Value("${deviceUrl.waterValve}") private String waterValveUrl;

	private InputDevice statusDevice;
	private OutputDevice waterValve;

	WatchDog.MaxTime fillMaxTime = null;
	
	class WatchDogTimerExpired implements Runnable {
		private Thread myThread;
		
		WatchDogTimerExpired() {
			myThread = Thread.currentThread();
		}
		
		@Override
		public void run() {
			logger.error("!!!!!! Fill timer expired. !!!!!!!");
			myThread.interrupt();
		}
	}
	
	
	@PostConstruct
	public void afterPropsSet() {
		statusDevice = deviceMapperService.getDevice(new DeviceUrl(statusDeviceUrl));
		waterValve = deviceMapperService.getDevice(new DeviceUrl(waterValveUrl));
	}
	
	@Override
	public boolean run(WatchTimer timer) throws InterruptedException {
		boolean retVal = false;
		
		fillMaxTime = watchDog.creatMaxTimer(390000 + (flowTime + fillRestFullFlow) * timeMutiple, new WatchDogTimerExpired(), "Water Fill");
		// sequence sum
		// t = n/2[2a + (n - 1)d]
		// n = number, d = slowDown, a = 1, t = total
		// t = total - flow - n*flowOffTIme 
		// n = flow/flowOnTime
		// d = (2t/n-2)/(n-1)
		
		if(totalOnTime == 0 || flowOnTime == 0) return false;
		
		long restTime = flowOffTime*timeMutiple;
		long totalRunTime = initalOnTime*timeMutiple;
		long totalTime = totalRunTime;

		long numberOfSegs = (totalOnTime)/flowOnTime;
		long totalSlowDown = (flowTime - totalOnTime - numberOfSegs*flowOffTime)*timeMutiple; 
		long flowSlowDown = (2*totalSlowDown/numberOfSegs-2)/(numberOfSegs-1) ;
		numberOfSegs--;
		
		logger.info("Segs "+ numberOfSegs + " with slow down of " + ((float )flowSlowDown)/(float)timeMutiple + "s");
		
		try {
			logger.info("Doing fill.");
			
			
			
			if(!simulate) waterValve.setOn();
			if(!simulate && systemTime.safeSleep(timer, initalOnTime*timeMutiple)) {
				logger.info("Restarting during a fill");
				if(!simulate) waterValve.setOff();
				if(statusDevice.isOn()) return true;
			}
			restTime = flowOffTime*timeMutiple + flowSlowDown*numberOfSegs;
			logger.info("Resting (" + restTime + "ms)");
			if(!simulate) waterValve.setOff();

			while(totalRunTime < totalOnTime*timeMutiple) {
				if(!simulate && systemTime.safeSleep(timer, restTime)) {
					logger.info("Restarting during a fill");
					if(!simulate) waterValve.setOff();
					if(statusDevice.isOn()) return true;
				}
				
				if(!simulate) waterValve.setOn();
				
				if(!simulate && systemTime.safeSleep(timer, flowOnTime*timeMutiple)) {
					logger.info("Restarting during a fill");
					if(!simulate) waterValve.setOff();
					if(statusDevice.isOn()) return true;
				}
				
				if(!simulate) waterValve.setOff();
				totalRunTime += flowOnTime*timeMutiple;
				totalTime += flowOnTime*timeMutiple + restTime;
				restTime-=flowSlowDown;
				logger.info("Resting (" + ((float) restTime)/(float)timeMutiple + "s) total time " + ((float)totalTime)/(float)timeMutiple + "s with Run " + ((float)totalRunTime)/(float)timeMutiple + "s time");
			}

			if(fillRestFullFlow != 0) {
				logger.info("On Full Flow for " + fillRestFullFlow );
				if(!simulate) waterValve.setOn();
				systemTime.safeSleep(timer, fillRestFullFlow * timeMutiple);
				if(!simulate) waterValve.setOff();
			}

		} finally {
			logger.info("Fill Completed");
			if(!simulate) waterValve.setOff();
			retVal = fillMaxTime.isExpired();
			watchDog.removeMaxTimer(fillMaxTime);
		}

		return retVal;
	}
}
