package com.herokuapp.dragoncards.game;

import javax.json.Json;
import javax.json.JsonValue;

import com.herokuapp.dragoncards.JsonSerializable;

/**
 * Representations of the different actions a dragon can perform when battling.
 * 
 * @author Jackson Hamilton
 */
public class BattleAction implements JsonSerializable {

  public static final String
      ATTACK = "ATTACK",
      SWITCH = "SWITCH",
      COUNTER = "COUNTER";

  private String type;
  private int player;
  private int initiator;
  private int target;

  public BattleAction(String type, int player, int initiator) {
    this.type = type;
    this.player = player;
    this.initiator = initiator;
  }

  public BattleAction(String type, int player, int initiator, int target) {
    this(type, player, initiator);
    this.target = target;
  }

  public String getType() {
    return this.type;
  }

  public int getPlayer() {
    return this.player;
  }

  public int getInitiator() {
    return this.initiator;
  }

  public int getTarget() {
    return this.target;
  }

  @Override
  public JsonValue toJson() {
    return Json.createObjectBuilder()
        .add("type", this.type)
        .add("player", this.player)
        .add("initiator", this.initiator)
        .add("target", this.target)
        .build();
  }

}
