package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.game.ActionTarget;
import com.herokuapp.dragoncards.messages.Message;

public class OpponentPilferMessage extends Message {
  private final ActionTarget target;

  public OpponentPilferMessage(ActionTarget target) {
    this.target = target;
  }

  public ActionTarget getTarget() {
    return this.target;
  }
}
