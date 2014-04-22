package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.messages.Message;

public class CreatePlayerMessage extends Message {
  public Player player;

  public CreatePlayerMessage(Player player) {
    this.player = player;
  }
}
