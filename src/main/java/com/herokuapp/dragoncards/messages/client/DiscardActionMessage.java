package com.herokuapp.dragoncards.messages.client;

import javax.json.JsonObject;

import com.herokuapp.dragoncards.game.Element;
import com.herokuapp.dragoncards.messages.Message;

public class DiscardActionMessage extends Message {
  private final Element element;
  private final int level;

  public DiscardActionMessage(JsonObject json) {
    this.element = Element.valueOf(json.getString("element").toUpperCase());
    this.level = json.getInt("level");
  }

  public Element getElement() {
    return this.element;
  }

  public int getLevel() {
    return this.level;
  }
}
