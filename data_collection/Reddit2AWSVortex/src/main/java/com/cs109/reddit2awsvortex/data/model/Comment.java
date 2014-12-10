package com.cs109.reddit2awsvortex.data.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * Comment entity representing data collected from Reddit comments JSON via 
 * REST API. Class is annotated with DynamoDB Mapper flags for persistence to
 * AWS DynamoDB.
 * @author Dario
 */
@DynamoDBTable(tableName="comment") 
public class Comment {
    
    @DynamoDBHashKey(attributeName="id")
    private String id;
    private String parent_id;
    private String comment_text;
    private Integer score;
    private Integer karma;
    private Integer gilded;
    private String user_id;
    private String subreddit_id;
    private String permalink;
    private Float published_date;
    
    public Comment(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getKarma() {
        return karma;
    }

    public void setKarma(Integer karma) {
        this.karma = karma;
    }

    public Integer getGilded() {
        return gilded;
    }

    public void setGilded(Integer gilded) {
        this.gilded = gilded;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Float getPublished_date() {
        return published_date;
    }

    public void setPublished_date(Float published_date) {
        this.published_date = published_date;
    }

    public String getSubreddit_id() {
        return subreddit_id;
    }

    public void setSubreddit_id(String subreddit_id) {
        this.subreddit_id = subreddit_id;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    @Override
    public String toString() {
        return "Comment{" + "id=" + id + ", parent_id=" + parent_id + ", comment_text=" + comment_text + ", score=" + score + ", karma=" + karma + ", gilded=" + gilded + ", user_id=" + user_id + ", subreddit_id=" + subreddit_id + ", permalink=" + permalink + ", published_date=" + published_date + '}';
    }
    
    public String csvString(){
        return id + "|" + parent_id + "|" + comment_text + "|" + score + "|" + karma + "|" + gilded + "|" + user_id + "|" + subreddit_id + "|" + permalink + "|" + published_date;
    }
}