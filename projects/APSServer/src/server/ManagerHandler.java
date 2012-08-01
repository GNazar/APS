package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.LinkedList;

import manager.message.*;
import manager.*;

//class responsible for handling messages from Manager
public class ManagerHandler extends Thread implements Handler{

	private int id;
	private RequestSender request;		//sending requests to database; 
	private ObjectInputStream in; 
	private ObjectOutputStream out;
	
	public ManagerHandler( ObjectInputStream i, ObjectOutputStream o, RequestSender r ) {
		this.in = i;
		this.out = o;
		this.request = r;
		
		this.setDaemon( true );		
		this.start();
	} 
	
	public String toString(){
		return "ManagerHandler: " + super.toString();
	}

	public void run(){
		Message message;
		Integer res_op;
		
		try {

			//login user;
			message = (Message)in.readObject();
			User u = (User)message.getData();
			this.id = request.getCompanyId( u.getCompanyName() );
			out.write( request.checkUser( u ) );	//send result to client;
			out.flush();
		
			do{
				message = (Message)in.readObject();
				
				//parse messages from client;
				switch (message.getCode() ){
			
				case TypeOperation.LOGIN_USER : 
					u = (User)message.getData();
					out.write( request.checkUser( u ) );
					out.flush();
					break;
					
				case TypeOperation.SAMPLE_HISTORY :
					MEnter en = (MEnter)message.getData();
					out.writeObject( request.sampleHistory(en.getDate1(), en.getDate2(), id, en.getParkAddr(), en.getNumber(), en.isFound() ) );
					break;
					
				case TypeOperation.ADD_CAR :
					MCar c = (MCar)message.getData();
					out.write( request.addCar( id, c ) );
					out.flush( );
					break;
					
				case TypeOperation.DELETE_CAR : 
					MCar cd = (MCar)message.getData();
					out.write( request.deleteCar( id, cd) );
					out.flush( );
					break;
					
				case TypeOperation.ADD_USER : 	
					MUser mu = (MUser)message.getData();
					res_op = new Integer (request.addUser(id, mu));
					out.writeObject( res_op );
					break;
					
				case TypeOperation.DELETE_USER :
					MUser dmu = (MUser)message.getData();
					res_op = new Integer (request.deleteUser(id, dmu));
					out.writeObject( res_op );
					break;
					
				case TypeOperation.ADD_PARKING : 
					MParking mp = (MParking)message.getData();
					res_op = new Integer( request.addParking( id, mp ) );
					out.writeObject( res_op );
					break;
					
				case TypeOperation.DELETE_PARKING : 	
					MParking dmp = (MParking)message.getData();
					res_op = new Integer( request.deleteParking(id, dmp));
					out.writeObject( res_op );
					break;
					
				case TypeOperation.CARS : 
					LinkedList<MCar> cars = request.cars( id );
					out.writeObject( cars );
					break;
					
				case TypeOperation.USERS : 
					LinkedList< MUser > users = request.users( id );
					out.writeObject( users );
					break;
					
				case TypeOperation.PARKINGS : 
					LinkedList<MParking> parkings = request.parkings( id );
					out.writeObject( parkings );
					break;
					
				default : 
					System.out.println( "Command code" + message.getCode() + " isn't determined!");
				}
			} while ( true );
			
		} catch (ClassNotFoundException | IOException | SQLException e) {
			System.out.println( "Error while receiving massage!" );
			
			if ( e instanceof IOException )
				this.closeStreams();
		}
		
	}
	
	private void closeStreams( ){
		try{
			in.close();
			out.close();
			System.out.println( "Input/output stream responsible for handling manager is closed!" );
		}catch( IOException e){
			System.out.println( "Error while closing input/output stream responsible for handling manager!" );
			System.out.println( e.getMessage() );
		}
	}

}
