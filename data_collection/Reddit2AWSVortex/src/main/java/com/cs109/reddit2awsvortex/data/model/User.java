package com.cs109.reddit2awsvortex.data.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 *
 * @author Dario
 */
@DynamoDBTable(tableName="user") 
public class User {
    
    @DynamoDBHashKey(attributeName = "id")
    private String id;
    private String username;
    private Integer karma_link;
    private Integer karma_comment;
    private Integer num_comments;
    private Integer num_posts;
    private Integer num_upvotes;
    private Float last_active;
    private Float account_created;
    private Boolean gold;
    private Boolean mod;
    private Boolean verified;
    
    public User() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getKarma_link() {
        return karma_link;
    }

    public void setKarma_link(Integer karma_link) {
        this.karma_link = karma_link;
    }

    public Integer getKarma_comment() {
        return karma_comment;
    }

    public void setKarma_comment(Integer karma_comment) {
        this.karma_comment = karma_comment;
    }

    public Integer getNum_comments() {
        return num_comments;
    }

    public void setNum_comments(Integer num_comments) {
        this.num_comments = num_comments;
    }

    public Integer getNum_posts() {
        return num_posts;
    }

    public void setNum_posts(Integer num_posts) {
        this.num_posts = num_posts;
    }

    public Integer getNum_upvotes() {
        return num_upvotes;
    }

    public void setNum_upvotes(Integer num_upvotes) {
        this.num_upvotes = num_upvotes;
    }

    public Float getLast_active() {
        return last_active;
    }

    public void setLast_active(Float last_active) {
        this.last_active = last_active;
    }

    public Float getAccount_created() {
        return account_created;
    }

    public void setAccount_created(Float account_created) {
        this.account_created = account_created;
    }

    public Boolean isGold() {
        return gold;
    }

    public void setGold(Boolean gold) {
        this.gold = gold;
    }

    public Boolean isMod() {
        return mod;
    }

    public void setMod(Boolean mod) {
        this.mod = mod;
    }

    public Boolean isVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }    
}