package com.herokuapp.dragoncards.messages.server;

import java.util.List;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.messages.Message;

public class LobbyMessage extends Message {
  private final List<Player> players;

  public LobbyMessage(List<Player> players) {
    this.players = players;
  }

  public List<Player> getPlayers() {
    return this.players;
  }
}
