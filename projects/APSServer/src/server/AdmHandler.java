package server;

import java.io.*;

//class responsible for handling system administrator commands; 

public class AdmHandler extends Thread {
	
	private class GAdmCommands{
		public final static String ADD_COMPANY = "add company";
		public final static String DELETE_COMPANY = "delete company";
		public final static String COMPANIES = "companies";
		public final static String LOG_OUT = "log out";
		public final static String EXIT = "exit";
		public final static String CHANGE_LOGIN = "change login";
	} 
	
	private BufferedReader console;
	private RequestSender rs;
	private Monitor monitor;
	
	public AdmHandler( Monitor m ){
		console = new BufferedReader( new InputStreamReader ( System.in ) );
		monitor = m;
	}
	
	public void setRequestSender( RequestSender r ){
		rs = r;
	}
	
	//method add new company to database;
	private void addCompany(){
		
		String name;
		String adm_login;
		String adm_pass;
		String mark;
		int result;
		
		//data inputting;
		System.out.print( "Company name: " );
		
		try{
			name = console.readLine();
			
			System.out.print( "Administrator login: " );
			adm_login = console.readLine();
			
			System.out.print( "Administrator password: " );
			adm_pass = console.readLine();
			
			System.out.print( "Mark: " );
			mark = console.readLine();
	 
			result = rs.addCompany(name, adm_login, adm_pass, mark);	//make request to database;
			
			if (result > 0)
				System.out.println( "Company added." );
			else System.out.println( "Company isn't added." );
			
		}catch ( IOException e ){
			System.out.println( "Error while reading data from console!" );
			System.out.println( e.getMessage() );
		}
	}
	
	//method delete company from database;
	private void deleteCompany(){
		String name;
		int result;
		
		//data inputting;
		System.out.print( "Company name: " );
		try{
			name = console.readLine();
			result = rs.deleteCompany(name);		//make request to database;
			
			if ( result > 0 )
				System.out.println( "Company deleted." );
			else System.out.println( "Company isn't deleted." );		
			
		}catch ( IOException e ){
			System.out.println( "Error while reading data from console!" );
			System.out.println( e.getMessage() );
		}
	}
	//method output list of companies
	private void companies(){
		
		if ( rs.companies() == 0 )
			System.out.println( "There no companies" );
		
	}
	
	//method encrypt String passed as parameter and return array;
	private int[] encrypt( String str ){
		int[] result = new int[ str.length() ];
		int c;
		
		for(int i = 0; i < str.length(); i++ ){
			c = str.charAt( i ) + 2*i + i*i - str.length();
			result[i] = c;
		}
		return result;
	}
	
	//method decrypt array passed as parameter;
	public String decrypt( int[] arr ){
		char result[] = new char[ arr.length ];
		
		for( int i = 0; i< arr.length; i++ ){
			result[i] = (char)( arr[i] - 2*i - i*i + arr.length );
			
		}
		
		return new String( result );
	}
	
	//method encrypt "login" and "pass" and write it in "file";
	private void writeLoginPass( String login, String pass, String file ){
		
		int[] log = encrypt( login );	//encrypt login;
		int[] ps = encrypt( pass );		//encrypt password;
		
		try {
			
			//create data output stream for writing login and password;
			DataOutputStream dos = new DataOutputStream( new BufferedOutputStream ( new FileOutputStream (file) ) );
			try{
				//write login;
				dos.writeInt( login.length() );
				for (int tmp : log )
					dos.writeInt( tmp );
				
				//write password;
				dos.writeInt( pass.length() );
				for ( int tmp : ps )	
					dos.writeInt( tmp );
				
			} catch (IOException e) {
				System.out.println( "Error while writing data in " + file );
				System.out.println( e.getMessage() );
			}finally{
				
				try {
					dos.close();
				} catch (IOException e) {
					System.out.println ( "Can't close data output stream!" );
					System.out.println( e.getMessage() );
				}
			}
		
		} catch (FileNotFoundException e) {
			System.out.println( "Can't found file " + file );
			System.out.println( e.getMessage() );
			
		}
	} 
	
	//login administrator;
	public int login( ){
		String file = "\\inf.adm";
		String login, pass, u_login, u_pass;
		int length;
		
		int[] log, ps;
		
		try {
			File directory = new File (".");
			file = directory.getCanonicalPath() + file;
			DataInputStream dis = new DataInputStream( new BufferedInputStream ( new FileInputStream (file) ) );
			try{			
				//read login
				length = dis.readInt();
				log = new int[length];
				for ( int i = 0; i < length; i++ ){
					log[i] = dis.readInt();
				}
		
				//read password;
				length = dis.readInt();
				ps = new int[ length ];
				for ( int i = 0; i < length; i++ ){
					ps[i] = dis.readInt();
				}
			
				login = decrypt( log );		//decrypt login;
				pass = decrypt ( ps );		//decrypt password;
			
			}finally{
				dis.close();
			}
			
			do{
				//read data;
				System.out.print( "Login: " );
				u_login = console.readLine();
				if (u_login.equals( GAdmCommands.EXIT ) )
					return -1;
				
				System.out.print( "Password: " );
				u_pass = console.readLine();
				if (u_pass.equals( GAdmCommands.EXIT ) )
					return -1;
				
			}while ( ! u_login.equals( login ) && ! u_pass.equals( pass ) )	;
			
		} catch (FileNotFoundException e) {
			System.out.println( "Can't found file " + file );
			System.out.println( e.getMessage() );
			return -1;
		}
		catch ( IOException e ){
			System.out.println( "Error while accessing to " + file );
			System.out.println( e.getMessage() );
			e.printStackTrace();
			return -1;
		}
		return 1;
	}
	
	//method change administrator login and password;
	private void changeLogin(){
		
		String file = "\\inf.adm";
		
		String login, pass;
		try{
			File directory = new File (".");
			file = directory.getCanonicalPath() + file;
			System.out.print ( "New login: " );
			try{
				login = console.readLine();

				System.out.print ( "New password: " );
				pass = console.readLine();
			
				this.writeLoginPass(login, pass, file);
			}catch( IOException e ){
				System.out.println( "Error while reading naw login and password!" );
				System.out.println( e.getMessage() );
			}
		}catch( IOException e ){
			System.out.println( "Can't found file " + file );
		}
		
	}
	
	public void run(){	
		String com;
		
		boolean b = true;
		boolean t = true;
		
		try{			
			while ( t ){
	
				com = console.readLine();
				
				if ( com == null )
					com = GAdmCommands.EXIT;
				
				switch( com ){
				
				case GAdmCommands.ADD_COMPANY : 
					this.addCompany();
					break;
					
				case GAdmCommands.DELETE_COMPANY : 
					this.deleteCompany();
					break;
					
				case GAdmCommands.COMPANIES : 
					this.companies();
					break;

				case GAdmCommands.LOG_OUT : 
					b = this.login() > 0 ? true : false;
					if ( b )
						System.out.println( "Administrator registrated. Enter command" );
					break;
						
				case GAdmCommands.EXIT : 
					t = false;
					break;
					
				case GAdmCommands.CHANGE_LOGIN :
					this.changeLogin();
					break;
						
				default : 
					System.out.println( "Uknown command!" );
				}//switch;
			}
		}catch( IOException e){
			System.out.println(" Error while reading command!");
			e.printStackTrace();
		}finally{
			
			monitor.terminate();	//set flag responsible for server executing; 
			
			try {
				console.close();
				System.out.println( "AdmHandler closed" );
			} catch (IOException e) {
				System.out.println( "" );
				System.out.println( e.getMessage() );
			}
		}
	}

}
