package com.herokuapp.dragoncards;

public class Card {
  private Element element;
  private int level;
  private double life;
  private double boost;

  public Card(Element element, int level) {
    this.element = element;
    this.level = level;
  }

  public void summon() {
    this.life = Math.log(10 * 5) / Math.log(10) * 10;
    this.boost = 0.0;
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

}
