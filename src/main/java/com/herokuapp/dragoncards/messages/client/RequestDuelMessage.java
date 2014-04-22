package com.herokuapp.dragoncards.messages.client;

import javax.json.JsonObject;

import com.herokuapp.dragoncards.messages.Message;

public class RequestDuelMessage extends Message {
  public String uuid;

  public RequestDuelMessage(JsonObject json) {
    this.uuid = json.getString("uuid");
  }
}
