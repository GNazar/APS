package manager.message;

//class for sending parking to server;

public class MParking extends Data{
	private String address;
	
	public MParking( String paddr){
		this.address = paddr;
	} 
	
	public String getAddress(){
		return address;
	}
}
