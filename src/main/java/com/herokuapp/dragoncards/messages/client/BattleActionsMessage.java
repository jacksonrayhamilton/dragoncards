package com.herokuapp.dragoncards.messages.client;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonObject;
import javax.json.JsonValue;

import com.herokuapp.dragoncards.game.BattleAction;
import com.herokuapp.dragoncards.messages.Message;

public class BattleActionsMessage extends Message {
  private final List<BattleAction> actions;

  public BattleActionsMessage(JsonObject json) {
    this.actions = new ArrayList<>(2);

    for (JsonValue jsonValue : json.getJsonArray("actions")) {
      JsonObject jsonObject = (JsonObject) jsonValue;
      String type = jsonObject.getString("type").toUpperCase();

      int player = jsonObject.getInt("player");
      int initiator = jsonObject.getInt("initiator");

      switch (type) {
        case BattleAction.ATTACK:
          int target = jsonObject.getInt("target");
          this.actions.add(new BattleAction(type, player, initiator, target));
          break;
        case BattleAction.SWITCH:
          this.actions.add(new BattleAction(type, player, initiator));
          break;
        case BattleAction.COUNTER:
          this.actions.add(new BattleAction(type, player, initiator));
          break;
        default:
          break;
      }
    }
  }

  public List<BattleAction> getActions() {
    return this.actions;
  }
}
