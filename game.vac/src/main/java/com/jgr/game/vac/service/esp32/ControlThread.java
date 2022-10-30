package com.jgr.game.vac.service.esp32;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

class ControlThread extends Thread {
	private static Logger logger = LoggerFactory.getLogger(ControlThread.class);
	private static final long COMMAND_TIMEOUT = 2500;
	private static final int LOGGER_SEQUENCE_NUM = 0; 
	private static final String SequenceNumLabel = "seqNum";
	private ESP32Device device = null;
	private LineNumberReader reader = null;
	private PrintWriter writer = null;
	private PrintStream deviceLogStream;
	Socket socket = null;
	HashMap<Integer, Map<String, Object>> responseMap = new HashMap<>();
	HashMap<Integer, ControlerEvent> eventMap = new HashMap<>();
	private int deviceLoggingInterval;
	private String deviceLoggingBase;
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss.SSSS");
	
	boolean busy = false;
	int nextSequenceNum = 100;
	ArrayList<String> loggerDeviceOrder = new ArrayList<>();
	ArrayList<Object> loggedData = new ArrayList<>();
	DataLoggingThread loggingThread;
	
	interface ControlerEvent {
		void handleEvent(Map<String, Object> data);
	}
	
	public boolean isDeviceLogging() {
		return deviceLogStream != null;
	}
	
	public void setDeviceLogging(boolean value) {
		synchronized(deviceLoggingBase) {
			if(value) {
				if(deviceLogStream == null) {
					int seqNum = 0;
					String fileName;
					do {
						fileName = deviceLoggingBase+"-"+seqNum+".csv";
						seqNum++;
					} while((new File(fileName)).exists());
					try {
						loggerDeviceOrder.clear();
						loggedData.clear();
						deviceLogStream = new PrintStream(fileName);
					} catch (FileNotFoundException ex) {
						logger.error("Unable to open device log file " + deviceLoggingBase+"-"+seqNum, ex);
					}
				}
			} else {
				if(deviceLogStream != null) {
					deviceLogStream.close();
					deviceLogStream = null;
				}
			}
		}
	}
	
	private class DataLoggingThread extends Thread {
		@Override
		public void run() {
			try {
				while(true) {
					if(deviceLogStream != null ) {
						logger.debug("Logging data");
						Map<String, Object> data = sendCommand("RP*");
						handleLoggerMsg(data);
					}
					sleep(deviceLoggingInterval);
				}
			} catch (InterruptedException ex) {
				logger.info("Data logging thread stopped.", ex);
			}
		}
			
	}
	
	public ControlThread(ESP32Device device, int deviceLoggingInterval, String deviceLoggingBase) {
		if(deviceLoggingBase == null || deviceLoggingInterval < 10) {
			throw new IllegalStateException("logging base must be defined with a log interval greater then 10. base="+deviceLoggingBase+" inverval " + deviceLoggingInterval);
		}
		this.device = device;
		this.deviceLoggingInterval = deviceLoggingInterval;
		this.deviceLoggingBase = deviceLoggingBase;
		setDaemon(true);
		setName("Control for " + device.getType() + " on " + device.getAddress());
	}
		
	void open() throws IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(device.getAddress(), device.getControlPort()));
		InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();
		
		reader = new LineNumberReader(new InputStreamReader(in));
		writer = new PrintWriter(new OutputStreamWriter(out));
		start();
	}
	
	private Integer nextSequenceNum() {
		return nextSequenceNum++;
	}
	
	Map<String, Object> sendCommand(String command) {
		Map<String, Object> retVal = null;

		// first see if we are logging this device and if so then return the value last logged and don't get it again
		if('R' == command.charAt(0)) {
			synchronized(deviceLoggingBase) {
				if(deviceLogStream != null) {
					String devId = command.substring(1, 2);
					int dataIndex = loggerDeviceOrder.indexOf(devId);
					if(dataIndex != -1) {
						Object data = loggedData.get(dataIndex);
						retVal = new HashMap<>();
						retVal.put("value", data);
						return retVal;
					}
					
				}
			}
		}

		synchronized(responseMap) {
			int retry = 0;
			do {
				Integer sequenceNum = nextSequenceNum();
				
				String message = sequenceNum + "-" + command; 
				// logger.info("Sending message - " + message);
				try {
					while(!responseMap.isEmpty()) {
						responseMap.wait();
					}

					responseMap.put(sequenceNum, null);
					writer.println(message);
					writer.flush();
					long startTime = System.currentTimeMillis();
					while(responseMap.get(sequenceNum) == null) {
						long curTime = System.currentTimeMillis();
						long timeWaited = curTime - startTime; 
						if(timeWaited >= COMMAND_TIMEOUT) {
							break;
						}
						responseMap.wait(COMMAND_TIMEOUT - timeWaited);
					}
					retVal = responseMap.remove(sequenceNum);
					responseMap.notifyAll();
					
					if(retVal == null) {
						logger.warn("Timed out waiting for response from device on seqNum " + sequenceNum + ".");
						retry++;
						continue;
					}
				} catch(InterruptedException ex) {
					logger.trace("intrupted while waiting for message response");
				}
				return retVal;
			} while(retry < 3);
		}
		throw new RuntimeException("Timed out waiting for response from device.");
	}
	
	
	void close() throws IOException {
		if(writer != null) {
			writer.close();
			writer = null;
		}
		if(reader != null) {
			reader.close();
			reader = null;
		}
		
		if(socket != null) {
			socket.close();
			socket = null;
		}
	}
	
	@Override
	public void run() {
		loggingThread = new DataLoggingThread();
		loggingThread.setDaemon(true);
		loggingThread.setName("Data Logging Thread");
		loggingThread.start();
		
		Gson gson = new Gson();
		try {
			while(true) {
				String line = reader.readLine();
				@SuppressWarnings("unchecked")
				Map<String, Object> data = gson.fromJson(line, Map.class);
				Object seqNumObj = data.get(SequenceNumLabel);
				if(seqNumObj == null) {
					if(!line.contentEquals("{\"status\":\"Hello.\"}")) {
						logger.info("invalid data from esp32 " + line);
					}
					continue;
				}
				logger.debug("Got data from esp32 - " + line);
				
				Integer seqNum = Integer.parseInt(seqNumObj.toString());
				handleStandardMsg(line, data, seqNum);
			}
		} catch(Throwable t) {
			logger.error("Error in control receiving thread", t);
		} 
	}

	private void handleLoggerMsg(Map<String, Object> data) {
		synchronized(deviceLoggingBase) {
			if(deviceLogStream != null) {
				if(loggerDeviceOrder.isEmpty()) {
					deviceLogStream.print("\"time\"");
					for(String key:data.keySet()) {
						if(SequenceNumLabel.equals(key)) {
							continue;
						}
						deviceLogStream.print(",\"");
						deviceLogStream.print(key);
						deviceLogStream.print('"');
						loggerDeviceOrder.add(key);
					}
					deviceLogStream.println();
				}
				loggedData.clear();
				
				deviceLogStream.print(dateFormat.format(new Date(System.currentTimeMillis())));
				for(String key:loggerDeviceOrder) {
					deviceLogStream.print(',');
					Object entryData = data.get(key);
					deviceLogStream.print(entryData.toString());
					loggedData.add(entryData);
				}
				deviceLogStream.println();
			}
		}
	}
	
	private void handleStandardMsg(String line, Map<String, Object> data, Integer seqNum) {
		synchronized(responseMap) {
			if(responseMap.containsKey(seqNum)) {
				responseMap.put(seqNum, data);
				responseMap.notifyAll();
			} else if(eventMap.containsKey(seqNum)) {
				eventMap.get(seqNum).handleEvent(data);
			} else {
				logger.info("Unexpected data from device (" + line + ")");
			}
		}
	}
}
