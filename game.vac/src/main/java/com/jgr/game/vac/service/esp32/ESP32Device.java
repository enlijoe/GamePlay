package com.jgr.game.vac.service.esp32;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgr.game.vac.interfaces.Device;
import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.PressureDevice;
import com.jgr.game.vac.service.DeviceManager;
import com.jgr.game.vac.service.DeviceUrl;
import com.jgr.game.vac.service.RemoteWatchDog;

class ESP32Device implements DeviceManager, RemoteWatchDog {
	private static Logger logger = LoggerFactory.getLogger(GenericEsp32DeviceFactory.class);

	private int deviceLoggingInterval;
	private String deviceLoggingBase;
	
	private String UUID;
	private String type;
	private int version;
	private int loggingPort;
	private int controlPort;
	private InetAddress address;
	
	LoggingThread loggingThread;
	ControlThread controlThread;
	HashMap<String, ESP32SubDevice> deviceMap = new HashMap<String, ESP32SubDevice>();
	
	@Override
	public void setDeviceLogging(boolean value) {
		if(controlThread != null) {
			controlThread.setDeviceLogging(value);
		} else {
			logger.error("Unable to start device logging since control thread is not active yet.");
		}
	}
	
	@Override
	public boolean isDeviceLogging() {
		return controlThread.isDeviceLogging();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <DeviceType> DeviceType getDevice(DeviceUrl deviceUrl) {
		ESP32SubDevice device = deviceMap.get(deviceUrl.getSubDevice());
		
		if(device == null) {
			logger.error("Can't find device for " + deviceUrl);
		} else {
			logger.debug("Found device " + device.getName() + " for " + deviceUrl);
		}
		
		return (DeviceType) device;
	}
	
	public HashMap<String, ? extends Device> getDeviceMap() {
		return deviceMap;
	}
	
	@Override
	public String getDescription() {
		return UUID;
	}
	
	private class ESP32SubDevice implements Device {
		private char type;
		private int id;
		private String name;
		
		void init(char type, int id, String name) {
			this.type = type;
			this.id = id;
			this.name = name;
		}
		
		@Override
		public DeviceManager getOwner() {
			return ESP32Device.this;
		}
		
		Map<String, Object> read() {
			return controlThread.sendCommand("R" + type + id);
		}
		
		Map<String, Object> write(int value) {
			return controlThread.sendCommand("W" + type + id + value);
		}
		
		Map<String, Object> status() {
			return controlThread.sendCommand("S" + type + id);
		}
		
		Map<String, Object> info() {
			return controlThread.sendCommand("I" + type + id);
		}
		
		public String getName() {
			return name;
		}
	}
	
	@Override
	public void checkIn() {
		Map<String, Object> data = controlThread.sendCommand("WC10");
		if(!"OK.".equalsIgnoreCase(data.get("status").toString())) {
			throw new RuntimeException("Error checking in with remote watch dog.");
		}
	}
	
	@Override
	public boolean getStatus() {
		Map<String, Object> data = controlThread.sendCommand("RC0");
		return !"0".equals(data.get("expired"));
	}
	
	@Override
	public void reset() {
		// we need to set everything to off and reset any watch dog errors here
		for(ESP32SubDevice device:deviceMap.values()) {
			if(device instanceof OutputDevice) {
				OutputDevice outputDevice = (OutputDevice) device;
				outputDevice.setOff();
			}
		}
		Map<String, Object> data = controlThread.sendCommand("WC30");
		if(!"OK.".equalsIgnoreCase(data.get("status").toString())) {
			throw new RuntimeException("Error resetting remote watch dog. (status=" + data.get("status").toString() + ")");
		}
	}

	@Override
	public void disable() {
		Map<String, Object> data = controlThread.sendCommand("WC00");
		if(!"OK.".equalsIgnoreCase(data.get("status").toString())) {
			throw new RuntimeException("Error trying to disable remote watch dog.");
		}
	}
	
	@Override
	public void enable() {
		Map<String, Object> data = controlThread.sendCommand("WC01");
		if(!"OK.".equalsIgnoreCase(data.get("status").toString())) {
			throw new RuntimeException("Error trying to enable remote watch dog.");
		}
	}
	
	@Override
	public void errorState() {
		// turn everything off and then enter an error state where nothing will be able to be turned on again until reset
		for(ESP32SubDevice device:deviceMap.values()) {
			if(device instanceof OutputDevice) {
				OutputDevice outputDevice = (OutputDevice) device;
				outputDevice.setOff();
			}
		}
		Map<String, Object> data = controlThread.sendCommand("WC20");
		if(!"OK.".equalsIgnoreCase(data.get("status").toString())) {
			throw new RuntimeException("Error sending error state to remote watch dog.");
		}
	}
	
	void handleError(String msg, Map<String, Object> data) {
		String errMsg = "";
		if(data.containsKey("error")) {
			msg = data.get("error").toString();
			throw new RuntimeException(msg + " - " + errMsg);
		} else {
			throw new RuntimeException(msg);
		}
	}
	
	class ESP32InputDevice extends ESP32SubDevice implements InputDevice {
		@Override
		public int getValue() {
			Map<String, Object> data = read();
			if(data.containsKey("value")) {
				return Integer.parseInt(data.get("value").toString());
			} else {
				handleError("Unable to read value", data);
			}
			return 0; // dead code
		}
		
		@Override
		public boolean isOff() {
			return getValue() == 0;
		}
		
		@Override
		public boolean isOn() {
			return getValue() != 0;
		}
		
	}
	
	class ESP32OutputDevice extends ESP32SubDevice implements OutputDevice {
		@Override
		public void setValue(int value) {
			Map<String, Object> data = write(value);
			if(!data.containsKey("status")) {
				handleError("Unable to write value", data);
			}
		}
		
		@Override
		public void setOff() {
			setValue(0);
		}
		
		@Override
		public void setOn() {
			setValue(1);
		}
	}
	
	class ESP32PressureDevice extends ESP32SubDevice implements PressureDevice {
		TransferFn transferFn = null;
		
		float minValue;
		float maxValue;
		
		ESP32PressureDevice(float minValue, float maxValue) {
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		
		@Override
		public float getMinValue() {
			return minValue;
		}

		@Override
		public float getMaxValue() {
			return maxValue;
		}

		@Override
		public float readValue() {
			int value;
			
			Map<String, Object> data = read();
			if(data.containsKey("value")) {
				value = Integer.parseInt(data.get("value").toString());
			} else {
				handleError("Unable to read value", data);
				return 0;
			}

			if(transferFn != null) {
				return transferFn.calc(value);
			} else {
				return value;
			}
		}
		
		@Override
		public void setTransferFn(TransferFn transferFn) {
			this.transferFn = transferFn;
		}
	}

	public void start() {
		loggingThread = new LoggingThread(this);
		controlThread = new ControlThread(this, deviceLoggingInterval, deviceLoggingBase);
		 
		//loggingThread.start();
		
		try {
			controlThread.open();
		} catch (IOException ex) {
			logger.error("Error opening control thread.", ex);
		}
		
		// get the list of devices
		Map<String, Object> data = controlThread.sendCommand("SC0");
		int numInput = getNumSubDevices("inputs", data);
		// logger.info("There are " + numInput + " input devices");
		for(int pos = 0; pos < numInput; pos++) {
			Map<String, Object> subDevData = controlThread.sendCommand("II"+pos);
			if(subDevData.containsKey("name")) {
				String name = subDevData.get("name").toString();
				ESP32InputDevice subDevice = new ESP32InputDevice();
				subDevice.init('I', pos, name);
				deviceMap.put(name, subDevice);
			}
		}
		
		int numOutput = getNumSubDevices("outputs", data);
		// logger.info("There are " + numOutput + " output devices");
		for(int pos = 0; pos < numOutput; pos++) {
			Map<String, Object> subDevData = controlThread.sendCommand("IO"+pos);
			if(subDevData.containsKey("name")) {
				String name = subDevData.get("name").toString();
				ESP32OutputDevice subDevice = new ESP32OutputDevice();
				subDevice.init('O', pos, name);
				deviceMap.put(name, subDevice);
			}
		}
		
		int numPressure = getNumSubDevices("pressure", data);
		// logger.info("There are " + numPressure + " pressure devices");
		for(int pos = 0; pos < numPressure; pos++) {
			Map<String, Object> subDevData = controlThread.sendCommand("IP"+pos);
			if(subDevData.containsKey("name")) {
				String name = subDevData.get("name").toString();
				float minValue = Float.parseFloat(subDevData.get("min").toString());
				float maxValue = Float.parseFloat(subDevData.get("max").toString());
				ESP32PressureDevice subDevice = new ESP32PressureDevice(minValue, maxValue);
				subDevice.init('P', pos, name);
				deviceMap.put(name, subDevice);
			}
		}
		
	}
	
	private int getNumSubDevices(String type, Map<String, Object> data) {
		if(data.containsKey(type)) {
			return Integer.parseInt(data.get(type).toString());
		}
		return 0;
	}
	
	public String getUUID() {
		return UUID;
	}
	public void setUUID(String uUID) {
		UUID = uUID;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public int getLoggingPort() {
		return loggingPort;
	}
	public void setLoggingPort(int loggingPort) {
		this.loggingPort = loggingPort;
	}
	public int getControlPort() {
		return controlPort;
	}
	public void setControlPort(int controlPort) {
		this.controlPort = controlPort;
	}
	public InetAddress getAddress() {
		return address;
	}
	public void setAddress(InetAddress address) {
		this.address = address;
	}

	protected int getDeviceLoggingInterval() {
		return deviceLoggingInterval;
	}

	protected void setDeviceLoggingInterval(int deviceLoggingInterval) {
		this.deviceLoggingInterval = deviceLoggingInterval;
	}

	protected String getDeviceLoggingBase() {
		return deviceLoggingBase;
	}

	protected void setDeviceLoggingBase(String deviceLoggingBase) {
		this.deviceLoggingBase = deviceLoggingBase;
	}
	
	
}
