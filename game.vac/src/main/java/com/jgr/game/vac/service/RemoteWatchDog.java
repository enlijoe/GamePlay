package com.jgr.game.vac.service;

public interface RemoteWatchDog {
	/**
	 * Checks in with the remote watch dog service.  If this is not done when enabled then after a time out of not checking in the device
	 * will enter an error state, disable all outputs changes and turn off all outputs
	 */
	public void checkIn();
	
	/**
	 * True if everything is good (no error state)
	 * False if in an error state and output are locked off 
	 * @return
	 */
	public boolean getStatus();
	
	/**
	 * Reset any error state, turn off all outputs, allows outputs to be modified again if they were locked
	 */
	public void reset();
	
	/**
	 * Force an error state, disable all outputs and turn them off
	 */
	public void errorState();
	
	/**
	 * Disable the remote watch dog if allowed
	 */
	public void disable();
	
	/**
	 * Enable the remote watch dog if allowed
	 */
	public void enable();
	
	/**
	 * Description of the device this remote watch dog is for
	 * @return
	 */
	public String getDescription();
}
