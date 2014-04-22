package com.herokuapp.dragoncards.messages.client;

import javax.json.JsonObject;

import com.herokuapp.dragoncards.messages.Message;

public class RequestDuelMessage extends Message {
  private final String uuid;

  public RequestDuelMessage(JsonObject json) {
    this.uuid = json.getString("uuid");
  }

  public String getUuid() {
    return this.uuid;
  }
}
