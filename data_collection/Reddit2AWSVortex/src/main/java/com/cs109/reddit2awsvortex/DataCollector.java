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
 * DataCollector is typically initiated by a shell script (acquire_deploy.sh). The shell script downloads a ZIP package
 * containing the DataCollector JAR file and requisite files. The script calls the JAR with certain command line 
 * arguments that determine whether the Collector will download a configuration file from AWS S3, use a custom file locally
 * supplied, or rever to a default one. The configuration determines what types of data the program execution will collect and process.
 * 
 * The intention beyond this design is that due to Reddit's metered (request / 2 seconds) API access from a single IP address
 * executing a multithreaded program from a single machine was not optimal, rather it was more desirable to provision a cluster of 
 * machine instances (AWS EC2) in the cloud and have them all rely on the same program, but make the program configuration dynamic and hosted
 * in the cloud such that the machines could work in concert on different pieces of the same Big Data puzzle.
 * 
 * DataCollector harvests JSON data from Reddit using its public REST API, stores the data in AWS DynamoDB NoSql database for further 
 * processing and analysis. It also reads in data files in various formats for data upload or transformation (ie: CSV export).
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
        BasicAWSCredentials awsCreds = new BasicAWSCredentials("accesskey", "secretkey");
        
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
        
        // Collect Subreddits and their associated Posts
        if (config.getCollectSubreddits()){
            DynamoConnector.harvestSubreddits(1000, dmapper, config.getCollectSubredditPosts()); 
        }
        
        // Process Posts from supplied CSV file and associted Comments
        if (config.getCollectPostsFromFile()){
            File postCSV = new File("post.csv");        
            DynamoConnector.processPosts(postCSV, dmapper, 1);             
        }       
        
        // Collect User data, which may include account metadata, posts, comments, likes, and dislikes
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
        
        // Process DynamoDB export file of 'comments' table data, generating a CSV file from it.
        if (config.getProcessCommentsExport()){
            DynamoExportParser.parseCommentsExport();
        }
    }  
}