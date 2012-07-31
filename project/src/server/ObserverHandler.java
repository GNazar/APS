package server;

import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.io.*;

import lpr.*;

//class responsible for handling messages from Observer;
public class ObserverHandler implements Handler{
	
	private RequestSender request;
	private UpHistory histUpdater;
	private CarsSender sender;
	
	private int compId;
	private int parkId;
	
	public ObserverHandler ( ObjectInputStream i, ObjectOutputStream o, RequestSender r ){
		int result[];
		this.request = r;
		try{
			ParkingId id = (ParkingId)i.readObject();	//get parking id and company id by company name and parking address;
			result = request.getParkingId(id);
		
			compId = result[0];
			parkId = result[1];
		
			o.write( compId );			//send parking id and company id to Observer  
			o.flush();
		
			histUpdater = new UpHistory( i, request, compId, parkId );
			histUpdater.start();		//history updating thread;
		
			sender = new CarsSender( o, compId, parkId, request );
			sender.start();				//cars updating thread;
			
		}catch( IOException | ClassNotFoundException e ){
			System.out.println ( "Error while creating observer handler" );
			System.out.println( e.getMessage() );
		}
	}
	
}
