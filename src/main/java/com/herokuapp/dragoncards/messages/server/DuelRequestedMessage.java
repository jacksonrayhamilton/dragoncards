package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.DuelRequest;
import com.herokuapp.dragoncards.messages.Message;

public class DuelRequestedMessage extends Message {
  private final DuelRequest duelRequest;

  public DuelRequestedMessage(DuelRequest duelRequest) {
    this.duelRequest = duelRequest;
  }

  public DuelRequest getDuelRequest() {
    return this.duelRequest;
  }
}
