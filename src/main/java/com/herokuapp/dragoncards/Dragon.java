package com.herokuapp.dragoncards;

public class Dragon extends Card {
  private final double maxLife;
  private double life;
  private final double power;
  private double boost;

  public Dragon(Element element, int level) {
    super(element, level);
    this.maxLife = Math.log(10 * level) / Math.log(10) * 10;
    this.life = this.maxLife;
    this.power = Math.log(10 * level) / Math.log(20) * 5;
    this.boost = 0.0;
  }

  public Dragon(Card card) {
    this(card.getElement(), card.getLevel());
  }

  public boolean isDead() {
    return this.life <= 0;
  }

  public boolean equals(Dragon other) {
    return (this.element == other.getElement() && this.level == other.getLevel());
  }
}
