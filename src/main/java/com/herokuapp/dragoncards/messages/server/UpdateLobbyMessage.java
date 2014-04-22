package com.herokuapp.dragoncards.messages.server;

import java.util.List;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.messages.Message;

public class UpdateLobbyMessage extends Message {
  public List<Player> playersJoined;
  public List<Player> playersLeft;

  public UpdateLobbyMessage(List<Player> playersJoined, List<Player> playersLeft) {
    this.playersJoined = playersJoined;
    this.playersLeft = playersLeft;
  }

}
