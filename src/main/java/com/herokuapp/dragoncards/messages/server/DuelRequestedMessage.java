package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.messages.Message;

public class DuelRequestedMessage extends Message {
  public Player opponent;

  public DuelRequestedMessage(Player opponent) {
    this.opponent = opponent;
  }
}
