package lpr;


import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import java.util.LinkedList;

import com.googlecode.javacpp.*;

public class Extracter {

	private IplImage res;
	private IplImage image;
	private CvMat mat;
	

	
	private final int threshold = 120;	
	
	private final int minlength = 150;			//min car plate length; 
	private int maxlength = 220;				//max car plate length;
	
	private final int minheight = 40;				//min car plate height;
	private int maxheight = 100;					//max car plate height;
	
	private final float maxcorr = 6.7f;			//max correlation of width and height;
	private final float mincorr = 3.7f;			//min correlation of width and height;
	
	private final float maxpcorr = 5f;		//max correlation of white and black points;
	private final float minpcorr = 2f;			//min correlation of white and black points;
	
	public Extracter ( ){
		
	}
	
	public void release(){
		res.release();
		image.release();
		mat.release();
	}
	
	//method extract plate area
	public IplImage extract( IplImage img ){
		
		int x, y, width, height;
		IplImage tmp;
		CvMat data = new CvMat();
		CvRect rect;

		x = ( int )Math.round( img.width() * 0.15 );								//
		y = ( int )Math.round( img.height() * 0.25 );								//
		width = ( int )Math.round( img.width() * 0.7 );								//	cut image area where car plate  
		height = ( int )Math.round( img.height() * 0.5 );							//	will be searching
																					//
		rect = new CvRect (x, y, width, height );									//
		cvGetSubRect (img, data, rect );											//
		tmp = cvCreateImageHeader( cvSize( rect.width(), rect.height() ), 8, 1);	//		
		tmp = cvGetImage( data, tmp );												//		
		
		image = IplImage.create( tmp.cvSize(), 8, 1 );								//	convert to grayscale;
		cvCvtColor( tmp, image, CV_RGB2GRAY );										//
		bin( threshold );															//binarize image;
		
		IplImage result;
		LinkedList< Integer > list = this.getVerticalSpaces();
		rect = this.getPlateArea( list );
		data.release();
		
		if (rect == null) return null;	//plate not found;
		data = new CvMat();
		cvGetSubRect (image, data, rect );											//
		result = cvCreateImageHeader( cvSize( rect.width(), rect.height() ), 8, 1);	//	cut plate;		
		result = cvGetImage( data, result );										//
		cvSaveImage ( "hc" + rect.y() + ".jpg", img );
		return result;
	}
	
	//method return list vertical spaces where might be number;
	private LinkedList< Integer > getVerticalSpaces(){
		 	
		int dist = 0;
		int prev = 0;
		CvMat data;
		LinkedList <Integer> list = new LinkedList<Integer>();

		this.getContours();	//find contours;
		
		for( int i = 0; i < res.height(); i++ ){
			
			data = new CvMat();
			cvGetRow( res, data, i);

			if ( this.isNumberArea( data ) ){	//check width of area where might be number; 
				dist = i - prev;
				
				if( ( dist > minheight) && ( dist < maxheight) ){	//check height of area where 
																	//might be number;
					list.add( prev );
					list.add( i );
				}
				prev = i;		
			}
			data.release();
		}	
		return list;
	}
	
	// if car plate is found method return its bounds, 
	//in other case function return null
	private CvRect getPlateArea( LinkedList< Integer > list ){
		
		int top, bot;
		int dist;			//distance between horizontal spaces; 
		int sum;
		int[] hbounds;		//image bounds by X;
		
		CvMat data = new CvMat();
		LinkedList <Integer> bounds = new LinkedList <Integer>();
		
		for ( int i = 0; list.size() > 1; i++ ) {
			
			top = list.poll();
			bot = list.pop();
			
			dist = bot - top;			
			cvGetCol( res, data, i);	
			
			//searching for number in horizontal space 
			//with bounds - top and bot;
			for (int j = 0; j < res.width(); j++ ){
				sum = 0; 
				for ( int k = top; k < bot; k++ ){		//	calculate number of white points in column;
					if ( data.get(k, j) > 0 )
						sum++;
				}
				
				if (sum > Math.round( 0.8*dist ) ){		//is line;			
					bounds.add( j );							
				}
			}//for;
			
			hbounds = this.extractPlate(bounds, top, bot );
			bounds.clear();
			
			if ( hbounds != null ){

				data.release();
				return new CvRect( hbounds[0], top , hbounds[1] - hbounds[0], bot - top );
			}					
		}
		data.release();
		return null;		//area is not found;		
	}
	
	// method detect car plate coordinates by X in vertical space; 
	private int[] extractPlate( LinkedList <Integer> bounds, int top, int bot ){
		
		int left = 0, right = 0;
		int width, height = bot - top;
		
		// white, black -  number white and black points in each column; 
		//pwhite, pblack - number white and black points in area which can contain car plate;
		//pcorr, corr - corelation white and black points
		float white, black, pwhite, pblack, pcorr, corr;
		
		double point;
		boolean w = false;
		if ( bounds.size() > 0 )
			left = bounds.pop();
		
		while( bounds.size() > 0 ){
			right = bounds.pop();
			
			width = right - left;
			pwhite = 0;
			pblack = 0;
			corr = width / height;

			if ( ( corr > mincorr ) && ( corr < maxcorr ) ){	
				int k = 0;
				for (int i = left; i < right; i++ ){	//count number of black and white points;
					white = 0; black = 0;
					
					for ( int j = top; j < bot; j++ ){
						point = mat.get(j, i); 
						
						if ( point > 0 )
							white++;
						else black++;	
					}
					//count number of transitions from white to black;
					if ( white / black > 15 ){		//white space;
						w = true;
					}else
						if ( w ){
							k++;
							w = false;
						}
					
					pwhite += white;
					pblack += black;
				}//internal for;
				
				pcorr = pwhite/pblack;
				
				if ( ( pcorr > minpcorr ) && ( pcorr < maxpcorr ) && ( k > 5 ) ) {
					return new int[]{ left, right };		
				}
				 
			}//if;
			left = right;	 
		}
		return null;	
	}
	
	//method create image with contours;
	private void getContours(){
		
		CvSeq cont = new CvSeq();
		CvMemStorage storage =  CvMemStorage.create();
		res = image.clone();
		cvFindContours( res, storage, cont, Loader.sizeof(CvContour.class),				//get external contours in "storage" 
				/*CV_RETR_EXTERNAL*/CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE );
		
		res.release();
		res = IplImage.create( image.cvSize(), 8, 3);	//image with contours;
		
		cvDrawContours( res, cont, CV_RGB(255,255,255), CV_RGB(255,255,255),1 , 2 , CV_AA, new CvPoint( 0, 0 ) ); 		
		storage.release();
	}
	
	private boolean isNumberArea ( CvMat data ){
		
		int white = 0;
		
		for ( int j = 0; j < res.width(); j++ ){
			
			while ( ( j < res.width() ) && ( data.get(0 , j) > 0 ) ){	//count number of white points;
				white++;
				j++;
			}
			
			if ( ( white > minlength ) && ( white < maxlength ) ){
				
				j = res.width();	//exit loop;
				return true;
			}	
		}	
		return false;
	} 
	
	//function convert grayscale  image to black and white;
	private void bin( double min ){	
		cvThreshold(image, image, min, 255, CV_THRESH_BINARY);
		mat = image.asCvMat();
	}
	
}


