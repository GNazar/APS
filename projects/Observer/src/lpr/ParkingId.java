package lpr;

import java.io.Serializable;

//Wrapper for two Strings

public class ParkingId implements Serializable {

	private String comp;
	private String park;
	
	public ParkingId( ){
		
	}
	
	public ParkingId( String c ,String p ){
		this.comp = c;
		this.park = p;
	}
	
	public String getCompany(){
		return comp;
	}
	
	public String getAddress(){
		return park;
	}
	
	public boolean equals ( Object o ){
		ParkingId t;
		if ( o == this ) return true;
		t = (ParkingId)o;
		if ( ! t.comp.equals(this.comp) ) return false;
		if ( ! t.park.equals(this.park) ) return false;
		return true;
	}
	
	public int hashCode(){
		return this.comp.hashCode() + this.park.hashCode();
	}

}
