package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.messages.Message;

public class GameoverMessage extends Message {
  private final Player winner;

  public GameoverMessage(Player winner) {
    this.winner = winner;
  }

  public Player getWinner() {
    return this.winner;
  }
}
