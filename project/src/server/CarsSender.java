package server;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lpr.Number;

//class responsible for sending cars update to local database;

public class CarsSender extends Thread{
	
	private ObjectOutputStream out;
	private final RequestSender request;
	
	private final int compId;
	private final int parkId;
	private final int frequency = 5;	//update frequency, must be ~30 min;
	
	public CarsSender( final ObjectOutputStream o, final int c, final int p, final RequestSender rs ){
		
		this.compId = c;
		this.parkId = p;
		this.request = rs;
		this.out = o;
		
	}
	
	public void run(){
		
			
		Set< Number > ns = new HashSet< Number >();
		while( !this.isInterrupted() ){		

			try {			 
				ns = request.getCarUpdating( compId, parkId );		//make request to database;	
																
				out.writeObject( ns );								//send update to client;
				TimeUnit.SECONDS.sleep( frequency );	 
			
			} catch ( IOException e ) {
				System.out.println( "Error while sending updating!" );
				System.out.println( e.getMessage() );
				
				this.closeOut();
				this.interrupt();
				
			} catch( SQLException e){
				System.out.println( "Error while cars sampling." );
				System.out.println( e.getMessage() );
			
			} catch ( InterruptedException e){
				this.closeOut( );
				System.out.println( e.getMessage() );
			}
			
		}//WHILE
		
	}
	
	private void closeOut(){
		try {
			out.close();
			System.out.println( "Sending cars is stopped");
		} catch ( IOException e ) {
			System.out.println( "Can't close output stream!" );
			System.out.println( e.getMessage() );
		}
	}
}
