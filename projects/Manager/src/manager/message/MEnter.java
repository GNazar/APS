package manager.message;

import lpr.Number;

//class for sending history request;

public class MEnter extends Data {
	private String parkAddr;
	private long date1;
	private long date2;
	private Number number;
	private int isFound;
	
	public  Number getNumber(){
		return number;
	}
	
	public long getDate1(){
		return date1;
	}

	public long getDate2(){
		return date2;
	}
	
	public int isFound(){
		return isFound;
	}
	
	public String getParkAddr(){
		return parkAddr;
	}
	
	
	public MEnter( long d1, long d2, String pid, Number n, int b ){
		this.parkAddr = pid;
		this.date1 = d1;
		this.date2 = d2;
		this.number = n;
		this.isFound = b;
		
	
	}
	
}
