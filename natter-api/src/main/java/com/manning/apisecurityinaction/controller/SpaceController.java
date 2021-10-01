package com.manning.apisecurityinaction.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import org.dalesbred.Database;
import org.json.*;
import com.manning.apisecurityinaction.DAL.*;

import spark.*;

public class SpaceController {

  private Messenger messenger;

  public SpaceController(Messenger messenger) {
    this.messenger = messenger;
  }

  public JSONObject createSpace(Request request, Response response) {
    var json = new JSONObject(request.body());
    var spaceName = json.getString("name");
    if (spaceName.length() > 255) {
      throw new IllegalArgumentException("space name too long");
    }
    var owner = json.getString("owner");
    if (!owner.matches("[a-zA-Z][a-zA-Z0-9]{1,29}")) {
      throw new IllegalArgumentException("invalid username");
    }

    return database.withTransaction(tx -> {
      var spaceId = database.findUniqueLong(
          "SELECT NEXT VALUE FOR space_id_seq;");

      database.updateUnique(
          "INSERT INTO spaces(space_id, name, owner) " +
              "VALUES(?, ?, ?);", spaceId, spaceName, owner);

      response.status(201);
      response.header("Location", "/spaces/" + spaceId);

      return new JSONObject()
          .put("name", spaceName) .put("uri", "/spaces/" + spaceId);

    });
  }

  // Additional REST API endpoints not covered in the book:
  public JSONObject postMessage(Request request, Response response) {
    var spaceId = Long.parseLong(request.params(":spaceId"));
    var json = new JSONObject(request.body());
    var user = json.getString("author");
    if (!user.matches("[a-zA-Z][a-zA-Z0-9]{0,29}")) {
      throw new IllegalArgumentException("invalid username");
    }
    var message = json.getString("message");
    if (message.length() > 1024) {
      throw new IllegalArgumentException("message is too long");
    }

    messenger.InsertMessage(spaceId, user, message);

    response.status(201);
    var uri = "/spaces/" + spaceId + "/messages/" + msgId;
    response.header("Location", uri);
    return new JSONObject().put("uri", uri);
  }

  public Message readMessage(Request request, Response response) {
    var spaceId = Long.parseLong(request.params(":spaceId"));
    var msgId = Long.parseLong(request.params(":msgId"));

    var message = messenger.FindMessage(msgId, spaceId);

    response.status(200);
    return message;
  }

  public JSONArray findMessages(Request request, Response response) {
    var since = Instant.now().minus(1, ChronoUnit.DAYS);
    if (request.queryParams("since") != null) {
      since = Instant.parse(request.queryParams("since"));
    }
    var spaceId = Long.parseLong(request.params(":spaceId"));

    var messages = database.findAll(Long.class,
        "SELECT msg_id FROM messages " +
            "WHERE space_id = ? AND msg_time >= ?;",
        spaceId, since);

    response.status(200);
    return new JSONArray(messages.stream()
        .map(msgId -> "/spaces/" + spaceId + "/messages/" + msgId)
        .collect(Collectors.toList()));
  }
}