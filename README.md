# Features Database

This class provides a way to store Mat objects into a database in Java for Android applications.

## Why is it needed?

In image processing applications it commonly required to read an image then calculate its features.
The features are calculated from input images and returned as keypoints and descriptors.

Suppose that you need to repeat this operation every time your Android app is initialized, then you
will waste lots of time to read the image, calulate keypoints, then compute the descriptors.

The soltion that is provided here is that you extract keypoints and compute descriptors only once,
and store them in SQLite database. This is done away from the Android in a Java application then 
the database is placed in Android under the assets folder.

This will save Android application initializtion time dramatically because you are reading from
database. It reduces also the APK size because we don't need to store the images in the assets
folder, but only the Mat objects of the keypoints and the descriptors.

## How to install


Clone the source code and the eclipse project

```
```

Download Eclipse for Java developers


Install OpenCV 3.4.1 or above


Add SQLite JDBC Driver JAR file to a Java project
 


## Related references

* http://answers.opencv.org/question/8873/best-way-to-store-a-mat-object-in-android/
* https://stackoverflow.com/questions/21849938/how-to-save-opencv-keypoint-features-to-database