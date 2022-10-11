package com.jgr.game.vac.operations;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.PressureDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;
import com.jgr.game.vac.service.WatchDog.WatchTimer;

public class AccelerationControledWaterFIll implements Operation {

	@Autowired SystemTime systemTime;
	@Autowired private DeviceMapperService deviceMapperService;
	
	
	/*
	 * 1.42psi/m or .00142psi/mm
	 * radius 1 = 26mm length = 160mm
	 * radius 2 = 32.5mm length = 460mm
	 * 1,000mm^3 = 1ml
	 * 
	 */
	
	@Value("${deviceUrl.externalPressure}") private String sourcePressureUrl;
	@Value("${deviceUrl.internalPressure}") private String insidePressureUrl;
	@Value("${deviceUrl.waterValve}") private String waterSwitchUrl;
	
	
	PressureDevice sourcePressure;
	PressureDevice insidePressure;
	OutputDevice waterSwitch;

	long timeMutiple;
	long maxVolumeFill;
	double maxPressure;
	double maxFillRate;
	long maxSegmentFlowTime;
	long totalFillTime;
	long poolingInterval;
	long volumeResolution;
	double volumePerPressure;
	double resolution = 0.0061039;		// psi
	double psi_mm = .00142;				// psi/mm (a constant)
	double volume1Ratio = .000668639; 	// psi/ml (9.1288ml resolution)
	double volume1Height = 160.0;		// mm (max 0.2272psi)
	double volume2Ratio = .000427929;	// psi/ml (14.238ml resolution)
	double volume2Height = 460.0;		// mm (max 0.8804psi)
	double volume3Ratio = 0.000213965;	// psi/ml (
	double volume3Height = 0.0;			// mm
	
	@PostConstruct
	public void afterPropsSet() {
		sourcePressure = deviceMapperService.getDevice(new DeviceUrl(sourcePressureUrl));
		insidePressure = deviceMapperService.getDevice(new DeviceUrl(insidePressureUrl));
		waterSwitch = deviceMapperService.getDevice(new DeviceUrl(waterSwitchUrl));
	}
	
	// Accel = const
	// FillRate = Accel*time+FillRateInit
	// VolumeFilled = time^2/2*Accel+FillRateInit*time+VolumeFilledInitial
	@Override
	public boolean run(WatchTimer timer) throws InterruptedException {
		long timeLeft = totalFillTime;
		long totalVolumeFlowed = 0;
		long fillingStarted = systemTime.currentTime();
		double beginSourcePressure = sourcePressure.readValue();
		double fillRateAcceleration = 2*maxVolumeFill/Math.pow(totalFillTime, 2.0);			
		
		while(timeLeft >= 0) {
			// double expectedMaxFillRate = 2*FillRateAcceleration*timeLeft;
			// turn on water for the max filling time or max pressure which ever comes first
			waterSwitch.setValue(1);
			long flowSegmentStarted = systemTime.currentTime(); 
			while(flowSegmentStarted - systemTime.currentTime() < maxSegmentFlowTime * timeMutiple) {
				// check pressure if to high then break;
				if(maxPressure > 0 && insidePressure.readValue() > maxPressure) {
					break;
				}
				systemTime.safeSleep(timer, poolingInterval*timeMutiple);
			}
			waterSwitch.setValue(0);
			// turnOffWater

			// figure out how much just flowed and total amount flowed
			double currentSourcePressure=0;
			long lastVolumeFlowed = totalVolumeFlowed;
		
			totalVolumeFlowed = (long) ((beginSourcePressure - currentSourcePressure)*volumePerPressure);
			
			// if we can't detect how much flowed then we assume at least 1/2 of the min amount we can detect has flowed (any error in this will get corrected latter)
			if(lastVolumeFlowed == totalVolumeFlowed) {
				totalVolumeFlowed+=volumeResolution/2;
			}
			
			long neededFlowTime =  (long) Math.sqrt(2*totalVolumeFlowed/fillRateAcceleration);
			
			// figure out how long to wait for to make our average flow rate
			long sleepTime = neededFlowTime - (systemTime.currentTime()-fillingStarted);
			
			if(sleepTime > 0) {
				systemTime.safeSleep(timer, sleepTime*timeMutiple);
			}
		}
		return false;
	}
	

}
