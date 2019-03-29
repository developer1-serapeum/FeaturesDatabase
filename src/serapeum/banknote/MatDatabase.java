package serapeum.banknote;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

/**
 * Store Mat and MatOfKeyPoint objects into SQLite database. 
 * 
 * @author developer1.serapeum@gmail.com
 */

public class MatDatabase {

    private String DB_NAME = "";

    /**
     * Create a database to store the features of an image.
     *
     * @param dbName: the database name with extension, e.g. features.db
     * @param folders: the folders that contains the images from which
     * we need to extract the features
     */
    public void createNewDatabase(String dbName, String[] folders) {

        this.DB_NAME = dbName;
        String url = "jdbc:sqlite:databases/" + dbName;

        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {

            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("A new database has been created with driver " + meta.getDriverName());

                for (int i=0; i< folders.length; i++) {

                    stmt.execute(createTable(folders[i]));

                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Connect to the features.db database
     *
     * @return the Connection object
     */
    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:databases/" + this.DB_NAME;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Creates an SQL statement for table creation
     * 
     * @param tableName: The name of table within the database.
     * @return a string that contains an SQL statement
     */
    String createTable(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName + " (\n"
                + "    name text PRIMARY KEY,\n"
                + "    rows integer NOT NULL,\n"
                + "    cols integer NOT NULL,\n"
                + " keypoints blob NOT NULL,\n"
                + " descriptors blob NOT NULL\n"
                + ");";
    }

    /**
     * Inserting a new row in a table
     * 
     * @param name
     * @param countryCode
     * @param rows
     * @param cols
     * @param keypoints
     * @param descriptors
     */
    public void insert(String name, String tableName, int rows, int cols, MatOfKeyPoint keypoints, Mat descriptors) {

        String sql = "INSERT INTO " + tableName + " VALUES(?,?,?,?,?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, rows);
            pstmt.setInt(3, cols);
            pstmt.setBytes(4, matOfKeyPointToBlob(keypoints));
            pstmt.setBytes(5, matToBlob(descriptors));

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * Covert OpenCV Mat object into Java byte array
     * 
     * @param mat: the input OpenCV Mat
     * @return byte array  with the Mat contents
     */
    private byte[] matToBlob(Mat mat) {

        // make a spot to save the data
        int[] data = new int[(int) mat.total() * mat.channels()];

        // load the data
        mat.convertTo(mat, CvType.CV_32S);
        mat.get(0,0, data);

        ByteBuffer buffer = ByteBuffer.allocate(data.length * 4);

        for (int i = 0; i < data.length; i++){
            buffer.putInt(data[i]);
        }

        byte[] byteArray = buffer.array();

        return byteArray;

    }

    /**
     * Covert OpenCV MatOfKeyPoint object into Java byte array
     * 
     * @param mat: The OpenCV object MatOfKeypoint
     * @return byte array  with the MatOfKeypoint contents
     */
            
    private byte[] matOfKeyPointToBlob(MatOfKeyPoint mat) {

        // make a spot to save the data
        float[] data = new float[(int) mat.total() * mat.channels()];

        // load the data
        mat.convertTo(mat, CvType.CV_32F);
        mat.get(0,0, data);

        ByteBuffer buffer = ByteBuffer.allocate(data.length * 8);

        for (int i = 0; i < data.length; i++){
            buffer.putFloat(data[i]);
        }

        byte[] byteArray = buffer.array();

        return byteArray;

    }

}
