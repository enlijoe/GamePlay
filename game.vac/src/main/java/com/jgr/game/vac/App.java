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
import com.jgr.game.vac.interfaces.PressureDevice;
import com.jgr.game.vac.interfaces.PressureSensor;
import com.jgr.game.vac.interfaces.PumpOnPoller;
import com.jgr.game.vac.interfaces.SealCompletePoller;
import com.jgr.game.vac.interfaces.SmartThings;
import com.jgr.game.vac.interfaces.StartTimePoller;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.interfaces.VaccumPoller;
import com.jgr.game.vac.interfaces.WaterFillPoller;
import com.jgr.game.vac.poller.FillRestTimePollerImpl;
import com.jgr.game.vac.poller.LightOffPollerImpl;
import com.jgr.game.vac.poller.LightOnPollerImpl;
import com.jgr.game.vac.poller.PumpOnPollerImpl;
import com.jgr.game.vac.poller.SealCompletePollerImpl;
import com.jgr.game.vac.poller.StartTimePollerImpl;
import com.jgr.game.vac.poller.VaccumPollerImpl;
import com.jgr.game.vac.poller.WaterFillPollerImpl;
import com.jgr.game.vac.service.Esp32Impl;
import com.jgr.game.vac.service.MainLine;
import com.jgr.game.vac.service.PressureSensorImpl;
import com.jgr.game.vac.service.PropertyService;
import com.jgr.game.vac.service.SleepMode;
import com.jgr.game.vac.service.SmartThingsImpl;
import com.jgr.game.vac.service.SystemTimeImpl;
import com.jgr.game.vac.service.WatchDog;
import com.jgr.game.vac.service.esp32.GenericEsp32DeviceFactory;
import com.jgr.game.vac.thread.DoStimThreadImpl;
import com.jgr.game.vac.thread.MaintainVacuumThreadImpl;
import com.jgr.game.vac.thread.RandomStimThreadImpl;
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
	
	@Bean
	GenericEsp32DeviceFactory esp32Devices() {
		return new GenericEsp32DeviceFactory();
	}
	

	@Bean 
	PropertyService propertyService() {
		return new PropertyService();
	}
	
	@Bean
	Esp32Impl esp32Internal() {
		return new Esp32Impl();
	}
	
	@Bean
	TheTeaseThreadImpl theTeaseThreadImpl() {
		return new TheTeaseThreadImpl();
	}
	
	@Bean
	SmartThingsImpl smartThingsInternal() throws IOException {
		return new SmartThingsImpl();
	}

	@Bean StartTimePoller startTimePoller() {
		StartTimePoller bean = new StartTimePollerImpl();
		return bean;
	}
	
	@Bean FillRestTimePoller fillRestTimePoller() {
		return new FillRestTimePollerImpl();
	}
	
	@Bean WatchDog watchDog() {
		return watchDog;
	}
	
	@Bean PressureSensor internalPressure() {
		return new PressureSensorImpl(internalPressureUrl);
	}

	@Bean PressureSensor externalPressure() {
		return new PressureSensorImpl(externalPressureUrl);
	}
	
	@Bean MaintainVacuumThreadImpl mintainVacuumRunable() {
		return new MaintainVacuumThreadImpl();
	}
	
	@Bean DoStimThread randomStimThread() {
		return new RandomStimThreadImpl();
	}
	
	@Bean DoStimThread doStimRunable() {
		return new DoStimThreadImpl();
	}
	
	@Bean PumpOnPoller pumpOnPoller() {
		return new PumpOnPollerImpl();
	}
	
	@Bean
	public LightOffPoller lightOffPoller() {
		return new LightOffPollerImpl();
	}
	
	@Bean
	public GenericEsp32DeviceFactory newEsp31() {
		return new GenericEsp32DeviceFactory();
	}
	
	@Bean
	public LightOnPoller lightOnPoller() {
		return new LightOnPollerImpl();
	}
	
	@Bean
	 public SmartThings smartThings() {
		return watchDog;
	}
	
	@Bean 
	public SystemTime systemTime() {
		return new SystemTimeImpl();
	}
	
	@Bean 
	public VaccumPoller vaccumPoller() {
		return new VaccumPollerImpl();
	}
	
	@Bean 
	public SealCompletePoller sealCompletePoller() {
		return new SealCompletePollerImpl();
	}
	
	@Bean
	public WaterFillPoller waterFillPoller() {
		return new WaterFillPollerImpl();
	}
	
	@Bean 
	public SleepMode sleepMode() {
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
    public CommandLineRunner commandLineRunner(final ApplicationContext ctx) throws IOException {
    	return new CommandLineRunner() {
//    		@Autowired WatchDog watchDog;
//			@Autowired MainLine mainLine;
//			@Autowired SleepMode sleepMode;
//			@Autowired GenericEsp32DeviceFactory newEsp31; 
//			@Autowired private StartTimePoller startTimePoller;
			@Autowired PressureDevice pressure1;
			@Autowired PressureDevice pressure2;
			@Autowired PressureDevice pressure3;
			@Autowired PressureDevice pressure4;
//			@Autowired private SmartThings smartThings;
//			@Autowired private Runnable doStimRunable;
//
	
//			@Value ("${game.sleepMode}") private boolean sleepingMode;
//			@Value("${smartthings.switch.waterValve}") private String waterValve;
//
//
			void doValue(String name , PressureDevice device){
				System.out.println("Input value for " + name + " is " + device.readValue() + " range " + device.getMinValue() + " to " + device.getMaxValue());
			}
			
			@Override
			public void run(String... args) throws Exception {
				logger.info("Starting app");
				doValue("pressure1", pressure1);
				doValue("pressure2", pressure2);
				doValue("pressure3", pressure3);
				doValue("pressure4", pressure4);
				
//				watchDog.init();
//				startTimePoller.displayStartTime(); // insure start time is calculated only when we first start.
//				// smartThings.listAllDevices();
//
////				mainLine.controlledFill(true);
//				
//				if(sleepingMode) {
//					sleepMode.runProgram();
//				} else {
//					while(mainLine.runProgram()) {
//						logger.info("Restarting");
//					}
//				}
//				
//				watchDog.shutdown();
				System.exit(0);	// make sure we shutdown
			}
		};
    }
    
    
}
