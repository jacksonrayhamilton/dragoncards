package com.herokuapp.dragoncards.messages.client;

import javax.json.JsonObject;

import com.herokuapp.dragoncards.messages.Message;

public class AnswerDuelRequestMessage extends Message {
  private final boolean accept;

  public AnswerDuelRequestMessage(JsonObject json) {
    this.accept = json.getBoolean("accept");
  }

  public boolean isAccept() {
    return this.accept;
  }
}
