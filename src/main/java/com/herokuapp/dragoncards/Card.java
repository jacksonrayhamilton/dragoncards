package com.herokuapp.dragoncards;

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
    return String.format("%s - %d", this.element.toString(), this.level);
  }

  public boolean equals(Card other) {
    return (this.element == other.getElement() && this.level == other.getLevel());
  }

}
