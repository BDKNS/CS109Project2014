package com.cs109.reddit2awsvortex;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The CollectorConfiguration constructor takes a .properties file as a parameter
 * and as part of its init() method reads in this file and sets a series of member fields
 * which are then used to govern the behavior of the DataCollector program. These class fields 
 * specify logic that collects Reddit subreddit, post, comment, and user data.
 * @author Dario
 */
public class CollectorConfiguration {
    
    private final String CONFIG_PROPS;
    
    private Boolean collectSubreddits = Boolean.FALSE;
    private Boolean collectSubredditPosts = Boolean.FALSE;
    private Boolean collectPostsFromFile = Boolean.FALSE;
    
    private Boolean collectUser = Boolean.FALSE;
    private Boolean collectUserMeta = Boolean.FALSE;
    private Boolean collectUserPosts = Boolean.FALSE;
    private Boolean collectUserComments = Boolean.FALSE;
    private Boolean collectUserLikes = Boolean.FALSE;
    private Boolean collectUserDislikes = Boolean.FALSE;   
    
    private Boolean processCommentsExport = Boolean.FALSE;

    CollectorConfiguration(String configProps) {
        this.CONFIG_PROPS = configProps;
    }

    public void init() {
        readConfigProps();
    }

    /***
     * readConfigProps() initiates the reading in of a specified .properties file
     * using the Java Util package property reader and stores the values in class level
     * variables, made available for elsewhere in the program by accessor methods.
     */
    private void readConfigProps() {
        Properties prop = new Properties();

        try {
            prop.load(new FileInputStream(CONFIG_PROPS));
            
            collectSubreddits = Boolean.parseBoolean(prop.getProperty("SUBREDDITS"));
            collectSubredditPosts = Boolean.parseBoolean(prop.getProperty("POSTS"));
            collectPostsFromFile = Boolean.parseBoolean(prop.getProperty("POST_FILE"));
            
            collectUser = Boolean.parseBoolean(prop.getProperty("USER"));
            collectUserMeta = Boolean.parseBoolean(prop.getProperty("USER_META"));
            collectUserPosts = Boolean.parseBoolean(prop.getProperty("USER_POSTS"));
            collectUserComments = Boolean.parseBoolean(prop.getProperty("USER_COMMENTS"));
            collectUserLikes = Boolean.parseBoolean(prop.getProperty("USER_LIKES"));
            collectUserDislikes = Boolean.parseBoolean(prop.getProperty("USER_DISLIKES"));
            
            processCommentsExport = Boolean.parseBoolean(prop.getProperty("PROCESS_COMMENTS"));

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
    }

    public String getCONFIG_PROPS() {
        return CONFIG_PROPS;
    }

    public Boolean getCollectSubreddits() {
        return collectSubreddits;
    }

    public Boolean getCollectSubredditPosts() {
        return collectSubredditPosts;
    }

    public Boolean getCollectPostsFromFile() {
        return collectPostsFromFile;
    }

    public Boolean getCollectUser() {
        return collectUser;
    }

    public Boolean getCollectUserMeta() {
        return collectUserMeta;
    }

    public Boolean getCollectUserPosts() {
        return collectUserPosts;
    }

    public Boolean getCollectUserComments() {
        return collectUserComments;
    }

    public Boolean getCollectUserLikes() {
        return collectUserLikes;
    }

    public Boolean getCollectUserDislikes() {
        return collectUserDislikes;
    }

    public Boolean getProcessCommentsExport() {
        return processCommentsExport;
    }
}