package com.cs109.reddit2awsvortex;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.cs109.reddit2awsvortex.aws.DynamoConnector;
import com.cs109.reddit2awsvortex.aws.DynamoExportParser;
import java.io.File;

/**
 *
 * @author Dario
 */
public class Program {
    
    public static void main(String[] args) {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials("986769786", "879666=8686868686");
        AmazonS3 s3Client = new AmazonS3Client(awsCreds);
        AmazonDynamoDBClient dynaClient = new AmazonDynamoDBClient(awsCreds);
        DynamoDBMapper dmapper = new DynamoDBMapper(dynaClient);        
        
        //File postCSV = new File("post.csv");        
        //DynamoConnector.harvestPostsFromFile(postCSV, dmapper);        
        
        //File userList = new File("good_users_long.txt");        
        //DynamoConnector.processUsers(userList, dmapper, false, true, false, false, false);
        
        DynamoExportParser.parseCommentsExport();
    }  
}