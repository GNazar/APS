

package lpr;

import java.io.*;
import java.net.*;


import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

//the main class responsible for connecting to server; 
//and launching cars and history updating; 

public class Observer {

	public static int PORT;
	public static String host;
	
	private BufferedReader reader;		//console;
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	Cars cars;				//class holds list of cars that is permitted enter in parking 
							//and responsible for its updating;
	History history;		//class holds local history send it to server;
	
	
	public Observer( ){
		String name, address;
		ParkingId id;
		this.reader = new BufferedReader( new InputStreamReader ( System.in ) );
		
		System.out.print( "Server IP: " );
				
		try {
			host = reader.readLine();
			
			System.out.print( "Server port: " );
			PORT = Integer.parseInt( reader.readLine() );
			
			this.init();	//connect to server;
		
			System.out.print(" Company name: " );
			name = reader.readLine();

			System.out.print(" Parking address: " );
			address = reader.readLine();
		
			id = new ParkingId ( name, address );
			out.writeObject( id );
		
			if ( (byte)in.read() <= 0 ){
				System.out.println( "Can't found parking! " );
				System.exit( 1 );
			}
						
			try{
				cars = new Cars( in );
				cars.start();
			}
			catch( IOException e){
				System.out.println( "Error while receiving car updating from Server" );
				System.out.println( e.getMessage() );
			}
		
			history = new History( out ); 
			history.start();
			
		}catch ( IOException e ){
			System.out.println( "Input/Output error while register company!" );
			System.out.println( e.getMessage() );
		
		}catch( NumberFormatException e ){
			System.out.println( "Number format for port isn't correct!" );
			System.out.println( e.getMessage() );
			System.exit( 1 );
		}	
			
	}
	/*DELETE-DELETE-DELETE-DELETE-DELETE-DELETE/	
	public void release(){
		history.closeOut();
		cars.closeIn();
	}
*/	
	//method connect to server and create input and output streams 
	private void init(){
		final int INITM = 0;
		
		try {
			InetAddress addr = InetAddress.getByName( Observer.host );
			
			try{
				socket = new Socket( addr,  Observer.PORT );
							
					out = new ObjectOutputStream( socket.getOutputStream() );
					in = new ObjectInputStream( socket.getInputStream() );	
					out.write( INITM );
					out.flush( );
								
			}catch( IOException e){
				System.out.println( "Can't create socket!" );
				System.out.println( e.getMessage() );
				System.exit( 1 );
			} 
		}catch ( UnknownHostException e){
			System.out.println( "Unknown host!" );
			System.out.println( e.getMessage() );
			System.exit( 1 );

		}
	}
	
	public boolean isAllowed( Number num ){

		boolean res = cars.contains( num );		//check if set cars contain parameter "num";
		
		Enter enter = new Enter( res, num );
		history.add( enter );		//add record to local history; 
		
		return res;
	}

	 public static void main(String[] args) throws InterruptedException {
		 String path = "testvideo.avi";				//test;
		 String clip_path = "file:///d:/TED/flame.avi";
		 
		 int t;
		 Plate p;
		 Number num;
		 Observer c;
		 IplImage pl, capture;
		 
		 Extracter ex = new Extracter();
    	 c = new Observer();
		 OCR ocr = new OCR( 5, 100 );	//class responsible for recognizing characters;
		 FrameGrabber fg =  new OpenCVFrameGrabber( path );
		 MediaPanel mp = new MediaPanel(); //class responsible for playing media-file;

		 
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!		 
		try {
			//			while( true ){
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			fg.start();
			try{
				while ( true  ){		 
				
					capture = fg.grab();
					pl = ex.extract( capture );
				
					if ( pl != null )  {					//plate found;
						p = new Plate( pl );
						num = p.recognize( ocr );
						
						if ( ! c.isAllowed( num ) )
							mp.play( clip_path );
						
						t = 0;
						
						while (t < 120 ){	
							fg.grab();
							t++;
						}
					}
				}
				}catch ( Exception e ){ 	System.out.println( "VideGrabbingError!!!!!!!!!!" );   }
//			}//external while;	
			
		} catch (Exception e) {
			System.out.println(" Error while starting grabbing video!");
			System.out.println( e.getMessage() );
		}finally{
			
			ex.release();
			mp.close();	
			try {
				fg.release();
			} catch (Exception e) {
				System.out.println( "Can't close frame grabber" );
			}
	 	}		
		
		System.out.println(" Observer stopped"); 
	}

}
