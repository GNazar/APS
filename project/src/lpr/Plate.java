
package lpr;

import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;

import java.util.List;

import com.googlecode.javacv.cpp.opencv_core.*;
 

public class Plate {
	IplImage img;
	Number number;
	
	public Plate( ){
		
	}
	
	public Plate ( IplImage image ){
		img = image;
	}
	
	public IplImage getImage(){
		return img;
	}
	
	public Number getValue( OCR ocr ){
		if( number == null )
			recognize( ocr );
		return number;
	}
		
	public boolean isRecognized( ){
		return number != null;
	}

	//method recognize image 
	public Number recognize( OCR ocr){
		
		if ( number == null ){
			
			char[] val = new char[8];	//array which holds number
			ImageProcessing pr = new ImageProcessing( this.img );
			List <IplImage> digits = pr.extract();	//extract digit area;
			int i = 0; 
			for( IplImage tmp : digits){
				val[i++] = ocr.classiffy( tmp );	//recognize;
			}
			number = new Number( val );
		}
		
		return number; 
	}

}
