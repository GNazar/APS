package server;


//class for terminating thread;
public class Monitor {
	private boolean value;
	
	public Monitor( ){
		value = true;
	}
	
	public synchronized void terminate(){
		value = false;
	}
	
	public synchronized boolean isAlive(){
		return value;
	}
}
