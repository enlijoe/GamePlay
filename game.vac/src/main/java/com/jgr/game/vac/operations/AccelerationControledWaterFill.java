package com.jgr.game.vac.operations;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.PressureDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.WatchDog.WatchTimer;
import com.jgr.game.vac.stereotype.InjectDevice;

/*
 * 1.42psi/m or .00142psi/mm
 * radius 1 = 35mm length = 160mm
 * radius 2 = 32.5mm length = 460mm
 * 1,000mm^3 = 1ml
 * 
 */

public class AccelerationControledWaterFill implements Operation {
	private Logger logger = LoggerFactory.getLogger(AccelerationControledWaterFill.class);

	@Autowired private SystemTime systemTime;
	
	@Value("${game.timeMutiple:1000}") private long timeMutiple;
	@Value("${game.waterFill.flowTime:3600}") private long flowTime;
	@Value("${game.waterFill.onTime:15}") private long flowOnTime;
	@Value("${game.water.volume:1300}") private long maxVolumeFill;
	@Value("${game.water.pressure.minRestTime:5}") private long minRestTime;
	@Value("${game.water.maxPressure:0}") private float maxPressure;
	@Value("${game.water.poolingInterval:1000}") private long poolingInterval;
	@Value("${game.water.pressure.heightOffset:118}") private long heightOffset;			// mm (a constant)
	@Value("${game.water.pressure.psi_mm:.00142}") private float psi_mm;					// psi/mm (a constant)
	@Value("${game.water.pressure.volume1Ratio:2.3000}") private float volume1Ratio; 		// mm/ml (9.1288ml resolution)
	@Value("${game.water.pressure.volume1Height:180}") private long volume1Height;			// mm (max 0.2272psi)
	@Value("${game.water.pressure.volume2Ratio:3.2485}") private float volume2Ratio;		// mm/ml (14.238ml resolution)
	@Value("${game.water.pressure.volume2Height:460}") private long volume2Height;			// mm (max 0.8804psi)
	@Value("${game.water.pressure.volume3Ratio:6.497}") private float volume3Ratio;			// mm/ml (
	@Value("${game.water.pressure.volume3Height:0}") private long volume3Height;			// mm
	
	@InjectDevice("${deviceUrl.externalPressure}") private PressureDevice sourcePressure;
	@InjectDevice("${deviceUrl.internalPressure}") private PressureDevice insidePressure;
	@InjectDevice("${deviceUrl.waterValve}") private OutputDevice waterSwitch;

	private static class VolumeData {
		float ratio;	// mm/ml  allows to turn a height into a volume fast
		long height;	// height of this section 0 if unused
		
		VolumeData(float ratio, long height) {
			this.ratio = ratio;		
			this.height = height;	
		}
	}

	private VolumeData[] volumeData;
	
	@PostConstruct
	public void afterPropsSet() {
		volumeData = new VolumeData[] {new VolumeData(volume1Ratio, volume1Height), 
									   new VolumeData(volume2Ratio, volume2Height), 
									   new VolumeData(volume3Ratio, volume3Height)};
	}
	
	// Accel = const
	// FillRate = Accel*time
	// VolumeFilled = .5*time^2*Accel
	@Override
	public boolean run(WatchTimer timer) throws InterruptedException {
		long fillingStarted = systemTime.currentTime();
		float beginSourcePressure = sourcePressure.readValue(); // 2.263183594
		long requestedVolume = maxVolumeFill;
		long actualVolume = calVolumeLeft(beginSourcePressure, heightOffset);
		
		// if there is not enough water to use then adjust how much will flow to be how much we have
		if(requestedVolume > actualVolume) {
			requestedVolume = actualVolume;
		}
		
		float fillRateAcceleration = (float) (2*requestedVolume/Math.pow(flowTime, 2.0));		// .0002161   0.0001029
		logger.info("Flow accel is " + fillRateAcceleration);
		
		logger.info("Volume requested " + maxVolumeFill + " volume available " + actualVolume);
		while(systemTime.currentTime() - fillingStarted < flowTime*timeMutiple) {
			// double expectedMaxFillRate = 2*FillRateAcceleration*timeLeft;
			// turn on water for the max filling time or max pressure which ever comes first
			logger.info("Flow stated for " + flowOnTime + " seconds.");
			long flowSegmentStarted = systemTime.currentTime();
			try {
				waterSwitch.setOn();
				while(systemTime.currentTime() - flowSegmentStarted < flowOnTime * timeMutiple) {
					if(systemTime.currentTime() - fillingStarted > flowTime*timeMutiple) break;
					// check pressure if to high then break;
					float pressureValue = insidePressure.readValue();
					if(maxPressure > 0 && pressureValue > maxPressure) {
						logger.info("Max pressure exceeded " + pressureValue);
						break;
					}
					if(systemTime.safeSleep(timer, poolingInterval)) {
						logger.info("Abort received");
						return true;
					}
				}
			} finally {
				waterSwitch.setOff();
			}
			logger.info("Flow Stopped after " + (systemTime.currentTime()-flowSegmentStarted)/timeMutiple + "seconds.");
			// after every flow we must sleep for this amount of time 
			// this allows the pressure to stabilize and also gives us a min amount of time to rest
			// note that the total time in here can be also extends by this amount so keep small
			if(systemTime.safeSleep(timer, minRestTime * timeMutiple)) {
				return true;
			}
			
			// sleep until the volume flowed is correct for the time passed
			float currentPressure = sourcePressure.readValue();
			float volumeFlowed = calVolumeUsed(currentPressure, heightOffset, actualVolume);
			if(volumeFlowed >= requestedVolume) {
				logger.info(volumeFlowed + "ml exceeded requested volume of " + requestedVolume + "ml.  Done filling with " + calVolumeLeft(currentPressure, heightOffset) +"ml left.");
				
				break;
			}
			logger.info("Total flow of " + volumeFlowed + "ml flow, expected " + calVolumeNeeded(fillingStarted, fillRateAcceleration) + " ml.");
			while(calVolumeNeeded(fillingStarted, fillRateAcceleration) < volumeFlowed) {
				if(systemTime.currentTime() - fillingStarted > flowTime*timeMutiple) break;
				if(systemTime.safeSleep(timer, poolingInterval)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private long calVolumeLeft(float currentPressure, long bottomHeight) {
		long currentHeight = (long) (currentPressure/psi_mm) - bottomHeight;
		long sectionHeight = 0;
		long sectionVolume = 0;
		
		for(VolumeData section:volumeData) {
			if(section.height > 0) {
				if(sectionHeight+section.height < currentHeight) {
					long onVolume = (long) (section.height*section.ratio);
					sectionVolume+=onVolume;
					sectionHeight+=section.height;
				} else {
					long onVolume = (long) ((currentHeight-sectionHeight)*section.ratio);
					sectionVolume+=onVolume;
					break;
				}
			}
		}
		return sectionVolume;
	}
	
	private long calVolumeUsed(float currentPressure, long bottomHeight, long beginVolume) {
		return beginVolume - calVolumeLeft(currentPressure, bottomHeight);
	}
	
	/**
	 * Calculate the volume in ml that should have flowed at this point in time from the starting time
	 * @param startTime
	 * @param fillRateAcceleration
	 * @return
	 */
	private long calVolumeNeeded(long startTime, float fillRateAcceleration) {
		long time = (systemTime.currentTime()-startTime)/1000;
		long neededFlow = (long) (0.5*fillRateAcceleration*Math.pow(time,2));
		return neededFlow;
	}
	
	public void test() {
		float currentPressure = (float) 2.2575378;

		calVolumeUsed(currentPressure, heightOffset, maxVolumeFill);
	}
}
