package com.jgr.game.vac.service.esp32;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO figure out why using this crashes the ESP32 controler
class LoggingThread extends Thread {
	private static Logger logger = LoggerFactory.getLogger(LoggingThread.class);

	private ESP32Device device;
	boolean supressEsp32Logs = false;
	
	public LoggingThread(ESP32Device device) {
		this.device = device;
		setDaemon(true);
		setName("Logger for " + device.getType() + " on " + device.getAddress());
	}
	
	@Override
	public void run() {
		Socket loggingSocket = null;
		try {
			loggingSocket = new Socket(device.getAddress(), device.getLoggingPort());
			LineNumberReader loggingReader = new LineNumberReader(new InputStreamReader(loggingSocket.getInputStream()));
			while(true) {
				String loggingLine = loggingReader.readLine();
				if(loggingLine == null) {
					break;
				}
				if(!supressEsp32Logs) {
					logger.info(device.getType() + " on " + device.getAddress() + "-"+ loggingLine);
				}
			}
		} catch(Throwable t) {
			logger.error("Error trying to setup logging for " + device.getType() + " on " + device.getAddress());
		} finally {
			if(loggingSocket != null) {
				try {
					loggingSocket.close();
				} catch(IOException ex) {
					
				}
			}
		}
	}
}
