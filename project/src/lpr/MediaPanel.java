package lpr;

import java.awt.Dimension;
import java.io.IOException;
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
	  
	  final int VIDEO_DURATION = 3200;
	  MediaLocator ml = new MediaLocator( path );
	  
	  mp.setMediaLocator( ml );	
	  mp.prefetch();
	  setVisible( true ); 
	  add ( mp );
	  mp.start();
	  
	  try {
		TimeUnit.MILLISECONDS.sleep( VIDEO_DURATION );
	} catch (InterruptedException e) {
		System.out.println( "Error! Mediaplayer has been interrupted!" );
		System.out.println( e.getMessage() );
	}
	 setVisible( false ); 
	  
  }
  
}     
     