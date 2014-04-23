package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.messages.Message;

public class DuelRequestAnsweredMessage extends Message {
  private final boolean accept;

  public DuelRequestAnsweredMessage(boolean accept) {
    this.accept = accept;
  }

  public boolean isAccept() {
    return this.accept;
  }
}
