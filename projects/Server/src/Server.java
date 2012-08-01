//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

import java.net.*;
import java.io.*;

public class Server {

	public static final int PORT = 1501;
	
	public static void main(String[] args) {
		try{
			ServerSocket ss = new ServerSocket( PORT );		//!!!!!!!!!!! can be already bound;
			System.out.println( "Server: " + ss ); 
			Socket s;
			
			try{
				while( true )
				{
					s = ss.accept();
					try{
						//	launch new thread;
						System.out.println( "Connection accepted: " + s );
						BufferedReader in = new BufferedReader( new InputStreamReader( s.getInputStream() ) );
						while ( true ){
							String str = in.readLine();
							System.out.println( "Accepted number: " + str );
						}
					
					}finally{
						System.out.println( "Close Server" );
						s.close();
					}
				}
				
				
				
				
			}finally{
				System.out.println( "Close ServerSocket" );
				ss.close();
			}
		
		}catch( IOException e){
			System.out.println( "Error while creating ServerSocket. Exception: " + e );
		}
	}

}
