package lpr;

import java.io.Serializable;
//class holds cars number;

public class Number implements Serializable {

	private char[] value;
	private int hash;
	
	public Number(){
		value = null;
	}
	
	public Number( char[] v ){
		value = v;
		valid( (char)1054, '0' );	//1054 - code 'O';
		valid( 'Â', '8' );
		culcHashCode( );
	}
	
	public char[] getValue(){
		return value;
	}
	
	public int hashCode(){
		return hash;
	}
	
	public boolean equals( Object  n ){
		
		
		if( !( n instanceof Number ) )
			return false;
		
		if ( this == n) return true;
		
		int i = 0;
		Number number = ( Number )n;
		char[] c = number.getValue();
		
		for ( int tmp : c ){
			if( tmp != (int)value[i++] )
				return false;
		}
		
		return true;
	}
	
	// method check if number appropriate format "CCDDDDCC",
	//where C - character and D - digit;
	public boolean isValid(){
		
		if( value.length <= 0 ) return false;
		
		int l = value.length - 1;
		
		if ( ( value[0] < '9' ) || ( value[1] < '9' ) 	// first two and last two character are digits
				|| ( value[l] < '9' ) || ( value[l-1] < '9' ) )
			return false;
			
		
		for (int i = 2; i < l - 1; i++ ){	//the middle four character are letters;
			
			if ( value[i] > '9' )
				return false;
		}
		return true;
	}
	//method calculate hash code
	private int culcHashCode(){
		
		for( char c : value ){
			hash += c;
		}
		
	return hash;
	}
	//method change symbol "d" to "c", if it located on 0, 1, 6, 7 positions
	//and "c" to "d", if it located on positions 2 - 5 inclusively;
	private void valid( char c, char d ){
		try{
			if (value[0] == d )
				value[0] = c;
		
			if (value[1] == d )
				value[1] = c;
		
			if (value[6] == d )
				value[6] = c;
		
			if (value[7] == d )
				value[7] = c;
		
		}catch( ArrayIndexOutOfBoundsException e){
			return;
		}
		
		for( int i=2; i < value.length - 2; i++ ){
				//( value[i] == 1054)
			if ( value[i] == c){				// 1054 - code 'O';
				value[i] = d;
			}
		}
	}
}
