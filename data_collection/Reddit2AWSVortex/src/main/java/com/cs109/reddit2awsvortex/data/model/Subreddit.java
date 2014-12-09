package com.cs109.reddit2awsvortex.data.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 *
 * @author Dario
 */
@DynamoDBTable(tableName="subreddit") 
public class Subreddit {
    
    public Subreddit(){}
    
    @DynamoDBHashKey(attributeName="id")
    private String id;
    private String display_name;
    private String title;
    private Integer num_subscribers;
    private Integer created;
    private Boolean isPublic;
    private Boolean mature;
    private String subreddit_type;
    private String submission_type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getNum_subscribers() {
        return num_subscribers;
    }

    public void setNum_subscribers(Integer num_subscribers) {
        this.num_subscribers = num_subscribers;
    }

    public Integer getCreated() {
        return created;
    }

    public void setCreated(Integer created) {
        this.created = created;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getMature() {
        return mature;
    }

    public void setMature(Boolean mature) {
        this.mature = mature;
    }

    public String getSubreddit_type() {
        return subreddit_type;
    }

    public void setSubreddit_type(String subreddit_type) {
        this.subreddit_type = subreddit_type;
    }

    public String getSubmission_type() {
        return submission_type;
    }

    public void setSubmission_type(String submission_type) {
        this.submission_type = submission_type;
    }
    
    public String toCSV(){
        return id + "|" + display_name + "|" + title + "|" + num_subscribers + "|" + created + "|" + isPublic + "|" + mature + "|" + subreddit_type + "|" + submission_type + "\n";
    }
}