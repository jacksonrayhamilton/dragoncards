package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.messages.Message;

public class CreatePlayerMessage extends Message {
  public String name;
  public String uuid;

  public CreatePlayerMessage(String name, String uuid) {
    this.name = name;
    this.uuid = uuid;
  }
}
