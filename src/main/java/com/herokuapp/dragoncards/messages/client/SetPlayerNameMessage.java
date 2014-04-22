package com.herokuapp.dragoncards.messages.client;

import javax.json.JsonObject;

import com.herokuapp.dragoncards.messages.Message;

public class SetPlayerNameMessage extends Message {
  private final String name;

  public SetPlayerNameMessage(JsonObject json) {
    this.name = json.getString("name");
  }

  public String getName() {
    return this.name;
  }
}
