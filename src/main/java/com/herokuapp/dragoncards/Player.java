package com.herokuapp.dragoncards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  private final String uuid;
  private State state;
  private List<DuelRequest> duelRequests;

  public Player(String name, State state) {
    this.name = name;
    this.uuid = UUID.randomUUID().toString();
    this.state = state;
    this.duelRequests =
        Collections.synchronizedList(new ArrayList<DuelRequest>());
  }

  public Player(String name) {
    this(name, State.IN_LIMBO);
  }

  public Player() {
    this("Anonymous", State.IN_LIMBO);
  }

  @Override
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

  public String getInformationalName() {
    return this.name + " (" + this.uuid + ")";
  }

  public State getState() {
    return this.state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean equals(Player player) {
    return this.getUuid().equals(player.getUuid());
  }

  public List<DuelRequest> getDuelRequests() {
    return this.duelRequests;
  }

  public void clearDuelRequests() {
    this.duelRequests.clear();
  }

  public void addDuelRequest(DuelRequest duelRequest) {
    this.duelRequests.add(duelRequest);
  }

  public void removeDuelRequest(DuelRequest duelRequest) {
    this.duelRequests.remove(duelRequest);
  }
}
