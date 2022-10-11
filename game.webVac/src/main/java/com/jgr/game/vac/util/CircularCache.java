package com.jgr.game.vac.util;

public class CircularCache<E> {

	private Object theArray[];
	private int writePos;
	private int size;
	private boolean full;
	
	public CircularCache(int size) {
		theArray = new Object[size];
		writePos = 0;
		this.size = size;
		full = false;
	}
	
	public E[] getCache() {
		Object arrayCopy[];

		synchronized (theArray) {
			if(!full) {
				if(writePos != 0) {
					arrayCopy = new Object[writePos];
					System.arraycopy(theArray, 0, arrayCopy, 0, writePos);
				} else {
					arrayCopy = null;
				}
			} else {
				arrayCopy = new Object[size];
				System.arraycopy(theArray, writePos, arrayCopy, 0, size - writePos);
				System.arraycopy(theArray, 0, arrayCopy, size - writePos, writePos);
			}
		}
		
		@SuppressWarnings("unchecked")
		E[] retVal = (E[]) arrayCopy; 
		return retVal;
	}
	
	public void add(E element) {
		synchronized (theArray) {
			theArray[writePos++] = element;
			if(writePos == size) {
				writePos = 0;
				full = true;
			}
		}
	}
	
}
