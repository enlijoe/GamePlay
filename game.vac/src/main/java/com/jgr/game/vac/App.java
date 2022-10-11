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

import com.jgr.game.vac.interfaces.DoStimThread;
import com.jgr.game.vac.interfaces.FillRestTimePoller;
import com.jgr.game.vac.interfaces.LightOffPoller;
import com.jgr.game.vac.interfaces.LightOnPoller;
import com.jgr.game.vac.interfaces.PumpOnPoller;
import com.jgr.game.vac.interfaces.SealCompletePoller;
import com.jgr.game.vac.interfaces.StartTimePoller;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.interfaces.VaccumPoller;
import com.jgr.game.vac.operations.Operation;
import com.jgr.game.vac.operations.SelfTestOperation;
import com.jgr.game.vac.operations.TimedControledWaterFill;
import com.jgr.game.vac.poller.FillRestTimePollerImpl;
import com.jgr.game.vac.poller.LightOffPollerImpl;
import com.jgr.game.vac.poller.LightOnPollerImpl;
import com.jgr.game.vac.poller.PumpOnPollerImpl;
import com.jgr.game.vac.poller.SealCompletePollerImpl;
import com.jgr.game.vac.poller.StartTimePollerImpl;
import com.jgr.game.vac.poller.VaccumPollerImpl;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.Esp32Impl;
import com.jgr.game.vac.service.MainLine;
import com.jgr.game.vac.service.SleepMode;
import com.jgr.game.vac.service.SmartThingsDeviceMgrImpl;
import com.jgr.game.vac.service.SystemTimeImpl;
import com.jgr.game.vac.service.WatchDog;
import com.jgr.game.vac.service.esp32.GenericEsp32DeviceFactory;
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

	@Value("${pressureSensor.internal.uri}") private String internalPressureUrl;
	@Value("${pressureSensor.external.uri}") private String externalPressureUrl;
	
	public App() throws IOException {
		watchDog = new WatchDog();
	}
	
	@Bean("esp32")
	GenericEsp32DeviceFactory esp32() {
		return new GenericEsp32DeviceFactory();
	}
	

	@Bean("oldEsp32")
	Esp32Impl OldEsp32() {
		return new Esp32Impl();
	}
	
	@Bean
	DeviceMapperService deviceMapperService() {
		return new DeviceMapperServiceImpl();
	}
	
	@Bean
	SelfTestOperation selfTestOperation() {
		return new SelfTestOperation();
	}
	
	@Bean
	TheTeaseThreadImpl theTeaseThreadImpl() {
		return new TheTeaseThreadImpl();
	}
	
	@Bean("smartThings")
	SmartThingsDeviceMgrImpl smartThings() throws IOException {
		return new SmartThingsDeviceMgrImpl();
	}

	@Bean 
	StartTimePoller startTimePoller() {
		StartTimePoller bean = new StartTimePollerImpl();
		return bean;
	}
	
	@Bean 
	FillRestTimePoller fillRestTimePoller() {
		return new FillRestTimePollerImpl();
	}
	
	@Bean 
	WatchDog watchDog() {
		return watchDog;
	}
	
	@Bean
	MaintainVacuumThreadImpl mintainVacuumRunable() {
		return new MaintainVacuumThreadImpl();
	}
	
	@Bean 
	DoStimThread doStimRunable() {
		return new DoStimThreadImpl();
	}
	
	@Bean
	PumpOnPoller pumpOnPoller() {
		return new PumpOnPollerImpl();
	}
	
	@Bean("waterFillOperation")
	Operation waterFillOperation() {
		return new TimedControledWaterFill();
	}
	
	@Bean
	LightOffPoller lightOffPoller() {
		return new LightOffPollerImpl();
	}
	
	@Bean
	LightOnPoller lightOnPoller() {
		return new LightOnPollerImpl();
	}
	
	@Bean 
	SystemTime systemTime() {
		return new SystemTimeImpl();
	}
	
	@Bean 
	VaccumPoller vaccumPoller() {
		return new VaccumPollerImpl();
	}
	
	@Bean 
	SealCompletePoller sealCompletePoller() {
		return new SealCompletePollerImpl();
	}
	
	@Bean 
	SleepMode sleepMode() {
		return new SleepMode();
	}

	@Bean MainLine mainLine() {
    	return new MainLine();
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
    		@Autowired WatchDog watchDog;
			@Autowired MainLine mainLine;
			@Autowired SleepMode sleepMode;
			@Autowired private StartTimePoller startTimePoller;
	
			@Value ("${game.sleepMode}") private boolean sleepingMode;

			@Override
			public void run(String... args) throws Exception {
				logger.info("Starting app");
				
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
				System.exit(0);	// make sure we shutdown
			}
		};
    }
    
    
}
