package com.manning.apisecurityinaction.DAL;

import org.dalesbred.Database;
import org.json.JSONObject;

import spark.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import org.json.*;
import com.manning.apisecurityinaction.DAL.*;

public class Messenger {
    private Database database;

    public Messenger(Database database) {
        this.database = database;
    }

    public boolean DeleteBySpaceIdAndMsgId(Long spaceId, Long msgId) {
        try{       
            database.updateUnique("DELETE FROM messages " +
            "WHERE space_id = ? AND msg_id = ?", spaceId, msgId);

            return true;
        } catch(Exception ex) {
            return false;
        }
    }

    public JSONObject InsertMessage(long spaceId, String user, String message) {
        return database.withTransaction(tx -> {
            var msgId = database.findUniqueLong(
                "SELECT NEXT VALUE FOR msg_id_seq;");
            database.updateUnique(
                "INSERT INTO messages(space_id, msg_id, msg_time," +
                    "author, msg_text) " +
                    "VALUES(?, ?, current_timestamp, ?, ?)",
                spaceId, msgId, user, message);
      
            response.status(201);
            var uri = "/spaces/" + spaceId + "/messages/" + msgId;
            response.header("Location", uri);
            return new JSONObject().put("uri", uri);
          });
    }

    public Message FindMessage(long msgId, long spaceId){
        return database.findUnique(Message.class,
        "SELECT space_id, msg_id, author, msg_time, msg_text " +
            "FROM messages WHERE msg_id = ? AND space_id = ?",
        msgId, spaceId);
    }


    public static class Message {
        private final long spaceId;
        private final long msgId;
        private final String author;
        private final Instant time;
        private final String message;
    
        public Message(long spaceId, long msgId, String author,
            Instant time, String message) {
          this.spaceId = spaceId;
          this.msgId = msgId;
          this.author = author;
          this.time = time;
          this.message = message;
        }
        @Override
        public String toString() {
          JSONObject msg = new JSONObject();
          msg.put("uri",
              "/spaces/" + spaceId + "/messages/" + msgId);
          msg.put("author", author);
          msg.put("time", time.toString());
          msg.put("message", message);
          return msg.toString();
        }
      }
    
}
