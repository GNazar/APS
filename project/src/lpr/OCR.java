
package lpr;

import static com.googlecode.javacv.cpp.opencv_highgui.*;
import com.googlecode.javacv.cpp.opencv_ml.CvKNearest;
import com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_core.*;

public class OCR {

	private String path = "templates\\";

	//array of characters that is going to be recognized;
	private char[] characters = {'0', '1', '2', '3', '4','5' ,'6', '7', '8', '9', 
				1040, 1042, 1057, 1045, 1053, 1030, 1050, 1052, 1054, 1056, 1058, 1061};
				//'À', 'B', 'C',   'E', 'H',   'I',   'K', 'M', 'O',   'P',   'T',  'X'

	private int train_s;	//number of templates;  
	private final int c = characters.length ;			//number of characters
	
	private CvMat trainData;
	private CvMat trainClasses;
	
	private static int size = 40;		//image size;
	private final int K = 5;
	private CvKNearest knn;
	
	public static int getSize(){
		return size;
	}
	
	public OCR( ){
		
		trainData = cvCreateMat( train_s * c, size*size, CV_32FC1);
		trainClasses = cvCreateMat( train_s * c, 1, CV_32FC1);
		
		getData();	//get templates;
		train();
	}
	
	public OCR( int train, int psize ){
		
		//System.out.println( "NonDefault Constructor" );
		
		if( train > 0 )
			this.train_s = train;
		
		if( psize > 0)
			size = psize;
		
		trainData = cvCreateMat( train_s * c, size*size, CV_32FC1);
		trainClasses = cvCreateMat( train_s * c, 1, CV_32FC1);
		
		getData();	//get templates;
		train();	//train using k-nearest algorithm;
	}
	
	//method read templates from hard disk, 
	//and adapt it for appropriate format; 
	
	public void getData(){
		IplImage src, prs;
		
		CvMat row = new CvMat(), data = new CvMat();
		CvMat row_h = new CvMat();
		 
		String file = new String();
		int i, j;
		
		for ( i = 0; i < c; i++){
			
			for( j = 0; j < train_s; j++ ){
				
				file = path + i + "\\" + j + ".pbm";	//file name training image;
		
				src = cvLoadImage( file, 0);	//load image-template
				
				if( src != null){
					
					prs = ImageProcessing.preprocess(src, getSize(), getSize());
					
					cvGetRow( trainClasses, row, i*train_s + j );
					
					cvSet( row, cvRealScalar( i ) );	//set numbers of characters;
					
					cvGetRow( trainData, row, i*train_s + j );
					
					//convert 8-bit image to 32-float image; 
					IplImage img = IplImage.create(cvSize( size, size ), IPL_DEPTH_32F, 1);
					cvConvertScale( prs, img, 1/255f, 0);	//	1/255 - coefficient for converting 
															//integer value to float(0..1);
				
					//convert matrix "data" to vector "row";
					cvGetSubRect( img, data, cvRect( 0, 0, size, size ) );
					cvReshape( data, row_h, 0, 1 );
					
					cvCopy( row_h, row );
					
					prs.release();
				}
				else{
					System.out.println( "Cant load file " + file );
				}
				
			}
		}
	}
	
	//train using k-nearest algorithm;
	public void train(){
		knn = new CvKNearest( trainData, trainClasses, null, false, K );	
	}
	
	//classify image passed as a parameter;
	public char classiffy( IplImage img ){	
		IplImage prs;
		
		char res;
		
		CvMat data = new CvMat();
		CvMat row_h = new CvMat();
		CvMat nearest = CvMat.create(1, K, CV_32FC1);
		int result;
	//	int accur = 0;
		prs = ImageProcessing.preprocess( img, size, size );		//normalize image;
		
		
		//set data;
		IplImage img32 = IplImage.create(cvSize( size, size ), IPL_DEPTH_32F, 1 );
		cvConvertScale( prs, img32, 1/255f, 0 );
		cvGetSubRect( img32, data, cvRect( 0, 0, size, size ) );
		
		cvReshape( data, row_h, 0, 1 );	
		
		result = (int) knn.find_nearest( row_h, K, null, null, nearest, null );
						// first null – vector with results of prediction  for each input sample. 
						//second null – Optional output pointers to the neighbor vectors themselves.		
		
		img32.release();
		prs.release();
		res = characters[( int)result ];
		
		return res;
	}
	public static void main(String[] args){
	
	} 
}
	
