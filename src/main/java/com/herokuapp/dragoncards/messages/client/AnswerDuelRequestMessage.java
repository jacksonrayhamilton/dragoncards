package com.herokuapp.dragoncards.messages.client;

import javax.json.JsonObject;

import com.herokuapp.dragoncards.messages.Message;

public class AnswerDuelRequestMessage extends Message {
  private final String uuid;
  private final boolean accept;

  public AnswerDuelRequestMessage(JsonObject json) {
    this.uuid = json.getString("uuid");
    this.accept = json.getBoolean("accept");
  }

  public String getUuid() {
    return this.uuid;
  }

  public boolean isAccept() {
    return this.accept;
  }
}
