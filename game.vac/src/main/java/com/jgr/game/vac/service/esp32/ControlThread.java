package com.jgr.game.vac.service.esp32;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

class ControlThread extends Thread {
	private static Logger logger = LoggerFactory.getLogger(ControlThread.class);
	private static final long COMMAND_TIMEOUT = 10000;
	
	private ESP32Device device = null;
	private LineNumberReader reader = null;
	private PrintWriter writer = null;
	Socket socket = null;
	HashMap<Integer, Map<String, Object>> responseMap = new HashMap<>();
	HashMap<Integer, ControlerEvent> eventMap = new HashMap<>();
	
	boolean busy = false;
	int nextSequenceNum = 1;
	
	interface ControlerEvent {
		void handleEvent(Map<String, Object> data);
	}
	
	public ControlThread(ESP32Device device) {
		this.device = device;
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
		synchronized (this) {
		
			Integer sequenceNum = nextSequenceNum();
			Map<String, Object> retVal = null;
			
			long startTime = System.currentTimeMillis();
			responseMap.put(sequenceNum, null);
			String message = sequenceNum + "-" + command; 
			// logger.info("Sending message - " + message);
			writer.println(message);
			writer.flush();
			try {
				while(responseMap.get(sequenceNum) == null) {
					long curTime = System.currentTimeMillis();
					long timeWaited = curTime - startTime; 
					if(timeWaited >= COMMAND_TIMEOUT) {
						break;
					}
					this.wait(COMMAND_TIMEOUT - timeWaited);
				}
				retVal = responseMap.get(sequenceNum);
				responseMap.remove(sequenceNum);
				if(retVal == null) {
					throw new RuntimeException("Timed out waiting for response from device on seqNum " + sequenceNum + ".");
				}
			} catch(InterruptedException ex) {
			}
			return retVal;
		}
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
		Gson gson = new Gson();
		try {
			while(true) {
				String line = reader.readLine();
				@SuppressWarnings("unchecked")
				Map<String, Object> data = gson.fromJson(line, Map.class);
				Object seqNumObj = data.get("seqNum");
				if(seqNumObj == null) {
					if(!line.contentEquals("{\"status\":\"Hello.\"}")) {
						logger.info("invalid data from esp32 " + line);
					}
					continue;
				}
				//logger.info("Got data from esp32 - " + line);
				Integer seqNum = Integer.parseInt(seqNumObj.toString()); 
				synchronized (this) {
					if(responseMap.containsKey(seqNum)) {
						responseMap.put(seqNum, data);
					} else if(eventMap.containsKey(seqNum)) {
						eventMap.get(seqNum).handleEvent(data);
					} else {
						logger.info("Unexpected data from device (" + line + ")");
					}
					notify();
				}
			}
		} catch(Throwable t) {
			logger.error("Error in control receiving thread", t);
		} 
	}
}
