package com.herokuapp.dragoncards.messages.server;

import java.util.List;

import com.herokuapp.dragoncards.game.Dragon;
import com.herokuapp.dragoncards.messages.Message;

public class SummonMessage extends Message {
  private final List<Dragon> dragons;

  public SummonMessage(List<Dragon> dragons) {
    this.dragons = dragons;
  }

  public List<Dragon> getDragons() {
    return this.dragons;
  }
}
