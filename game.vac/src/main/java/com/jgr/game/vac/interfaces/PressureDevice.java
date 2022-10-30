package com.jgr.game.vac.interfaces;

public interface PressureDevice extends Device {
	public interface TransferFn {
		float calc(long data);
	}
	
	
	public float readValue();
	public float getMinValue();
	public float getMaxValue();
	public void setTransferFn(TransferFn transferFn);
}
