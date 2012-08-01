package manager.message;

//class for sending user to server;

public class User extends Data{
	private String login;
	private String pass;
	private String name;
	
	public User( ){
		this.login = null;
		this.pass = null;
		this.name = null;
	}
	
	public User ( String l, String p, String n ){
		this.login = l;
		this.pass = p;
		this.name = n;
	}


	public String getLogin(){
		return login;
	}
	
	public String getPass(){
		return pass;
	}
	
	public String getCompanyName( ){
		return name;
	}
}

