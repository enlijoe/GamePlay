package com.jgr.game.vac.thread;

import java.io.PrintStream;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;

import com.jgr.game.vac.interfaces.PressureSensor;
import com.jgr.game.vac.interfaces.SystemTime;

public class PressureMonitor  implements Runnable {
	@Autowired PressureSensor externalPressure;
	@Autowired PressureSensor internalPressure;
	@Autowired private SystemTime systemTime;
	
	private class PressureStats {
		long highPressure = 0;
		long lowPressure = Long.MAX_VALUE;
		// long normalPressure = 0;
		long currentPressure = 0;
		String device;
		PressureSensor sensor;
		
		PressureStats(PressureSensor sensor, String device) {
			this.device = device;
			this.sensor = sensor;
		}
		
		void calcNormalPressure() {
			
		}
		
		void update() {
			long reading = sensor.getPressure(device);
			currentPressure = reading;
			if(highPressure < reading) {
				highPressure = reading;
			}
			
			if(lowPressure > reading ) {
				lowPressure = reading;
			}
		}
	}
	
	String[] internalDevices = {"inside", "outside", "bowel", "bladder"};
	String[] externalDevices = {"bowel", "bladder"};
	
	ArrayList<PressureStats> internalPressureStats = null;
	ArrayList<PressureStats> externalPressureStats = null;
	PrintStream output;
	
	public void calcInternalNorm() {
		calcNorm(internalPressureStats);
	}
	
	public void calcExternalNorm() {
		calcNorm(externalPressureStats);
	}
	
	private void calcNorm(ArrayList<PressureStats> stats) {
		for(PressureStats stat:stats) {
			stat.calcNormalPressure();
		}
	}
	
	@Override
	public void run() {
		
		internalPressureStats = buildStats(internalPressure, internalDevices);
		externalPressureStats = buildStats(externalPressure, externalDevices);
		
		try {
			while(true) {
				updatePressure(internalPressureStats);
				updatePressure(externalPressureStats);
				output.print('\n');
				systemTime.sleep(100);
			}
		} catch(Exception ex) {
			
		}
	}
	
	ArrayList<PressureStats> buildStats(PressureSensor sensor, String[] devices) {
		ArrayList<PressureStats> retVal = new ArrayList<PressureMonitor.PressureStats>();
		
		for(String device:devices) {
			retVal.add(new PressureStats(sensor, device));
		}
		return retVal;
	}
	
	void updatePressure(ArrayList<PressureStats> stats) {
		for(PressureStats stat:stats) {
			stat.update();
			if(output != null) {
				output.print(stat.currentPressure);
				output.print('\t');
			}
		}
	}
}
