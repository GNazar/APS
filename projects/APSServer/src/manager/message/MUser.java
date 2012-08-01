package manager.message;

//class for sending user from server to client;

public class MUser extends Data{
	private String login;
	private String pass;
	private boolean type;	// false - user; true - administrator;
	
	public MUser( ){
		this.login = null;
		this.pass = null;
		this.type = false;
	}
	
	public MUser ( String l, String p, boolean t ){
		this.login = l;
		this.pass = p;
		this.type = t;
	}

	public MUser ( String l, String p ){
		this.login = l;
		this.pass = p;
	}

	public String getLogin(){
		return login;
	}
	
	public String getPass(){
		return pass;
	}
	
	public boolean getType( ){
		return type;
	}
}

