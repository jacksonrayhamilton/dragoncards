package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.messages.Message;

public class DuelRequestAnsweredMessage extends Message {
  private final Player player;
  private final boolean accept;

  public DuelRequestAnsweredMessage(Player player, boolean accept) {
    this.player = player;
    this.accept = accept;
  }

  public Player getPlayer() {
    return player;
  }

  public boolean isAccept() {
    return this.accept;
  }
}
