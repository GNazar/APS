package lpr;

import java.io.Serializable;

//class holds result of entry   

public class Enter implements Serializable {
	public final long date;
	private Number number;
	public final boolean isFound;
	
	public  Number getNumber(){
		return number;
	}
	
	public Enter(){
		date = System.currentTimeMillis();
		isFound = false;
		number = null;
		
	}
	
	public Enter( final boolean f, Number n ){
		date = System.currentTimeMillis();
		isFound = f;
		number = n;
		
	}
	
	public Enter(long d, boolean f, Number n ){
		date = d;
		isFound = f;
		number = n;
		
	}
	
}
