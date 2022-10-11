package com.jgr.game.vac.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.jgr.game.vac.dao.PressureDataDao;
import com.jgr.game.vac.entity.PressureData;
import com.jgr.game.vac.entity.PressureDataPk;
import com.jgr.game.vac.service.DataLoggingThread;
import com.jgr.game.vac.service.PressureDevice;
import com.jgr.game.vac.util.CircularCache;

@Service
@Scope("prototype")
public class DataLoggingThreadImpl extends Thread implements DataLoggingThread {
	private @Autowired PressureDataDao pressureDataDao;
	
	private List<PressureDevice> devices;
	private long logInterval;
	private long maxLogTime;
	private long timeStarted;
	private long timeEnded;
	private boolean running = false;
	private boolean shutdown = false;
	private CircularCache<List<PressureData>> cache;
	
	public DataLoggingThreadImpl(int cacheSize) {
		if(cacheSize != 0) { 
			cache = new CircularCache<>(cacheSize);
		} else {
			cache = null;
		}
	}
	
	public List<PressureData>[] getCache() {
		if(cache != null) {
			return cache.getCache();
		}
		
		return null;
	}

	public boolean isRunning() {
		return running;
	}
	
	@Override
	public void run() {
		if(devices == null || devices.isEmpty()) {
			throw new IllegalStateException("at least one device must be defined");
		}
		if(logInterval == 0) {
			throw new IllegalStateException("log interval  must be defined");
		}
		if(maxLogTime == 0) {
			throw new IllegalStateException("max log time must be defined");
		}
		
		running = true;
		timeStarted = System.currentTimeMillis();
		while(!shutdown && timeStarted + maxLogTime > System.currentTimeMillis()) {
				try {
					List<PressureData> dataList = null;
					if(cache != null) {
						dataList = new ArrayList<PressureData>();
						cache.add(dataList);
					}
					for(PressureDevice device:devices) {
						PressureDataPk pk = new PressureDataPk(System.currentTimeMillis(), device.getBeanName());
						PressureData data = new PressureData(pk, device.getValue());
						pressureDataDao.save(data);
						if(cache != null) {
							dataList.add(data);
						}
					}
				sleep(logInterval);
			} catch (InterruptedException e) {
				// this can be ignored we are just being woken up
			}
		}
		timeEnded = System.currentTimeMillis();
	}
	
	public void shutdown() {
		if(running) {
			shutdown = true;
			interrupt();
		}
	}
	
	public long getTimeStarted() {
		return timeStarted;
	}
	
	public long getTimeEnded() {
		return timeEnded;
	}

	public void setDevice(List<PressureDevice> devices) {
		this.devices = devices;
	}

	public void setLogInterval(long logInterval) {
		this.logInterval = logInterval;
	}

	public void setMaxLogTime(long maxLogTime) {
		this.maxLogTime = maxLogTime;
	}
	
}