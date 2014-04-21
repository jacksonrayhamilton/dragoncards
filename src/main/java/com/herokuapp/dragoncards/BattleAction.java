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
  private int initiator;
  private int target;

  public BattleAction(String type, int initiator, int target) {
    this.type = type;
    this.initiator = initiator;
    this.target = target;
  }

  public String getType() {
    return type;
  }

  public int getInitiator() {
    return initiator;
  }

  public int getTarget() {
    return target;
  }

}
