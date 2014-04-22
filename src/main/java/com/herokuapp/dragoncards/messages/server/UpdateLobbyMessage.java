package com.herokuapp.dragoncards.messages.server;

import java.util.List;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.messages.Message;

public class UpdateLobbyMessage extends Message {
  public List<Player> playersJoined;
  public List<String> playersLeft;

  public UpdateLobbyMessage(List<Player> playersJoined, List<String> playersLeft) {
    super();
    this.playersJoined = playersJoined;
    this.playersLeft = playersLeft;
  }

}
