package com.herokuapp.dragoncards.messages.server;

import java.util.List;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.messages.Message;

public class UpdateLobbyMessage extends Message {
  private final List<Player> playersJoined;
  private final List<Player> playersLeft;

  public UpdateLobbyMessage(List<Player> playersJoined, List<Player> playersLeft) {
    this.playersJoined = playersJoined;
    this.playersLeft = playersLeft;
  }

  public List<Player> getPlayersJoined() {
    return playersJoined;
  }

  public List<Player> getPlayersLeft() {
    return playersLeft;
  }

}
