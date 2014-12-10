package com.cs109.reddit2awsvortex;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.cs109.reddit2awsvortex.aws.DynamoConnector;
import com.cs109.reddit2awsvortex.aws.DynamoExportParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dario
 */
public class DataCollector {
    
    public static final String S3_BUCKET = "cs109-team-project";
    public static final String S3_KEY_POSTFIX = "_collectorConfig.properties";
    public static final String COLLECTOR_PROPS = "properties\\configuration.properties";

    // TODO --- Let's not have this static, change later.
    private static CollectorConfiguration configM;
    
    public static void main(String[] args) {        
        
        // AWS Credentials, requiring IAM Role with S3 & Dynamo Access
        BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAJUC3SGEQYGLT7ZZA", "vYMWq6G5/YWlXB/oPBe4uUBcl7zZP+mTO5hAUjEd");
        
        // Create AWS clients for S3 and DynamoDB services.
        AmazonS3 s3Client = new AmazonS3Client(awsCreds);
        AmazonDynamoDBClient dynamoClient = new AmazonDynamoDBClient(awsCreds);
        DynamoDBMapper dmapper = new DynamoDBMapper(dynamoClient);  
        
        CollectorConfiguration config = null;
        
        if (args.length > 0){            
            if (args[0].equalsIgnoreCase("AWS") && (args[1] != null)){
                String fileName = args[1] + S3_KEY_POSTFIX;
                S3Object object = s3Client.getObject(
                        new GetObjectRequest(S3_BUCKET, fileName));
                InputStream objectData = object.getObjectContent();
                
                try{
                    objectData.close();             
                    Files.copy(objectData, Paths.get(fileName));
                } catch (IOException ex) {
                    Logger.getLogger(DataCollector.class.getName()).log(Level.SEVERE, null, ex);
                }                
                config = new CollectorConfiguration(fileName);
            }            
            else if (args[0].equalsIgnoreCase("CUSTOM")) {                
                config = new CollectorConfiguration(args[1]);
            }         
            else{
                System.err.println("ERROR: You must supply CLI arguments for config type");
                System.exit(0);
            }
        }
        
        else{
            config = new CollectorConfiguration(COLLECTOR_PROPS);         
        }
        
        config.init();
        configM = config;
        
        if (config.getCollectSubreddits()){
            DynamoConnector.harvestSubreddits(1000, dmapper, config.getCollectSubredditPosts()); 
        }
        
        if (config.getCollectPostsFromFile()){
            File postCSV = new File("post.csv");        
            DynamoConnector.processPosts(postCSV, dmapper, 1);             
        }       
        
        if (config.getCollectUser()){
            File userList = new File("good_users_long.txt");        
            DynamoConnector.processUsers(userList, 
                    dmapper, 
                    config.getCollectUserMeta(), 
                    config.getCollectUserPosts(), 
                    config.getCollectUserComments(), 
                    config.getCollectUserLikes(), 
                    config.getCollectUserDislikes());
        }
        if (config.getProcessCommentsExport()){
            DynamoExportParser.parseCommentsExport();
        }
    }  
}