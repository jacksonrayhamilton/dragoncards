package com.herokuapp.dragoncards;

import java.util.UUID;

public class Player {
  private String name;
  private String uuid;
  private State state;

  public Player(String name, State state) {
    this.name = name;
    this.uuid = UUID.randomUUID().toString();
    this.state = state;
  }
}
