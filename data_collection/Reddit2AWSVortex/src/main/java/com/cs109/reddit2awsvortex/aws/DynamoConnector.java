package com.cs109.reddit2awsvortex.aws;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.cs109.reddit2awsvortex.data.model.Comment;
import com.cs109.reddit2awsvortex.data.model.Post;
import com.cs109.reddit2awsvortex.data.model.Subreddit;
import com.cs109.reddit2awsvortex.data.model.User;
import com.cs109.reddit2awsvortex.data.model.UserComment;
import com.cs109.reddit2awsvortex.data.model.UserDisliked;
import com.cs109.reddit2awsvortex.data.model.UserLiked;
import com.cs109.reddit2awsvortex.data.model.UserSubmitted;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Scanner;
import java.util.UUID;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Dario
 */
public class DynamoConnector {   
    
    public static void harvestSubreddits(Integer subredditLimit, DynamoDBMapper dmapper){
        try {

            Client client = Client.create();
            Writer csvOutput;
            csvOutput = new BufferedWriter(new FileWriter("subreddit.csv", true));
            csvOutput.append("id| display_name| title| num_subscribers| created| public| mature | subreddit_type | submission_type \n");
            csvOutput.close();

            String after = "7878";
            for (int i = 100; i <= 15000; i = i + 100) {

                int httpStatus;
                ClientResponse response;
                do {
                    Thread.sleep(2050);
                    WebResource webResource;
                    if (i == 100) {
                        webResource = client.resource("http://www.reddit.com/subreddits/popular.json?limit=100");
                    } else {
                        webResource = client.resource("http://www.reddit.com/subreddits/popular.json?limit=100" + "&after=" + after);
                    }
                    //WebResource webResource = client.resource("http://www.reddit.com/subreddits/popular.json?limit=1000" + "&after=" + after);
                    response = webResource.accept("application/json").get(ClientResponse.class);
                    httpStatus = response.getStatus();

                } while (httpStatus != 200);

                String output = response.getEntity(String.class);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode actualObj = mapper.readTree(output);
                JsonNode data = actualObj.get("data");
                JsonNode children = data.get("children");

                System.out.println("children size =" + children.size());
                System.out.println("Output from Server .... \n");
                System.out.println(children.toString());

                if (children.isArray()) {
                    for (final JsonNode objNode : children) {
                        after = objNode.get("kind").getTextValue() + "_" + objNode.get("data").get("id").getTextValue();
                        JsonNode subredditData = objNode.get("data");

                        System.out.println(subredditData.get("display_name").getTextValue() + " --- " + subredditData.get("title").getTextValue());

                        // Create a new Subreddit
                        Subreddit subReddit = new Subreddit();
                        subReddit.setId(subredditData.get("id").getTextValue());
                        subReddit.setDisplay_name(subredditData.get("display_name").getTextValue());
                        subReddit.setTitle(subredditData.get("title").getTextValue());
                        subReddit.setNum_subscribers(subredditData.get("subscribers").getIntValue());
                        subReddit.setCreated(subredditData.get("created").getIntValue());
                        subReddit.setMature(subredditData.get("over18").getBooleanValue());
                        if (subredditData.get("subreddit_type").getTextValue().equalsIgnoreCase("public")) {
                            subReddit.setIsPublic(Boolean.TRUE);
                        } else {
                            subReddit.setIsPublic(Boolean.FALSE);
                        }
                        subReddit.setSubreddit_type(subredditData.get("subreddit_type").getTextValue());
                        subReddit.setSubmission_type(subredditData.get("submission_type").getTextValue());

                        // Save the subreddit to DynamoDB
                        //dmapper.save(subReddit); 
                        if (!subReddit.getDisplay_name().equalsIgnoreCase("ads") && !subReddit.getDisplay_name().equalsIgnoreCase("promos")) {
                            DynamoConnector.harvestSubredditPosts(subReddit.getDisplay_name(), dmapper);
                        }
                        csvOutput = new BufferedWriter(new FileWriter("subreddit.csv", true));
                        csvOutput.append(subReddit.toCSV());
                        csvOutput.close();
                    }
                }             
            }
            //csvOutput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }    
    
    public static void harvestSubredditPosts(String subreddit, DynamoDBMapper dmapper){
        
        try {
            Client client = Client.create();
            
            String after = null;
            for (int i = 100; i <= 500; i = i + 100) {

                int httpStatus;
                ClientResponse response;
                do {
                    Thread.sleep(2050);
                    System.out.println("** Attempting post collection for subreddit, " + subreddit);
                    WebResource webResource;
                    if (i == 100) {
                        webResource = client.resource("http://www.reddit.com/r/"+subreddit+"/top.json?limit=100");
                    } else {
                        webResource = client.resource("http://www.reddit.com/r/"+subreddit+"/top.json?limit=100" + "&after=" + after);
                    }

                    response = webResource.accept("application/json").get(ClientResponse.class);
                    httpStatus = response.getStatus();

                } while (httpStatus != 200);

                String output = response.getEntity(String.class);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode actualObj = mapper.readTree(output);
                JsonNode data = actualObj.get("data");
                JsonNode children = data.get("children");

                System.out.println(subreddit + " Comment Iteration: " + i);

                if (children.isArray()) {
                    for (final JsonNode objNode : children) {
                        after = objNode.get("kind").getTextValue() + "_" + objNode.get("data").get("id").getTextValue();
                        JsonNode postData = objNode.get("data");

                        // Create a new Subreddit
                        Post post = new Post();
                        post.setId(postData.get("id").getTextValue());
                        post.setTitle(postData.get("title").getTextValue());
                        post.setSubreddit_title(subreddit);
                        post.setSubreddit_id(postData.get("subreddit_id").getTextValue());
                        //post.setSelf_post(postData.get("id").getBooleanValue());
                        post.setNum_comments(postData.get("num_comments").getIntValue());
                        post.setScore(postData.get("score").getIntValue());
                        //post.setText_content(postData.get("id").getTextValue());
                        post.setGilded(postData.get("gilded").getIntValue());
                        post.setPublished_date(new Float(postData.get("created").getIntValue()));
                        post.setEdited(postData.get("edited").getBooleanValue());
                        post.setMature(postData.get("over_18").getBooleanValue());
                        post.setUser_id(postData.get("author").getTextValue());
                        post.setUrl(postData.get("url").getTextValue());
                        post.setPermalink(postData.get("permalink").getTextValue());                        
                       
                        // Save the post to DynamoDB
                        dmapper.save(post);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }  
    
    public static void harvestPostsFromFile(File postCSV, DynamoDBMapper dmapper){        

        try {
            Scanner sc = new Scanner(postCSV);

            int postIndex = 1;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] arr = line.split(",");
                System.out.println("*** Processing post #"+postIndex);
                
                if (postIndex >= 35000){
                                
                    for (int i = 0; i <arr.length; i++){
                        if (arr[i].contains("/r/") && arr[i].substring(0, 3).contains("/r/") && !arr[i].contains("?"))
                        {
                            System.out.println(arr[i]);
                            DynamoConnector.harvestPostComments(arr[i], dmapper);

                            break;
                        }
                    }
                }                
                postIndex++;
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }        
    }
    
    public static void harvestPostComments(String permalink,  DynamoDBMapper dmapper){
        
        try {
            Client client = Client.create();

                int httpStatus;
                ClientResponse response;
                int retryCnt = 0;
                do {
                    Thread.sleep(2050);
                    System.out.println("** Attempting comment collection for post, " + permalink);
                    WebResource webResource;
                    webResource = client.resource("http://www.reddit.com" + permalink + ".json?limit=100");
                    response = webResource.accept("application/json").get(ClientResponse.class);
                    httpStatus = response.getStatus();
                    retryCnt++;

                } while (httpStatus != 200 && retryCnt < 40);
                
                if (httpStatus != 200){
                    return;
                }

                String output = response.getEntity(String.class);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode actualObj = mapper.readTree(output);

                if (actualObj.isArray()){
                    for (final JsonNode objNode : actualObj) {
                        JsonNode postData = objNode.get("data");
                        JsonNode postChildren = postData.get("children");
                        
                        if (postChildren.isArray()){
                            for (final JsonNode commentNode : postChildren) {
                                if (commentNode.get("kind").getTextValue().equalsIgnoreCase("t1")){
                                    JsonNode commentData = commentNode.get("data");
                                    
                                    Comment comment = new Comment();
                                    comment.setId(commentData.get("id").getTextValue());
                                    comment.setComment_text(commentData.get("body").getTextValue().replace("\n", " "));
                                    comment.setUser_id(commentData.get("author").getTextValue());
                                    comment.setSubreddit_id(commentData.get("subreddit").getTextValue());
                                    comment.setGilded(commentData.get("gilded").getIntValue());
                                    comment.setScore(commentData.get("score").getIntValue());
                                    comment.setKarma(commentData.get("score").getIntValue());
                                    comment.setPublished_date(new Float(commentData.get("created_utc").getIntValue()));
                                    comment.setParent_id(commentData.get("parent_id").getTextValue());
                                    comment.setPermalink(permalink);
                                    
                                    // Save the post to DynamoDB
                                    dmapper.save(comment);
                                    
                                    System.out.println("===============  T1 COMMENT FOUND ===========");
                                    
                                    JsonNode reply = commentData.get("replies");
                                    if (reply.get("kind") != null) {
                                        DynamoConnector.processReplies(reply, permalink, dmapper);
                                    }                                    
                                }
                            }                            
                        }
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void processReplies(JsonNode replies, String permalink, DynamoDBMapper dmapper){
        JsonNode postData = replies.get("data");
        JsonNode postChildren = postData.get("children");

        if (postChildren.isArray()) {
            for (final JsonNode commentNode : postChildren) {
                if (commentNode.get("kind").getTextValue().equalsIgnoreCase("t1")) {
                    JsonNode commentData = commentNode.get("data");

                    Comment comment = new Comment();
                    comment.setId(commentData.get("id").getTextValue());
                    comment.setComment_text(commentData.get("body").getTextValue().replace("\n", " "));
                    comment.setUser_id(commentData.get("author").getTextValue());
                    comment.setSubreddit_id(commentData.get("subreddit").getTextValue());
                    comment.setGilded(commentData.get("gilded").getIntValue());
                    comment.setScore(commentData.get("score").getIntValue());
                    comment.setKarma(commentData.get("score").getIntValue());
                    comment.setPublished_date(new Float(commentData.get("created_utc").getIntValue()));
                    comment.setParent_id(commentData.get("parent_id").getTextValue());
                    comment.setPermalink(permalink);

                    // Save the post to DynamoDB
                    dmapper.save(comment);
                    
                    System.out.println("===============  T1 SUB-COMMENT FOUND ===========");
                    
                    JsonNode reply = commentData.get("replies");
                    if (reply.get("kind") != null) {
                        DynamoConnector.processReplies(reply, permalink, dmapper);
                    }
                }
            }
        }        
    }
    
    public static void processUsers(File userList, DynamoDBMapper dmapper, Boolean collectMeta, Boolean collectPosts, Boolean collectComments, Boolean collectLikes, Boolean collectDislikes){
        try {

            Scanner sc = new Scanner(userList);

            int postIndex = 1;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                System.out.println("*** Processing User ID=" + line + " @ INDEX=" + postIndex);

                if (postIndex > 0) {
                    if (collectMeta) {
                        DynamoConnector.harvestUserMeta(line, dmapper);
                    }
                    if (collectPosts){
                        DynamoConnector.harvestUserSubmitted(line, dmapper);
                    }
                    if (collectComments) {
                        DynamoConnector.harvestUserComment(line, dmapper);
                    }
                    if (collectLikes){
                        DynamoConnector.harvestUserLikes(line, dmapper);
                    }
                    if (collectDislikes){
                        DynamoConnector.harvestUserDisikes(line, dmapper);
                    }
                }
                postIndex++;
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }        
    }
    
    public static void harvestUserComment(String userId,  DynamoDBMapper dmapper){
        
        try {
            Client client = Client.create();

            int httpStatus;
            ClientResponse response;
            int retryCnt = 0;
            do {
                Thread.sleep(2050);
                System.out.println("** Attempting comment collection for user, " + userId);
                WebResource webResource;
                webResource = client.resource("http://www.reddit.com/user/" + userId + "/comments.json");
                response = webResource.accept("application/json").get(ClientResponse.class);
                httpStatus = response.getStatus();
                retryCnt++;

            } while (httpStatus != 200 && retryCnt < 40);
            
            if (retryCnt >= 40){
                return;
            }

            String output = response.getEntity(String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(output);
            
            JsonNode postData = actualObj.get("data");
            JsonNode postChildren = postData.get("children");

            if (postChildren.isArray()) {
                for (final JsonNode commentNode : postChildren) {
                    if (commentNode.get("kind").getTextValue().equalsIgnoreCase("t1")) {
                        JsonNode commentData = commentNode.get("data");

                        UserComment comment = new UserComment();
                        comment.setId(commentData.get("id").getTextValue());
                        comment.setComment_text(commentData.get("body").getTextValue().replace("\n", " "));
                        comment.setUser_id(commentData.get("author").getTextValue());
                        comment.setSubreddit_id(commentData.get("subreddit").getTextValue());
                        comment.setGilded(commentData.get("gilded").getIntValue());
                        comment.setScore(commentData.get("score").getIntValue());
                        comment.setKarma(commentData.get("score").getIntValue());
                        comment.setPublished_date(new Float(commentData.get("created_utc").getIntValue()));
                        comment.setParent_id(commentData.get("parent_id").getTextValue());
                        comment.setPermalink(commentData.get("link_url").getTextValue());

                        // Save the post to DynamoDB
                        System.out.println("*** PERSISTING COMMENT, USER = " + userId);
                        dmapper.save(comment);
                    }
                }
            }            
        }
        catch (Exception e) {
            e.printStackTrace();
        }        
    }
    
    public static void harvestUserMeta(String userId,  DynamoDBMapper dmapper){
        
        try {
            Client client = Client.create();

            int httpStatus;
            ClientResponse response;
            int retryCnt = 0;
            do {
                Thread.sleep(2050);
                System.out.println("** Attempting JSON ABOUT for user, " + userId);
                WebResource webResource;
                webResource = client.resource("http://www.reddit.com/user/" + userId + "/about.json");
                response = webResource.accept("application/json").get(ClientResponse.class);
                httpStatus = response.getStatus();
                retryCnt++;

            } while (httpStatus != 200 && retryCnt < 40);

            if (retryCnt >= 40) {
                return;
            }

            String output = response.getEntity(String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(output);

            JsonNode userData = actualObj.get("data");
            
            User user = new User();
            user.setId(userData.get("id").getTextValue());
            user.setUsername(userData.get("name").getTextValue());
            user.setKarma_link(userData.get("link_karma").getIntValue());
            user.setKarma_comment(userData.get("comment_karma").getIntValue());
            user.setGold(userData.get("is_gold").getBooleanValue());
            user.setMod(userData.get("is_mod").getBooleanValue());
            user.setVerified(userData.get("has_verified_email").getBooleanValue());
            user.setAccount_created(new Float(userData.get("created_utc").getIntValue()));
            // Save the post to DynamoDB
            
            System.out.println("*** PERSISTING USER = " + userId);
            dmapper.save(user);
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
    
    public static void harvestUserSubmitted(String userId,  DynamoDBMapper dmapper){
        
        try {
            Client client = Client.create();

            int httpStatus;
            ClientResponse response;
            int retryCnt = 0;
            do {
                Thread.sleep(550);
                System.out.println("** Attempting submitted collection for user, " + userId);
                WebResource webResource;
                webResource = client.resource("http://www.reddit.com/user/" + userId + "/submitted.json?limit=100");
                response = webResource.accept("application/json").get(ClientResponse.class);
                httpStatus = response.getStatus();
                retryCnt++;

            } while (httpStatus != 200 && retryCnt < 40);

            if (retryCnt >= 40) {
                return;
            }

            String output = response.getEntity(String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(output);

            JsonNode postData = actualObj.get("data");
            JsonNode postChildren = postData.get("children");

            if (postChildren.isArray()) {
                for (final JsonNode commentNode : postChildren) {

                    JsonNode commentData = commentNode.get("data");

                    // Create a new Subreddit
                    UserSubmitted post = new UserSubmitted();
                    post.setId(commentData.get("id").getTextValue());
                    post.setTitle(commentData.get("title").getTextValue());
                    post.setSubreddit_title(commentData.get("subreddit").getTextValue());
                    post.setSubreddit_id(commentData.get("subreddit_id").getTextValue());
                    post.setSelf_post(commentData.get("is_self").getBooleanValue());
                    post.setNum_comments(commentData.get("num_comments").getIntValue());
                    post.setScore(commentData.get("score").getIntValue());
                    post.setUps(commentData.get("ups").getIntValue());
                    post.setDowns(commentData.get("downs").getIntValue());
                    //post.setText_content(postData.get("id").getTextValue());
                    post.setGilded(commentData.get("gilded").getIntValue());
                    post.setPublished_date(new Float(commentData.get("created").getIntValue()));
                    post.setEdited(commentData.get("edited").getBooleanValue());
                    post.setMature(commentData.get("over_18").getBooleanValue());
                    post.setUser_id(commentData.get("author").getTextValue());
                    post.setUrl(commentData.get("url").getTextValue());
                    post.setPermalink(commentData.get("permalink").getTextValue());

                    // Save the post to DynamoDB
                    System.out.println("*** PERSISTING SUBMITTED, USER = " + userId);
                    dmapper.save(post);                    
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }         
    }
    
    public static void harvestUserLikes(String userId, DynamoDBMapper dmapper) {

        try {
            Client client = Client.create();

            int httpStatus;
            ClientResponse response;
            int retryCnt = 0;
            do {
                Thread.sleep(550);
                System.out.println("** Attempting submitted collection for user, " + userId);
                WebResource webResource;
                webResource = client.resource("http://www.reddit.com/user/" + userId + "/liked.json?limit=100");
                response = webResource.accept("application/json").get(ClientResponse.class);
                httpStatus = response.getStatus();
                retryCnt++;

            } while (httpStatus != 200 && retryCnt < 40);

            if (retryCnt >= 40) {
                return;
            }

            String output = response.getEntity(String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(output);

            JsonNode postData = actualObj.get("data");
            JsonNode postChildren = postData.get("children");

            if (postChildren.isArray()) {
                for (final JsonNode commentNode : postChildren) {

                    JsonNode commentData = commentNode.get("data");

                    // Create a new Subreddit
                    UserLiked post = new UserLiked();
                    post.setId(UUID.randomUUID().toString());
                    post.setContent_id(commentData.get("id").getTextValue());
                    post.setTitle(commentData.get("title").getTextValue());
                    post.setSubreddit_title(commentData.get("subreddit").getTextValue());
                    post.setSubreddit_id(commentData.get("subreddit_id").getTextValue());
                    post.setSelf_post(commentData.get("is_self").getBooleanValue());
                    post.setNum_comments(commentData.get("num_comments").getIntValue());
                    post.setScore(commentData.get("score").getIntValue());
                    post.setUps(commentData.get("ups").getIntValue());
                    post.setDowns(commentData.get("downs").getIntValue());
                    //post.setText_content(postData.get("id").getTextValue());
                    post.setGilded(commentData.get("gilded").getIntValue());
                    post.setPublished_date(new Float(commentData.get("created").getIntValue()));
                    post.setEdited(commentData.get("edited").getBooleanValue());
                    post.setMature(commentData.get("over_18").getBooleanValue());
                    post.setAuthor(commentData.get("author").getTextValue());
                    post.setLiked_by(userId);
                    post.setUrl(commentData.get("url").getTextValue());
                    post.setPermalink(commentData.get("permalink").getTextValue());

                    // Save the post to DynamoDB
                    System.out.println("*** PERSISTING LIKED, USER = " + userId);
                    dmapper.save(post);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void harvestUserDisikes(String userId, DynamoDBMapper dmapper) {

        try {
            Client client = Client.create();

            int httpStatus;
            ClientResponse response;
            int retryCnt = 0;
            do {
                Thread.sleep(550);
                System.out.println("** Attempting disliked collection for user, " + userId);
                WebResource webResource;
                webResource = client.resource("http://www.reddit.com/user/" + userId + "/disliked.json");
                response = webResource.accept("application/json").get(ClientResponse.class);
                httpStatus = response.getStatus();
                retryCnt++;

            } while (httpStatus != 200 && retryCnt < 40);

            if (retryCnt >= 40) {
                return;
            }

            String output = response.getEntity(String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(output);

            JsonNode postData = actualObj.get("data");
            JsonNode postChildren = postData.get("children");

            if (postChildren.isArray()) {
                for (final JsonNode commentNode : postChildren) {

                    JsonNode commentData = commentNode.get("data");

                    // Create a new Subreddit
                    UserDisliked post = new UserDisliked();
                    post.setId(UUID.randomUUID().toString());
                    post.setContent_id(commentData.get("id").getTextValue());
                    post.setTitle(commentData.get("title").getTextValue());
                    post.setSubreddit_title(commentData.get("subreddit").getTextValue());
                    post.setSubreddit_id(commentData.get("subreddit_id").getTextValue());
                    post.setSelf_post(commentData.get("is_self").getBooleanValue());
                    post.setNum_comments(commentData.get("num_comments").getIntValue());
                    post.setScore(commentData.get("score").getIntValue());
                    post.setUps(commentData.get("ups").getIntValue());
                    post.setDowns(commentData.get("downs").getIntValue());
                    //post.setText_content(postData.get("id").getTextValue());
                    post.setGilded(commentData.get("gilded").getIntValue());
                    post.setPublished_date(new Float(commentData.get("created").getIntValue()));
                    post.setEdited(commentData.get("edited").getBooleanValue());
                    post.setMature(commentData.get("over_18").getBooleanValue());
                    post.setAuthor(commentData.get("author").getTextValue());
                    post.setDisliked_by(userId);
                    post.setUrl(commentData.get("url").getTextValue());
                    post.setPermalink(commentData.get("permalink").getTextValue());

                    // Save the post to DynamoDB
                    System.out.println("*** PERSISTING DISLIKED, USER = " + userId);
                    dmapper.save(post);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}