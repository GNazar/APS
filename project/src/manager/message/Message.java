package manager.message;

import java.io.*;

//class for sending messages to server
public class Message implements Serializable{
	final private int id;			//company id;
	final private int code;		//operation code;
	final private Data data;			//operation contents;
	
	public Message( ){
		this.id = 0;
		this.code = 0;
		this.data = null;
	}
	
	public Message ( int i, int c, Data d ){
		this.id = i;
		this.code = c;
		this.data = d;
	}
	
	public int getId( ){
		return id;
	}
	
	public int getCode( ){
		return code;
	}
	
	public Data getData( ){
		return data;
	}
}
