package com.cs109.reddit2awsvortex.aws;

import com.cs109.reddit2awsvortex.data.model.Comment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dario
 */
public class DynamoExportParser {
    
    
    public static void parseCommentsExport(){
        
        try {
            Writer csvOutput = new BufferedWriter(new FileWriter("MEGAOUT.csv", false));
            csvOutput.append("id | parent_id | comment_text | score | karma | gilded | user_id | subreddit_id | permalink | published_date" + "\n"); 
            BufferedReader br = new BufferedReader(new FileReader("commentsP1.txt"));
            String line = "";
            int postIndex = 1;
            while ((line = br.readLine()) != null) {
                //System.out.println("\n*** Processing Line \n" + line + "\n @ INDEX=" + postIndex);
                
                // Split on STX 
                String[] sArr = line.split("");
                System.out.println(sArr.length + " at index = " + postIndex);
                Comment currComment = new Comment();
                for (int i = 0; i < sArr.length; i++){
                    // Split on ETX
                    String[] fArr = sArr[i].split("");
                    
                    String field = fArr[0];
                    String val = fArr[1];
                    val = cleanVal(field, val);
                    currComment = DynamoExportParser.mapField(field, val, currComment);
                    
                }
                System.out.println(currComment.toString());     
                csvOutput.append(currComment.csvString()+ "\n");                
                postIndex++;
            }
            
            // Repeat process for second data file
            ///////////////////////////////////////////////

            br = new BufferedReader(new FileReader("commentsP2.txt"));
            while ((line = br.readLine()) != null) {
            // do something
                //System.out.println("\n*** Processing Line \n" + line + "\n @ INDEX=" + postIndex);

                // Split on STX 
                String[] sArr = line.split("");
                System.out.println(sArr.length + " at index = " + postIndex);
                Comment currComment = new Comment();
                for (int i = 0; i < sArr.length; i++) {
                    // Split on ETX
                    String[] fArr = sArr[i].split("");

                    String field = fArr[0];
                    String val = fArr[1];
                    val = cleanVal(field, val);
                    currComment = DynamoExportParser.mapField(field, val, currComment);

                }
                System.out.println(currComment.toString());
                csvOutput.append(currComment.csvString() + "\n");
                postIndex++;
            }            
            
            ////////////////////////////////////////////////////
            csvOutput.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(DynamoExportParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Comment mapField(String field, String value, Comment comment){
        
        switch (field) {
            case "id":
                comment.setId(value);
                break;
            case "published_date":
                comment.setPublished_date(Float.parseFloat(value));
                break;
            case "karma":
                comment.setKarma(Integer.parseInt(value));
                break;
            case "permalink":
                comment.setPermalink(value);
                break;
            case "comment_text":
                comment.setComment_text(value);
                break;
            case "score":
                comment.setScore(Integer.parseInt(value));
                break;
            case "subreddit_id":
                comment.setSubreddit_id(value);
                break;
            case "user_id":
                comment.setUser_id(value);
                break;
            case "gilded":
                comment.setGilded(Integer.parseInt(value));
                break;
            case "parent_id":
                comment.setParent_id(value);
                break;
            default:
                throw new IllegalArgumentException("Invalid field: " + field);
        }
        return comment;
    }
    
    public static String cleanVal(String field, String val){
        val = val.substring(3);        
        //val = val.replaceAll("\n", "");
        //val = val.replaceAll("s:", "");
        //val = val.replaceAll("[n:]", "");
        val = val.replaceAll("}", "");
        //val = val.replace("{", "");
        val = val.replace(":", "");
        if (!field.equalsIgnoreCase("comment_text")){
            val = val.replace("\"", "");
        }
        else{
            val = val.substring(1);
        }        
        return val;
    }  
}