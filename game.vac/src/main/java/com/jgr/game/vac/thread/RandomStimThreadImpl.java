package com.jgr.game.vac.thread;

import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.stream.LongStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.DoStimThread;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceNames;
import com.jgr.game.vac.service.PropertyService;
import com.jgr.game.vac.service.WatchDog;


// TODO fixme for the new way of doing this look at DoStimThread
public class RandomStimThreadImpl implements DoStimThread {
	@Autowired private DeviceNames deviceNames;
	@Autowired private PropertyService propertyService;
	@Autowired private SmartThings smartThings;
	@Autowired private SystemTime systemTime;

	Thread me;
	Random rnd;
	LongStream longRandomNumbers;
	PrimitiveIterator.OfLong longItr;
	long startTime;
	long endTime;
	long[] abortWindows = null;
	int onWindow = 0;
	
	private Logger logger = LoggerFactory.getLogger(RandomStimThreadImpl.class);

	public RandomStimThreadImpl() {
		rnd = new Random(System.currentTimeMillis());
		LongStream longRandomNumbers = rnd.longs(900, 1100);
		longItr = longRandomNumbers.iterator();
		
	}
	
	@Override
	public void run(WatchDog.WatchTimer timer) {
		me = Thread.currentThread();
		me.setName("Do Stim");
		startTime = systemTime.currentTime();
		endTime = startTime + propertyService.getStimRunTime() * propertyService.getTimeMutiple();
		onWindow = 0;

		if(abortWindows== null && StringUtils.isNotEmpty(propertyService.getAbortWindowsString())) {
			String[] abortWindowsStringArray = StringUtils.split(propertyService.getAbortWindowsString());
			if(abortWindowsStringArray.length != 0) {
				abortWindows = new long[abortWindowsStringArray.length];
				for(int pos = 0; pos < abortWindowsStringArray.length; pos++) {
					abortWindows[pos] = Long.parseLong(abortWindowsStringArray[pos]) * propertyService.getTimeMutiple() * 60;
				}
			}
		}
		
		
		logger.info("Cycling Stim Started");
		try {
			while(true) {
				// turn on estim
				logger.info("Stim started");
				smartThings.setDeviceState(deviceNames.getNipplesSwitch(), true);
				if(delay(propertyService.getNippleOnTime())) {
					break;
				}
				// turn vibe on
				smartThings.setDeviceState(deviceNames.getVibeSwitch(), true);
				if(delay(propertyService.getVibeOnTime())) {
					break;
				}

				// turn both off
				smartThings.setDeviceState(deviceNames.getNipplesSwitch(), false);
				smartThings.setDeviceState(deviceNames.getVibeSwitch(), false);

				// don't start next cycle if not enough time left
				if(endTime < System.currentTimeMillis() + (propertyService.getStimRestTime() + propertyService.getNippleOnTime() + propertyService.getVibeOnTime()) * propertyService.getTimeMutiple()) {
					break;
				}
				
				// rest time
				logger.info("Stim Cycle Copmplete");
				if(delay(propertyService.getStimRestTime())) {
					break;
				}
			}
			
			return;
		} catch (InterruptedException iex) {
			logger.info("Stim interrupted");
		} finally {
			logger.info("Cycling Stim Ended");
		}
		
	}
	
	boolean lastAbortWindow = false;
	
	private boolean delay(long time) throws InterruptedException {
		long rndNum = longItr.nextLong() ;
		long delayTime = ((time * rndNum * propertyService.getTimeMutiple()) / 1000);
		
		long endTime = delayTime + systemTime.currentTime();
		
		// verify our end time
		while(System.currentTimeMillis() < endTime) {
			systemTime.sleep(1000);

			if(abortWindows != null && onWindow < abortWindows.length) {
				boolean allowAbort = false;
				if(startTime + abortWindows[onWindow] < System.currentTimeMillis()) {
					if(lastAbortWindow != true) {
						logger.info("Abort window open");
						lastAbortWindow = true;
					}
					allowAbort = true;
					if(onWindow+1 < abortWindows.length) {
						if(startTime + abortWindows[onWindow+1] < System.currentTimeMillis()) {
							if(lastAbortWindow != false) {
								logger.info("Abort window closed");
								lastAbortWindow = false;
							}
							
							// end window
							onWindow+=2;
							allowAbort = false;
						}
					}
				}
				if(allowAbort) {
					if(smartThings.isOn(smartThings.getSwitchState(deviceNames.getStatusLight()))) {
						return true;
					}
				}
			}
		}

		return false;
	}
	
}
