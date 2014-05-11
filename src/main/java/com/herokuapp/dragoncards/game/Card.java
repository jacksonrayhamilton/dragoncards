package com.herokuapp.dragoncards.game;

import javax.json.Json;
import javax.json.JsonValue;

import com.herokuapp.dragoncards.JsonSerializable;

/**
 * A card representing an elemental dragon.
 * 
 * @author Jackson Hamilton
 */
public class Card implements JsonSerializable {
  protected final Element element;
  protected final int level;

  public Card(Element element, int level) {
    this.element = element;
    this.level = level;
  }

  public Element getElement() {
    return this.element;
  }

  public int getLevel() {
    return this.level;
  }

  @Override
  public String toString() {
    return "Card: {\n"
        + "  element: " + this.element + "\n"
        + "  level: " + this.level + "\n"
        + "}";
  }

  @Override
  public boolean equals(Object other) {
    return (this.getElement() == ((Card) other).getElement() && this.getLevel() == ((Card) other).getLevel());
  }

  @Override
  public JsonValue toJson() {
    return Json.createObjectBuilder()
        .add("element", this.element.toString())
        .add("level", this.level)
        .build();
  }

}
