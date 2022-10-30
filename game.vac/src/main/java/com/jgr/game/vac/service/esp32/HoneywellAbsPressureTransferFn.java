package com.jgr.game.vac.service.esp32;

import com.jgr.game.vac.interfaces.PressureDevice.TransferFn;

public class HoneywellAbsPressureTransferFn implements TransferFn {
	float lowValue;
	float highValue;
	
	long readingRange = 16384;
	// m will be 0.000762939453125
	
	public HoneywellAbsPressureTransferFn(float lowValue, float highValue) {
		this.highValue = highValue;
		this.lowValue = lowValue;
	}
	
	@Override
	public float calc(long data) {
		double rung = (0.8 * (double) readingRange);
		double rise = (highValue-lowValue); 
		double m = rise/rung ;
		double b = -(readingRange/rung + 5);
		
		double retVal = m * data + b;
		
		return (float) retVal;
	}
	
	public static void main(String[] args) {
		TransferFn transferFn = new HoneywellAbsPressureTransferFn(-5, 5);
		System.out.println(transferFn.calc(9365));
	}
}
