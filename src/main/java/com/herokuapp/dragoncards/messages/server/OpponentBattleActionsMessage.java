package com.herokuapp.dragoncards.messages.server;

import java.util.List;

import com.herokuapp.dragoncards.game.BattleAction;
import com.herokuapp.dragoncards.messages.Message;

public class OpponentBattleActionsMessage extends Message {
  private final List<BattleAction> actions;

  public OpponentBattleActionsMessage(List<BattleAction> actions) {
    this.actions = actions;
  }

  public List<BattleAction> getActions() {
    return this.actions;
  }
}
