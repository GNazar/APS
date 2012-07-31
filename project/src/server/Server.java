package server;

import java.net.*;
import java.io.*;

public class Server {

	public static final int PORT = 1501;
	
	private ServerSocket ss;
	private RequestSender rs;

	
	public Server(){
		
		try{
			ss = new ServerSocket( PORT );	//create server socket;
			rs = new RequestSender();
							
		}catch( IOException e){
			System.out.println( "Error while creating ServerSocket!" );
			System.out.println( e.getMessage() );
			System.exit(1);
		}
	}
	
	public Socket accept() throws IOException{
		return ss.accept();
	}
	
	public void close(){
		try{
			if( ss!=null )
				ss.close();
		}
		catch( IOException e ){
			System.out.println( "Socket not closed!" );
			System.out.println( e.getMessage() );
		}
		
		rs.delete();
		
		System.out.println( "Server closed" );
	}
	
	public RequestSender getRequestSender(){
		return rs;
	}
	
	public Handler handle( Socket socket ){
		
		Handler res = null;
		final int OBSERVER = 0;
		final int MANAGER = 1;
		int i = -1;
		try{
			ObjectOutputStream out = new ObjectOutputStream ( socket.getOutputStream() );
			ObjectInputStream in = new ObjectInputStream ( socket.getInputStream() );
			try{
				
				i = (byte)in.read();
				if( i == MANAGER ){
					System.out.println ( "Manager is connected" );
					res = new ManagerHandler( in, out, rs );
					return res;	
				}
		
				if ( i == OBSERVER ){
					res = new ObserverHandler( in, out, rs );
					System.out.println ( "Obser is connected" );
					return res;	
				}
				
			}catch ( IOException e ){
				System.out.println( "Error while initialize client!" );
				System.out.println( e.getMessage() );
				out.close( );
				in.close( );
				socket.close();
			}
			
		}catch( IOException e){
			System.out.println( "Error while creating input/output streams for interraction with client" );
			System.out.println( e.getMessage() );
		}
		return null;		
	}
		
	public static void main(String[] args)  {
		
		Socket socket;
		Monitor monitor = new Monitor();
		
		AdmHandler handler = new AdmHandler( monitor );	//interface system administrator;
		
		handler.login();								//login administrator;
		Server s = new Server();
		
		handler.setRequestSender( s.getRequestSender() );
		
		handler.start();								
		
		Handler h;
		
		//listening for new connection;
		while( monitor.isAlive() ){
			try{
				socket = s.accept();
				h = s.handle( socket );
			}catch( IOException e ){
				System.out.println( "Error while connecting" );
				System.out.println( e.getMessage() );
			}
		}
		//s.close();
	}

}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		

		
		
		
		
		
		
		
		
		
