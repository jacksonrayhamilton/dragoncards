package com.herokuapp.dragoncards;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonValue;

/**
 * Representation of a player (client) who seeks matches on the game network.
 * 
 * @author Jackson Hamilton
 */
public class Player implements JsonSerializable {
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

  @Override
  public JsonValue toJson() {
    return Json.createObjectBuilder()
        .add("name", this.name)
        .add("uuid", this.uuid)
        .build();
  }

  public String getUuid() {
    return this.uuid;
  }

  public String getName() {
    return this.name;
  }
}
