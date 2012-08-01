package manager;

import java.io.*;

import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


import lpr.Number;
import manager.message.*;

//class provides interaction with user; 

public class Manager {
	
	public final static int PORT = 1501;
	
	private int companyId;
	private BufferedReader reader;
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	
	public int getCompanyId(){
		return this.companyId;
	}
	
	public Manager( ){
		
		this.reader = new BufferedReader( new InputStreamReader ( System.in ) );
		this.init();

	}
	
	//login new user
	public int login(){
		User user;
		Message mes;
		int result = -1;
		String name, login, pass;
		
		try{	
			//input data
			System.out.println( "Company name: " );
			name = reader.readLine();
			if ( name.equals( UserCommands.EXIT) )
				return result;
			
			System.out.println( "Login: " );
			login = reader.readLine();
			if ( login.equals( UserCommands.EXIT) )
				return result;
			
			System.out.println( "Password: " );
			pass = reader.readLine();
			if ( login.equals( UserCommands.EXIT) )
				return result;

			
			user = new User( login, pass, name );
			mes = new Message ( -1, TypeOperation.LOGIN_USER, user );
			out.writeObject( mes );				//send message to server;
			result = (byte)in.read( );			//receive result;
			
		
		}catch( IOException e ){
			System.out.println("Error while sending/receiving data!");
			System.out.println( e.getMessage() );
			e.printStackTrace();
		}
		
		return result;
	} 
	
	//get history;
	public int history(){
		String patt = "yy.MM.dd HH:mm:ss";
		String str;
		ArrayList<MEnter> res;
		Date tempd;
		
		long d1 = -1, d2 = -1;
		String addr = null;
		Number nmb = null;
		int b = -1, result = 0;
		
		MEnter en = null;
		Message m = null;
		
		//input data;
		System.out.println( "Date in format: " + patt + ". If you want to skip parametr, press Enter." );
		System.out.print( "since->");
		try{
			str = reader.readLine();
			if (str.length() != 0){
				SimpleDateFormat formatter = new SimpleDateFormat( patt );
				d1 = formatter.parse( str ).getTime();				//convert date;
				System.out.print( "to->" );
				str = reader.readLine();
				d2 = formatter.parse( str ).getTime();				//convert date;
			}
			
			System.out.print( "Parking address->" );
			str = reader.readLine();
			if (str.length() != 0)
				addr = str;
			
			System.out.print( "Cars number->" );
			str = reader.readLine();
			if (str.length() != 0)
				nmb = new Number ( str.toCharArray() );

			System.out.print( "Is found( 1 - found, 0 - not found )->" );
			str = reader.readLine();
			if (str.length() != 0)
				b = Integer.parseInt( str );

			en = new MEnter( d1, d2, addr, nmb, b );
			m = new Message( companyId, TypeOperation.SAMPLE_HISTORY, en );
			
			out.writeObject( m );							//send request to server;
			res = ( ArrayList<MEnter> )in.readObject();		//receive result
			
			//output history;
			for ( MEnter me : res ){
				tempd = new Date ( me.getDate2() );
				System.out.printf("%-25s   %-20s  %-10s   %b\n", tempd.toString(), me.getParkAddr(), new String ( me.getNumber().getValue() ), me.isFound() ==1 );
				result++;
			}
			
			if ( result == 0 ){
				System.out.println( "No records appropriate your request" );
			}
			
		}catch ( ParseException e ){
			System.out.println( "Wrong date format!" );
		//	System.out.println( e.getMessage() );
			return -1;
		}catch ( IOException | ClassNotFoundException | ClassCastException e ){
			System.out.println( "Input/Output error!" );
			System.out.println( e.getMessage() );
			return -1;
		}catch ( NumberFormatException e ){
			System.out.println( "Data format is not valid!" );
			System.out.println( e.getMessage() );
			return -1;
		}
		
		return result;	
	}
	
	private void addCar( ){
		int result = -1;
		String str;
		String addr;
		
		Number n;
		MCar mc;
		Message mes;
		
		System.out.print ( "Enter number->" );
		try{
			str = reader.readLine();			
			n = new Number ( str.toCharArray( ) );
			System.out.print ( "Enter parking address->" );
			addr = reader.readLine();
			mc = new MCar( n, addr );
			mes = new Message( companyId, TypeOperation.ADD_CAR, mc );
			
			out.writeObject( mes );				//send data to server	
			
			result = (byte)in.read();			//receive result of operation;
			
			if ( result > 0 )
				System.out.println( "Car is added!" );
			else System.out.println( "Car isn't added!" );
			
		}catch ( IOException e ){
			System.out.println( "Input/Output error while adding car!" );
			System.out.println( e.getMessage() );
			//e.printStackTrace();		//!!!!!!!!!!!!!!!
		}
	}
	
	private void deleteCar( ){
		int result = -1;
		String str;
		String addr;
		
		Number n;
		MCar mc;
		Message mes;
		
		//data inputting;
		System.out.print ( "Enter number->" );
		try{
			str = reader.readLine();
			n = new Number ( str.toCharArray( ) );
			System.out.println( "Number to sent:" + new String (n.getValue() ) );
			System.out.print ( "Enter parking address->" );
			addr = reader.readLine();
			mc = new MCar( n, addr );
			mes = new Message( companyId, TypeOperation.DELETE_CAR, mc );
			out.writeObject( mes );			//send data to server;
			result = (byte)in.read();		//receive result;
			
			if ( result > 0 )
				System.out.println( "Car is deleted!" );
			else System.out.println( "Error! Car isn't deleted!" );
			
		}catch ( IOException e ){
			System.out.println( "Input/Output error!" );
			System.out.println( e.getMessage() );
		}	
		
	} 

	private void addUser( ){
		String name, pass;
		Integer result = null;
		MUser mu;
		Message mes;
		
		//data inputting
		System.out.println( "User name->" );
		try{
			name = reader.readLine();
			System.out.println( "Password->" );
			pass = reader.readLine();
			
			mu = new MUser( name, pass );
			mes = new Message( companyId,TypeOperation.ADD_USER, mu );
			
			out.writeObject( mes );					//send data			
			result = ( Integer )in.readObject();	//receive result;
			
			if ( result.intValue() > 0 )
				System.out.println( "User is added" );
			else System.out.println( "Error! User isn't added!" );
		
		}catch ( IOException | ClassNotFoundException e ){
			System.out.println( "Input/Output error while adding user!" );
			System.out.println( e.getMessage() );
		}	
		
	}
	
	private void deleteUser(){
		String name, pass;
		Integer result = null;
		MUser mu;
		Message mes;
		
		//data inputting
		System.out.println( "User name->" );
		try{
			name = reader.readLine();
			System.out.println( "Password->" );
			pass = reader.readLine();
			
			mu = new MUser( name, pass );
			mes = new Message( companyId,TypeOperation.DELETE_USER, mu );
			
			out.writeObject( mes );					//send data to server
			result = ( Integer )in.readObject();	//receive result;
			
			if ( result.intValue() > 0 )
				System.out.println( "User is deleted" );
			else System.out.println( "Error! User isn't deleted!" );
		
		}catch ( IOException | ClassNotFoundException e ){
			System.out.println( "Input/Output error while deleting user!" );
			System.out.println( e.getMessage() );
		}		
	}
	
	private void addParking(){
		String addr;
		MParking mp;
		Message mes;
		Integer result = null;
		
		System.out.println( "Address->" );
		try{
			addr = reader.readLine();
			mp = new MParking( addr );
			mes = new Message( companyId, TypeOperation.ADD_PARKING , mp );
			
			out.writeObject( mes );					//send data to server
			result = ( Integer )in.readObject();	//receive operation result
			
			if ( result.intValue() > 0 )
				System.out.println( "Parking is added" );
			else System.out.println( "Error! Parking isn't added!" );
		
		}catch( IOException | ClassNotFoundException e ){
			System.out.println( "Input/Output error while adding parking!" );
			System.out.println( e.getMessage() );	
		}
	}
	
	private void deleteParking(){
		String addr;
		MParking mp;
		Message mes;
		Integer result = null;
		
		//data inputting
		System.out.println( "Address->" );
		try{
			addr = reader.readLine();
			mp = new MParking( addr );
			mes = new Message( companyId, TypeOperation.DELETE_PARKING , mp );
			out.writeObject( mes );
			result = ( Integer )in.readObject();
			
			if ( result.intValue() > 0 )
				System.out.println( "Parking is deleted" );
			else System.out.println( "Error! Parking isn't deleted!" );
		
		}catch( IOException | ClassNotFoundException e ){
			System.out.println( "Input/Output error while adding parking!" );
			System.out.println( e.getMessage() );	
		}
	}
	
	//get list of cars that allowed enter to parking;
	private void cars(){
		int result = 0;
		LinkedList<MCar> cars;
		Message mes;
		
		try{
			mes = new Message( companyId, TypeOperation.CARS, null );
			
			out.writeObject( mes );								//send data to server;
			cars = ( LinkedList<MCar> )in.readObject();			//receiver result;
			
			//outputting
			for ( MCar tmp : cars ){
				System.out.printf(" %-12s  %-20s  %-25s\n", new String ( tmp.getNumber().getValue() ), 
					tmp.getAddress(), new Date ( tmp.getTime() ));
				
				result++;
			}
			
			if ( result == 0 )
				System.out.println( "List is empty." );
			
		}catch ( IOException | ClassNotFoundException e ){
			System.out.println( "Input/Output error while getting cars!" );
			System.out.println( e.getMessage() );
		}

	}

	//get company users; 
	private void users(){
		int result = 0;
		LinkedList<MUser> users;
		Message mes;
		mes = new Message( companyId, TypeOperation.USERS, null );
		try{
			
			out.writeObject( mes );							//send data to server;
			users = ( LinkedList<MUser> )in.readObject();	//receive list of users;
			
			//output users
			for ( MUser tmp : users ){
				System.out.printf( "%-20s  %s\n", tmp.getLogin(), tmp.getType() ? "administrator" : "user" );			
				result++;
			}
			
			if ( result == 0 )
				System.out.println( "List is empty." );
			
		}catch ( IOException | ClassNotFoundException e ){
			System.out.println( "Input/Output error while adding car!" );
			System.out.println( e.getMessage() );
			//e.printStackTrace();		//!!!!!!!!!!!!!!!
		}

	}
	//get list of parking for company;
	private void parkings(){
		int result = 0;
		LinkedList<MParking> parkings;
		Message mes;
		mes = new Message ( companyId, TypeOperation.PARKINGS , null );
		
		try{
			out.writeObject( mes );									//send data to server;
			parkings = (LinkedList< MParking >)in.readObject( );	//receive list of cars
			
			for (MParking tmp : parkings ){
				System.out.println( "   " + tmp.getAddress() );
				result++;
			}
			
			if ( result == 0 )
				System.out.println( "List is empty." );
			
		}catch ( IOException | ClassNotFoundException e ){
			System.out.println( "Input/Output error while sampling parkings!" );
			System.out.println( e.getMessage() );
		}
		
	} 	
	private void exit(){
		try{
			socket.close();
			try{
				in.close();
				out.close();
				try{
					reader.close();
				}catch( IOException e ){
					System.out.println( "Can't close System.in!" );
					System.out.println ( e.getMessage() );
				}
			}catch( IOException e ){
				System.out.println( "Can't ObjectInput/OutputStream!" );
				System.out.println( e.getMessage() );
			}
		}catch( IOException e ){
			System.out.println( "Can't close socket!" );
			System.out.println( e.getMessage() );
		}
	}
	//method connect to server and create input/output streams
	private void init(){
		
		final byte INITM = 1;
		
		System.out.print( "Sever IP: ");
		try {
			
			String host = reader.readLine();
			
			InetAddress addr = InetAddress.getByName( host );
			
			try{				
				socket = new Socket( addr,  PORT );

				try {
					
					out = new ObjectOutputStream( socket.getOutputStream() );
					in = new ObjectInputStream( socket.getInputStream() );
					out.write( INITM );		//send type of client;
					out.flush();

				}catch ( IOException e ){
					
					System.out.println( "Can't create input/output stream!" );
					System.out.println( e.getMessage() );
					socket.close();
					System.exit( 1 );
				}
			
			}catch( IOException e){
				System.out.println( "Can't create socket!" );
				System.out.println( e.getMessage() );
				System.exit( 1 );
			} 
			
		}catch ( UnknownHostException e){
			System.out.println( "Unknown host!" );
			System.out.println( e.getMessage() );
			System.exit( 1 );
		}catch ( IOException e ){
			System.out.println( "Error while reading data form console!" );
			System.out.println( e.getMessage() );
			System.exit( 1 );
		}

	}

	private int parseUserCommands( ){
		String scom;
		
		do{																			 
			try {
				
				scom = this.reader.readLine();
				switch ( scom ){
										 
				case UserCommands.HISTORY : this.history();
					break;
				case UserCommands.CARS : this.cars();
					break;
				case UserCommands.PARKING : this.parkings();
					break;
					
				case UserCommands.LOG_OUT : return 0;
				
				case UserCommands.EXIT : this.exit();
					return -1;
										    
				default : System.out.println( "Uknown command!" );
				
				
				}
			} catch (IOException e) {
				System.out.println( "Error while reading command!" );
				return -1;
			}
		}while ( true );
	}
	
	private int parseAdmCommands( ){
		String scom;
		
		do{																			 
			try {
				
				scom = this.reader.readLine();
				switch ( scom ){
										 
				case AdmCommands.HISTORY : this.history();
					break;
				case UserCommands.CARS : this.cars();
					break;
				case UserCommands.PARKING : this.parkings();
					break;
				case AdmCommands.ADD_CAR : this.addCar();
					break;
				case AdmCommands.DALETE_CAR : this.deleteCar();
					break;
				case AdmCommands.ADD_USER : this.addUser();
					break;
				case AdmCommands.DELETE_USER : this.deleteUser();
					break;
				case AdmCommands.ADD_PARKING : this.addParking();
					break;
				case AdmCommands.DELETE_PARKING : this.deleteParking();
					break;
				case AdmCommands.USERS : this.users();
					break;
				
				case AdmCommands.LOG_OUT : return 0;
				
				case UserCommands.EXIT : this.exit(); 
					return -1;
										    
				default : System.out.println( "Uknown command!" );
				
				
				}
			} catch (IOException e) {
				System.out.println( "Error while reading command!" );
				return -1;
			}
		}while ( true );
	}

	
	public static void main(String[] args) throws InterruptedException {
		
		int login, i = -1;
		
		Manager m = new Manager();
		
		do{
			do{
				login = m.login();

				if ( login < 0 )
					System.out.println( "Error! Log in unsuccessful!" );
			}while ( login == 0 );
		
			if ( login == 1 ){
				System.out.println("Log in successful for user.");
				 i = m.parseUserCommands();
			}else{
				if ( login == 2 ){
					System.out.println("Log in successful for administrator.");
					i = m.parseAdmCommands();
					//handle administrator;
				}
			}
		}while ( i >= 0);
		
	}

}
