package lpr;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import lpr.Enter;

public class History extends Thread{
	
	private Object s;	//object for synchronization;
	private ObjectOutputStream out;
	private ArrayList< Enter > history;
	private int frequency = 5;	// update frequency in minutes;
	
	public History( ObjectOutputStream o ){
		this.s = new Object();
		
		this.out = o;
		this.history = new ArrayList< Enter >();
		this.setDaemon( true );			//make thread background;
	}
	
	//method add new record to local history
	public void add( Enter e ){
		
		synchronized ( s ) {
			history.add( e );
		}
		
	}
	
	//method close output stream;
	public void closeOut(){
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			System.out.println( "Can't close output stream " );
			System.out.println( e.getMessage() );
		}
	}
	
	public void run(){
		
		while( ! this.interrupted() ){
			
			try {
				synchronized ( s ){
					
					if( !history.isEmpty() ){						
						
						out.writeObject( history );	//send update to server;
						history.clear();
						out.reset();
					}
				}
				TimeUnit.SECONDS.sleep( frequency );
				
			} 
			catch ( IOException e ) {
				System.out.println( "History updating failed!" );
				System.out.println( e.getMessage() );
				this.interrupt();
			}
			 
			catch (InterruptedException e) {
				System.out.println( "Thread resposible for history updeting is interupted!" );
				System.out.println( e.getMessage() );
			}
		}//while;
		this.closeOut();
	}

}
