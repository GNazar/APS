
package lpr;

import java.util.*;

import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_core.*;

public class ImageProcessing {
	
	private IplImage img;
		
	private int RATIO;	// permissible image aspect ratio;
	public final  static float ACCUR = 0.07f;	//noises;
		
	public IplImage getImg(){
		return img;
	}
//==========================================================================//	
	public ImageProcessing( IplImage image ){
		RATIO = 4;
		img = image;
	}
	
	public ImageProcessing(IplImage fname, int ratio, float accurency){
		this( fname );
		this.RATIO = ratio;
	}
	
	public void release(){
		img.release();
	}
	
	// return list of digit areas;	
	 public List<IplImage> extract(){
		 
		 List <IplImage> res = new LinkedList<IplImage>();
		 P x, y; 
		 int start = 0;
		 CvRect rect;
		 CvMat data = new CvMat();
		 IplImage digit;
		 
		x = getHDigitBound( start );					//get horizontal coordinates of plate area;
		while( x!=null ){	
			y = getVDigitBound( x );					//get vertical coordinates of digit area; 
			rect = new CvRect( x.min, y.min, x.max - x.min + 1, y.max - y.min +1 );
			
			cvGetSubRect (img, data, rect );												//	
			digit = cvCreateImageHeader( cvSize( rect.width(), rect.height() ), 8, 1);		//	extract digit area;	
			digit = cvGetImage( data, digit );												//

			res.add( digit );
			start = x.max;
			x = getHDigitBound( start );	//get horizontal coordinates of plate area;
		}
		 data.release();
		 
		return res;
	 }
	//================================================================//
	 //method scale image passed through parameter "src" to size "width", "height"; 
	 public static IplImage preprocess( IplImage src, int width, int height ){
		IplImage res = IplImage.create( cvSize( width, height ), 8, 1);
		cvResize( src, res, CV_INTER_NN );
		return res;
	 }


	//==========================================================================//	
	//function finds extreme vertical point area that is going to be recognized;
	private int getBound( final CvMat data, final int step ){
		int i = 0, d = 0;
		int c = (step > 0) ? 0 : img.height()-1;
		
		long D = Math.round( img.height() * 0.03 );		//noises threshold 	
		while( ( D > d) && ( i < img.height() / 2 ) ){
			
			if ( data.get( c ,0 ) != 0 )
				d++;
			
			c += step;
			i++;
		}
		
		return c;
	}
	
	//===================================================================//
	//return percentage black and white points by x;
	private float isHSpace( int  x ){
		int top, bottom; 
		CvMat data = new CvMat();
		
		cvGetCol( img, data, x );
		
		top = getBound( data, 1 ); 		//top image point;
		bottom = getBound ( data, -1 );	//bottom image point;
		
		if( bottom - top < img.height()/2 ) return 1;
		
		int black = 0, white = 0;
		for(int i = top; i < bottom; i++){	//count amount of black and white points;
			
			if( data.get( i, 0 ) == 0)
				black++;
			else white++;
		}
		data.release();
		return black / (float)white;	//percentage;
	}
	
//===============================================//	
	//return percentage black and white points by y;
	private float isVSpace( int y, final P p ){
		int black = 0, white = 0;
		
		CvMat data = new CvMat();
		cvGetRow( img, data, y);
		
		for( int i = p.min; i < p.max; i++ ){
			
			if (data.get(0 , i) == 0 )
				black++;
			else white++;
		}
		data.release();
		return black / (float)white;	//percentage; 
	}
	
//===============================================//	
	//function return image bounds by X;
	private P getHDigitBound( int start ){
		
		boolean found = false, beg = true; 
		int left = 0, right = 0;
		
		for( int i=start + 1; i < img.width(); i++ ){
			
			if( isHSpace( i ) < ACCUR ){
				
				if( found ){ 		// found the end of digit;
					found = false;
					beg = false;
					
					if( img.height()/( right-left ) < RATIO ){ 
						return new P( left, right );
					}
				}
				
				beg = true;
				left = i;
			}else{
				
				if( beg ){		//the beginning of digit is found; 
					found = true;
					right = i;
				}
			}
		}//for;
		
		return null;
		
	}
	
//===============================================//	
	//function return image bounds by Y;
	private P getVDigitBound( final P b){
		int m = img.height()/2;
		int top, bottom = img.height()-1;
		
		while( ( isVSpace( m , b ) > ACCUR ) && ( m > 0 ) ){	//find top point;
			m--;
		}
		top = m;
		
		m = img.height()/2;
		while( ( isVSpace( m , b ) > ACCUR ) &&( m < bottom ) ) {	//find bottom point;
			m++;
		}
		bottom = m;
		
		return new P( top, bottom );
	}
	
}
//auxiliary class, which holds two integer values  
class P{
	final int min;
	final int max;
		
	P( int min, int max ){
		this.min = min;
		this.max = max;
	}
}
