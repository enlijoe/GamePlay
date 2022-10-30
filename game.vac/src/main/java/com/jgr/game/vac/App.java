package com.jgr.game.vac;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.jgr.game.vac.interfaces.FillRestTimePoller;
import com.jgr.game.vac.interfaces.LightOffPoller;
import com.jgr.game.vac.interfaces.LightOnPoller;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.PressureDevice;
import com.jgr.game.vac.interfaces.PressureDevice.TransferFn;
import com.jgr.game.vac.interfaces.PumpOnPoller;
import com.jgr.game.vac.interfaces.SealCompletePoller;
import com.jgr.game.vac.interfaces.StartTimePoller;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.interfaces.VaccumPoller;
import com.jgr.game.vac.operations.AccelerationControledWaterFill;
import com.jgr.game.vac.operations.Operation;
import com.jgr.game.vac.operations.SelfTestOperation;
import com.jgr.game.vac.operations.TimedOperation;
import com.jgr.game.vac.poller.FillRestTimePollerImpl;
import com.jgr.game.vac.poller.LightOffPollerImpl;
import com.jgr.game.vac.poller.LightOnPollerImpl;
import com.jgr.game.vac.poller.PumpOnPollerImpl;
import com.jgr.game.vac.poller.SealCompletePollerImpl;
import com.jgr.game.vac.poller.StartTimePollerImpl;
import com.jgr.game.vac.poller.VaccumPollerImpl;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;
import com.jgr.game.vac.service.MainLine;
import com.jgr.game.vac.service.SleepMode;
import com.jgr.game.vac.service.SmartThingsDeviceMgrImpl;
import com.jgr.game.vac.service.SystemTimeImpl;
import com.jgr.game.vac.service.WatchDog;
import com.jgr.game.vac.service.esp32.GenericEsp32DeviceFactory;
import com.jgr.game.vac.service.esp32.HoneywellAbsPressureTransferFn;
import com.jgr.game.vac.service.impl.DeviceMapperServiceImpl;
import com.jgr.game.vac.thread.DoStimThreadImpl;
import com.jgr.game.vac.thread.MaintainVacuumThreadImpl;
import com.jgr.game.vac.thread.TheTeaseThreadImpl;


// ServiceTaskJavaDelegateActivityBehavior

@SpringBootApplication
public class App {
	private Logger logger = LoggerFactory.getLogger(App.class);
	private static final int version = 11;
	WatchDog watchDog;

	public App() throws IOException {
		watchDog = new WatchDog();
	}
	
	@Bean("esp32")
	GenericEsp32DeviceFactory esp32() {
		return new GenericEsp32DeviceFactory();
	}
	
	@Bean
	DeviceMapperService deviceMapperService() {
		return new DeviceMapperServiceImpl();
	}
	
	@Bean
	SelfTestOperation selfTestOperation(DeviceMapperService deviceMapperService) {
		return deviceMapperService.injectDevices(new SelfTestOperation());
	}
	
	@Bean
	TheTeaseThreadImpl theTeaseThreadImpl(DeviceMapperService deviceMapperService) {
		return deviceMapperService.injectDevices(new TheTeaseThreadImpl());
	}
	
	@Bean("smartThings")
	SmartThingsDeviceMgrImpl smartThings() throws IOException {
		return new SmartThingsDeviceMgrImpl();
	}

	@Bean 
	StartTimePoller startTimePoller(DeviceMapperService deviceMapperService) {
		StartTimePoller bean = new StartTimePollerImpl();
		return deviceMapperService.injectDevices(bean);
	}
	
	@Bean 
	FillRestTimePoller fillRestTimePoller(DeviceMapperService deviceMapperService) {
		return deviceMapperService.injectDevices(new FillRestTimePollerImpl());
	}
	
	@Bean 
	WatchDog watchDog() {
		return watchDog;
	}
	
	@Bean
	MaintainVacuumThreadImpl mintainVacuumRunable(DeviceMapperService deviceMapperService) {
		return deviceMapperService.injectDevices(new MaintainVacuumThreadImpl());
	}
	
	@Bean("NormalStim")
	TimedOperation doStimRunable(DeviceMapperService deviceMapperService) {
		return deviceMapperService.injectDevices(new DoStimThreadImpl());
	}
	
	@Bean
	PumpOnPoller pumpOnPoller(DeviceMapperService deviceMapperService) {
		return deviceMapperService.injectDevices(new PumpOnPollerImpl());
	}
	
	@Bean("waterFillOperation")
	Operation waterFillOperation(DeviceMapperService deviceMapperService) {
		return deviceMapperService.injectDevices(new AccelerationControledWaterFill());
	}
	
	@Bean
	LightOffPoller lightOffPoller(DeviceMapperService deviceMapperService) {
		return deviceMapperService.injectDevices(new LightOffPollerImpl());
	}
	
	@Bean
	LightOnPoller lightOnPoller(DeviceMapperService deviceMapperService) {
		return deviceMapperService.injectDevices(new LightOnPollerImpl());
	}
	
	@Bean 
	SystemTime systemTime(DeviceMapperService deviceMapperService) {
		return deviceMapperService.injectDevices(new SystemTimeImpl());
	}
	
	@Bean 
	VaccumPoller vaccumPoller(DeviceMapperService deviceMapperService) {
		return deviceMapperService.injectDevices(new VaccumPollerImpl());
	}
	
	@Bean("abpdJJT005PDSA3")
	TransferFn abpdJJT005PDSA3() {
		return new HoneywellAbsPressureTransferFn(-5,5);
	}
	
	@Bean("abpdJJT015PDSA3")
	TransferFn abpdJJT015PDSA3() {
		return new HoneywellAbsPressureTransferFn(-15,15);
	}
	
	@Bean 
	SealCompletePoller sealCompletePoller(DeviceMapperService deviceMapperService) {
		return deviceMapperService.injectDevices(new SealCompletePollerImpl());
	}
	
	@Bean 
	SleepMode sleepMode(DeviceMapperService deviceMapperService) {
		return deviceMapperService.injectDevices(new SleepMode());
	}

	@Bean MainLine mainLine(DeviceMapperService deviceMapperService) {
    	return deviceMapperService.injectDevices(new MainLine());
	}

	
	public static void main(String... args) {
		System.out.println("Version " + version + " starting.");
    	SpringApplication app = new SpringApplication(App.class);
    	app.setHeadless(true);
    	app.setWebApplicationType(WebApplicationType.NONE);
    	app.run(args);
    }
    
	
	
    @Bean
    CommandLineRunner commandLineRunner(final ApplicationContext ctx) throws IOException {
    	return new CommandLineRunner() {
    		@Autowired private WatchDog watchDog;
			@Autowired private MainLine mainLine;
			@Autowired private SleepMode sleepMode;
			@Autowired private StartTimePoller startTimePoller;
			@Autowired private TransferFn abpdJJT005PDSA3;
			// @Autowired private TransferFn abpdJJT015PDSA3;
			@Autowired private DeviceMapperService deviceMapperService;
			
			@Value ("${game.sleepMode}") private boolean sleepingMode;
			@Value("${deviceUrl.vaccumPressure}") private String vaccumPressureUrl;
			@Value("${deviceUrl.externalPressure}") private String sourcePressureUrl;
			@Value("${deviceUrl.internalPressure}") private String insidePressureUrl;
			@Value("${deviceUrl.stimPower}") private String stimPowerUrl;
			@Value("${deviceUrl.controlerPower}") private String controlerPowerUrl;

			@Override
			public void run(String... args) throws Exception {
				logger.info("Starting app");
				
				PressureDevice vaccumPressure = deviceMapperService.getDevice(new DeviceUrl(vaccumPressureUrl));
				PressureDevice sourcePressure = deviceMapperService.getDevice(new DeviceUrl(sourcePressureUrl));
				PressureDevice insidePressure = deviceMapperService.getDevice(new DeviceUrl(insidePressureUrl));
				OutputDevice stimPower = deviceMapperService.getDevice(new DeviceUrl(stimPowerUrl));
				OutputDevice controlerPower = deviceMapperService.getDevice(new DeviceUrl(controlerPowerUrl));
				
				sourcePressure.setTransferFn(abpdJJT005PDSA3);
				insidePressure.setTransferFn(abpdJJT005PDSA3);
				vaccumPressure.setTransferFn(abpdJJT005PDSA3);
				logger.info("Done setting up transfer functions for pressure devices.");

				watchDog.init();
				startTimePoller.displayStartTime(); // insure start time is calculated only when we first start.

				if(sleepingMode) {
					sleepMode.runProgram();
				} else {
					while(mainLine.runProgram()) {
						logger.info("Restarting.");
					}
					logger.info("Main thread exit.");
				}
				if(watchDog.isAlive()) {
					watchDog.shutdown();
				}
				
				// Hay I usually forget to do this stuff now I can't forget anymore
				stimPower.setOff();
				controlerPower.setOff();
				
				System.exit(0);	// make sure we shutdown
			}
		};
    }
    
    
}
