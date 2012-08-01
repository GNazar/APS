package manager.message;

import lpr.Number;

//class for sending cars to server
public class MCar extends Data{
	private Number number;
	private long time;
	private String address;
	
	public MCar ( Number n, String a ){
		this.number = n;
		this.address = a;
		this.time = System.currentTimeMillis();
	}

	public MCar ( Number n, String a, final long t ){
		this.number = n;
		this.address = a;
		this.time = t;
	}
	
	public Number getNumber (){
		return number;
	}
	
	public long getTime(){
		return time;
	}
	
	public String getAddress(){
		return address;
	}

}
