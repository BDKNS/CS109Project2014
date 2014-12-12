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
    
    /***
     * Method to process a DynamoDB Export of 'Comments' table data. DynamoDB uses the AWS DataPipeline service as its export facility and 
     * outputted data is stored in a proprietary schema: http://docs.aws.amazon.com/datapipeline/latest/DeveloperGuide/dp-importexport-ddb-pipelinejson-verifydata2.html
     * This function reads an export file and produces a CSV from it.
     */
    public static void parseCommentsExport(){
        
        try {
            Writer csvOutput = new BufferedWriter(new FileWriter("comments_pipedelimited.csv", false));
            csvOutput.append("id | parent_id | comment_text | score | karma | gilded | user_id | subreddit_id | permalink | published_date" + "\n"); 
            BufferedReader br = new BufferedReader(new FileReader("commentsP1.txt"));
            String line;
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
    
    /***
     * Takes a field name, a field value, and a Comment object. Sets the appropriate field of Comment
     * with the supplied value based on the passed in field name. Helper function to parseCommentsExport()
     * @param field
     * @param value
     * @param comment
     * @return 
     */
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
    
    /***
     * Helper function to clean DynamoDB's export schema packaging from supplied field value.
     * @param field
     * @param val
     * @return 
     */
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