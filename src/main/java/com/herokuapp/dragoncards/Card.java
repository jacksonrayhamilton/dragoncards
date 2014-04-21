package com.herokuapp.dragoncards;

/**
 * A card representing an elemental dragon.
 * 
 * @author Jackson Hamilton
 */
public class Card {
  protected final Element element;
  protected final int level;

  public Card(Element element, int level) {
    this.element = element;
    this.level = level;
  }

  public Element getElement() {
    return element;
  }

  public int getLevel() {
    return level;
  }

  public String toString() {
    return "Card: {\n"
        + "  element: " + this.element + "\n"
        + "  level: " + this.level + "\n"
        + "}";
  }

  public boolean equals(Card other) {
    return (this.element == other.getElement() && this.level == other.getLevel());
  }

}
