package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.DuelRequest;
import com.herokuapp.dragoncards.messages.Message;

public class DuelRequestAnsweredMessage extends Message {
  private final DuelRequest duelRequest;
  private final boolean accept;

  public DuelRequestAnsweredMessage(DuelRequest duelRequest, boolean accept) {
    this.duelRequest = duelRequest;
    this.accept = accept;
  }

  public DuelRequest getDuelRequest() {
    return this.duelRequest;
  }

  public boolean isAccept() {
    return this.accept;
  }
}
