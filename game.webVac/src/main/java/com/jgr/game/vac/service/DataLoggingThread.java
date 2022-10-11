package com.jgr.game.vac.service;

import java.util.List;

import com.jgr.game.vac.entity.PressureData;

public interface DataLoggingThread {
	public List<PressureData>[] getCache();
	void shutdown();
	public long getTimeEnded();
	public boolean isRunning();
	public long getTimeStarted();
}
