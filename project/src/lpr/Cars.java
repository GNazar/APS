package lpr;

import java.io.*;
import java.util.*;
//class responsible for receiving cars update from server and add itlocal set; 
public class Cars extends Thread {
	
	private ObjectInputStream in;
	private Set< Number > cars;		//list of cars that that are received;
	private final Object synch;
	
	public Cars( final ObjectInputStream i ) throws IOException{
	
		this.in = i;
		this.cars = new HashSet< Number >();
		this.synch = new Object();
		this.setDaemon( true );
		this.update();
		
	}
	
	//method check if number passed as parameter exists in set "cars"
	public boolean contains( Number n ){
		boolean b;
		
		synchronized( synch ){	
			b = cars.contains( n );
		}
		
		return b;
	}
	
	//method close input socket;
	public void closeIn(){
		try {
			in.close();
		} catch (IOException e) {
			System.out.println( "Can't close input stream " );
			System.out.println( e.getMessage() );
			e.printStackTrace();
		}
	}
	
	//method output contents "cars" set;
	public void display(){
		
		System.out.println( "Numbers: " );
		
		for( Number n : cars ){
			System.out.println( ": " + new String ( n.getValue() ) );
		}
		
	}
	
	//method accept cars update from server;
	private void update() throws IOException {		
		Object tmp;
		Set< Number > set;

		try{
			tmp = in.readObject();	//receive object;
			
			try{
				set = ( Set< Number > )tmp;
				
				synchronized( synch ){
					cars.addAll( set );		//add received cars set to local set of cars;
				}
			
			}catch( ClassCastException e ){
				System.out.println( "Error while receiving cars updating!" );
				System.out.println( e.getMessage() );
			}
		}
		
		catch ( ClassNotFoundException e ){
			System.out.println( "Error while receiving cars updating!" );
			System.out.println( e.getMessage() );
			
		}
		
	}
	
	public void run(){
		
		while( !this.isInterrupted() ){
			try{
				update();
			} 
			catch( IOException e1 ){
				System.out.println( e1.getMessage() );
				
				this.closeIn();
				
				this.interrupt();
			}
		}
			
	}
	
	public Object getSynchObject(){
		return synch;
	} 

}
