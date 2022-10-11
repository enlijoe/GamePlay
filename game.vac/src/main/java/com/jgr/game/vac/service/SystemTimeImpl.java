package com.jgr.game.vac.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.SystemTime;

public class SystemTimeImpl implements SystemTime {
	@Autowired private DeviceMapperService deviceMapperService;

	@Value("${game.timeMutiple}") private long timeMutiple;
	
	private Logger logger = LoggerFactory.getLogger(MainLine.class);

	@Value("${deviceUrl.status}") private String statusDeviceUrl;
	
	private InputDevice statusDevice;
	
	@PostConstruct
	public void afterPropsSet() {
		statusDevice = deviceMapperService.getDevice(new DeviceUrl(statusDeviceUrl));
	}
	
	@Override
	public long currentTime() {
		return System.currentTimeMillis();
	}
	
	@Override
	public void sleep(long time) throws InterruptedException {
		Thread.sleep(time);
	}

	@Override
	public void sleep(WatchDog.WatchTimer timer, long time) throws InterruptedException {
		long curTime = currentTime();
		long endTIme =  curTime + time;
		long timeLeft = endTIme-curTime; 
		
		while(timeLeft > 0) {
			timer.checkin();
			if(timeLeft > 5 * timeMutiple) {
				sleep(5 * timeMutiple);
			} else {
				sleep(timeLeft);
			}
			curTime = currentTime();
			timeLeft = endTIme-curTime;  
		}
	}
	
	public boolean safeSleep(WatchDog.WatchTimer timer, long time) throws InterruptedException {
		long curTime = currentTime();
		long endTIme =  curTime+ time;
		long timeLeft = endTIme-curTime; 
		
		while(timeLeft > 0) {
			timer.checkin();
			if(timeLeft > 5000) {
				sleep(5000);
			} else {
				sleep(timeLeft);
			}
			curTime = currentTime();
			timeLeft = endTIme-curTime;  
			
			if(statusDevice.isOn()) {
				logger.info("Restarting");
				return true;
			}
		}
		return false;
	}
	
}
