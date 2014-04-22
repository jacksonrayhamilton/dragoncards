package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.messages.Message;

public class OpponentPilferMessage extends Message {
  private final int target;

  public OpponentPilferMessage(int target) {
    this.target = target;
  }

  public int getTarget() {
    return this.target;
  }
}
