package com.herokuapp.dragoncards;

/**
 * Representations of the different actions a dragon can perform when battling.
 * 
 * @author Jackson Hamilton
 */
public class BattleAction {

  public static final String
      ATTACK = "ATTACK",
      SWITCH = "SWITCH",
      COUNTER = "COUNTER";

  private String type;
  private int player;
  private int initiator;
  private int target;

  public BattleAction(String type, int player, int initiator, int target) {
    this.type = type;
    this.player = player;
    this.initiator = initiator;
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

}
