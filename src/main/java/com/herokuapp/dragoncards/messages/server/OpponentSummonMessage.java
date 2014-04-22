package com.herokuapp.dragoncards.messages.server;

import java.util.List;

import com.herokuapp.dragoncards.game.Dragon;
import com.herokuapp.dragoncards.messages.Message;

public class OpponentSummonMessage extends Message {
  public List<Dragon> dragons;

  public OpponentSummonMessage(List<Dragon> dragons) {
    this.dragons = dragons;
  }
}
