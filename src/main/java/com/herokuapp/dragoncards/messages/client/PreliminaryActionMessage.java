package com.herokuapp.dragoncards.messages.client;

import javax.json.JsonObject;

import com.herokuapp.dragoncards.game.ActionTarget;
import com.herokuapp.dragoncards.game.CollectAction;
import com.herokuapp.dragoncards.game.PreliminaryAction;
import com.herokuapp.dragoncards.game.SummonAction;
import com.herokuapp.dragoncards.messages.Message;

public class PreliminaryActionMessage extends Message {
  private final PreliminaryAction action;
  private final ActionTarget target;

  public PreliminaryActionMessage(JsonObject json) {
    String action = json.getString("action");
    switch (action) {
      case "draw":
      case "pilfer":
        this.action = CollectAction.valueOf(action.toUpperCase());
        break;
      case "summon":
        this.action = SummonAction.valueOf(action.toUpperCase());
        break;
      default:
        this.action = null;
        break;
    }
    String target = json.getString("action");
    if (target != null) {
      this.target = ActionTarget.valueOf(target.toUpperCase());
    } else {
      this.target = null;
    }
  }

  public PreliminaryAction getAction() {
    return this.action;
  }

  public ActionTarget getTarget() {
    return this.target;
  }
}
