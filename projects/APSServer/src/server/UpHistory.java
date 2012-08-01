package server;

import java.net.Socket;
import java.security.interfaces.RSAKey;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.io.*;

import lpr.*;

//class responsible for receiving history from Observer and add it to database;

public class UpHistory extends Thread{
	//private Socket socket;
	private RequestSender request;
	private ObjectInputStream in; 
	
	private final int compId;
	private final int parkId;
	
	public UpHistory( final ObjectInputStream i, final RequestSender rs, final int c,
			final int p ){
		
		//this.socket = s;
		this.in = i;
		this.request = rs;
		this.compId = c;
		this.parkId = p;
		
		this.setDaemon( true );
		
		
	}
	
	public void run(){
		
		while ( !this.isInterrupted() ){
			try {
				Object obj = in.readObject();	//accept history;
				ArrayList< Enter > history = (ArrayList< Enter >)obj;
				
				for( Enter e : history ){
					request.insertEnter( e, compId, parkId );	//add to database;
				}
				
			} 
			catch ( ClassNotFoundException | ClassCastException e ) {
				System.out.println( "UpHistory. Error while receiving history updating at company!" );
				System.out.println( e.getMessage() );
			}
			catch( IOException e ){
				System.out.println( "UpHistory. Error while receiving history updating!" );
				
				System.out.println( e.getMessage() );	//!!!!!!!!!handle connection reset!!!!!!!!!!!
						
				this.interrupt();
			}
		}//while
		
	}

	private void closeIn(){
		try {
			in.close();
			System.out.println( "History updating is stopped");
		} catch (IOException e1) {
			
			System.out.println( "Can't close input stream history updating");
		}

	}
	

}
