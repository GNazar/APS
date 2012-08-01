package lpr;

import java.awt.Dimension;
import java.io.File;
import java.util.concurrent.TimeUnit;
import javax.media.CannotRealizeException;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.bean.playerbean.MediaPlayer;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
    
public class MediaPanel extends JFrame
{
	private MediaPlayer mp;
	
  public MediaPanel() {
	  
	  mp = new MediaPlayer();
	  mp.setPlaybackLoop( false );
	  setSize ( new Dimension (640,480 ) );
	  
  }
  public void close(){
	  mp.close();
  }
  
  public void play( String path ){
	  
	  File f = new File ( path );

	  if  ( f.exists() ){

		  path = "file:///" + toURL( path ) + "/flame.avi";
		  final int VIDEO_DURATION = 3500;
		  MediaLocator ml = new MediaLocator( path );
	
		  try {
			  mp.setMediaLocator( ml );	
			  mp.prefetch();
			  setVisible( true ); 
			  add ( mp );
			  mp.start();
	
			  TimeUnit.MILLISECONDS.sleep( VIDEO_DURATION );
		  } catch (InterruptedException e) {
			  System.out.println( "Error! Mediaplayer has been interrupted!" );
			  System.out.println( e.getMessage() );
		  }catch( Exception e){
			  System.out.println( "Error while playing video!" );
			  System.out.println( e.getMessage() );
		  } 
		  setVisible( false ); 
	  }
	  else{
		  System.out.println( "Can't found video! Video must be in current directory." );
	  }
  }
	private static String toURL( String str){
		char[] res = str.toCharArray();
		
		for( int i = 0;i < res.length; i++ ){
			if ( res[i] == '\\')
				res[i] = '/';
		}
		return new String( res );
	}

  
}     
     