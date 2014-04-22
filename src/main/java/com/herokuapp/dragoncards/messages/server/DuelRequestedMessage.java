package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.messages.Message;

public class DuelRequestedMessage extends Message {
  public String uuid;

  public DuelRequestedMessage(String uuid) {
    super();
    this.uuid = uuid;
  }

}
