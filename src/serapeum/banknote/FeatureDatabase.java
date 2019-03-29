package serapeum.banknote;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class FeatureDatabase {
    public static void main(String[] args) {
                
        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Compute descriptors and store in the database
        FeatureDatabaseRun featureDatabaseRun = new FeatureDatabaseRun();
        featureDatabaseRun.run(args);

    }
}

class FeatureDatabaseRun {
    
    Scalar color = new Scalar(255, 0, 0);

    String trainImagePath = "images";
    String masksPath = "";
    
    // The required folders within images directory 
    String[] folders = {"egypt_banknote_back", "egypt_banknote_forward"};
    
    // These are debug outputs only
    String keypointsPath = "";
    String descriptorsPath = "";
    
    ArrayList<MatOfKeyPoint> trainKeypointsList = new ArrayList<>();
    ArrayList<Mat> trainDescriptorsList = new ArrayList<>();
    ArrayList<Mat> trainImageList = new ArrayList<>();
    

    /**
     * Scan the training images directories and read all
     * images within. Each directory will have a table within
     * the database.
     * 
     * For each image KeyPoints are extracted and Descriptors
     * are computed. Finally, everything is stored in a database
     * with the training image name as primary key.
     */

    public void run(String[] args) {
        
        // Create an empty database
        MatDatabase matDatabase = new MatDatabase();
        matDatabase.createNewDatabase("features.db", folders);

        for (int i = 0; i < folders.length; i++) {
                    
            // input images 
            trainImagePath = "images/" + folders[i];
         
            // debug output
            keypointsPath = "debug/keypoints" ;
            descriptorsPath = "debug/descriptors";
            
            File folder = new File(trainImagePath);
            
            File[] listOfFiles = folder.listFiles(); 
            
            // loop over all images within each folder
            for (int k = 0; k < listOfFiles.length; k++) {
                
                String imageName = listOfFiles[k].getName();
                System.out.println(imageName);
                
                Mat trainImageRaw = imageRead(folder + "/" + imageName);
                
                // Reduce noise by filtering (optional step)
                Mat trainImage = reduceNoise(trainImageRaw);

                // detect KeyPoints
                Mat mask = new Mat();
                MatOfKeyPoint trainKeypoints = new MatOfKeyPoint();
                detectKeypoints(trainImage, trainKeypoints, mask);
                
                // Compute descriptors
                Mat descriptors = new Mat();
                computeDescriptors(trainImage, trainKeypoints, descriptors);
                
                // Write key points, descriptors to the database
                matDatabase.insert(
                        imageName,
                        folders[i],
                        descriptors.rows(),
                        descriptors.cols(),
                        trainKeypoints,
                        descriptors);
                
                // debug output: draw KeyPoints over images
                Mat outImage = new Mat(); 
                Features2d.drawKeypoints(trainImage, trainKeypoints, outImage, new Scalar(0, 0, 255));
                imageWrite(keypointsPath + "/"+imageName , outImage);
                
                // debug output: write key points and descriptors to files
                matToFile(keypointsPath + "/key_"+imageName+".txt", trainKeypoints);
                matToFile(descriptorsPath + "/desc_"+imageName+".txt", descriptors);

                System.out.println("**------------------------------**");

            }

        }

        System.out.println("Finished keypoints extraction" );
        
        
        System.exit(0);
    }
            
    /**
     * Remove noise with Bilateral filter
     * @param src: The image to be modified
     * @return A noise reduced Mat with same dimensions
     */
    private Mat reduceNoise(Mat src) {

        Mat dst = new Mat();

        int _distance = 8;
        int _sigmaColor = 8;
        int _sigmaSpace = 12;

        Imgproc.bilateralFilter(src, dst, _distance, _sigmaColor, _sigmaSpace);

        return dst;
    }

    void detectKeypoints(Mat image, MatOfKeyPoint keypoints, Mat mask){

        ORB detector = ORB.create();
        detector.detect(image, keypoints, mask);
        
    }
    
    void computeDescriptors(Mat image, MatOfKeyPoint keypoints, Mat descriptors){

        ORB descriptor = ORB.create();
        descriptor.compute(image, keypoints, descriptors);
    }

    Mat imageRead(String imageFilename) {

        Mat src = new Mat();
        
        src = Imgcodecs.imread(imageFilename, Imgcodecs.IMREAD_GRAYSCALE);
        
        if( src.empty() ) {
            System.out.println("Error opening image: " + imageFilename);
            System.exit(-1);
        }
        
        return src;

    }
    
    boolean imageWrite(String imageName, Mat image){

        String filename = imageName;
        
        try {
            Imgcodecs.imwrite(filename, image);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    void matToFile(String filePath, Mat mat) {

        try {
            FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            writer.write(mat.dump());

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

            
}
