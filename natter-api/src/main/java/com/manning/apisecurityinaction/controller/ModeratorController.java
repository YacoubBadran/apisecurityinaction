package com.manning.apisecurityinaction.controller;

import org.dalesbred.Database;
import org.json.JSONObject;
import com.manning.apisecurityinaction.DAL.*;

import spark.*;

public class ModeratorController {

  private Messenger messenger;

  public ModeratorController(Messenger messenger) {
    this.messenger = messenger;
  }

  public JSONObject deletePost(Request request, Response response) {
    var spaceId = Long.parseLong(request.params(":spaceId"));
    var msgId = Long.parseLong(request.params(":msgId"));

    messenger.DeleteBySpaceIdAndMsgId(spaceId, msgId);

    response.status(200);
    return new JSONObject();

  }
}
