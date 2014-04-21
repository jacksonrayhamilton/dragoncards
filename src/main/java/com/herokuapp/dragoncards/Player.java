package com.herokuapp.dragoncards;

import java.util.UUID;

/**
 * Representation of a player (client) who seeks matches on the game network.
 * 
 * @author Jackson Hamilton
 */
public class Player {
  private String name;
  private String uuid;
  private State state;

  public Player(String name, State state) {
    this.name = name;
    this.uuid = UUID.randomUUID().toString();
    this.state = state;
  }

  public String toString() {
    return "Player: {\n"
        + "  name: " + this.name + "\n"
        + "  uuid: " + this.uuid + "\n"
        + "  state: " + this.state + "\n"
        + "}";
  }
}
