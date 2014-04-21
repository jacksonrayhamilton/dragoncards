package com.herokuapp.dragoncards;

/**
 * A dragon which a player can battle with. A dragons is synonymous with the
 * card that summoned it.
 * 
 * @author Jackson Hamilton
 */
public class Dragon extends Card {
  private final double maxLife;
  private double life;
  private final double power;
  private double boost;
  private boolean countering;

  /**
   * Constructs a dragon with stats correlating to its element and level.
   * 
   * @param element
   * @param level
   */
  public Dragon(Element element, int level) {
    super(element, level);
    this.maxLife = Math.log(10 * level) / Math.log(10) * 10;
    this.life = this.maxLife;
    this.power = Math.log(10 * level) / Math.log(20) * 5;
    this.boost = 1.0;
    this.countering = false;
  }

  public Dragon(Card card) {
    this(card.getElement(), card.getLevel());
  }

  public double getDamage() {
    return this.power * this.boost;
  }

  public void takeDamage(Element element, double damage) {
    if (Element.getDominated(element) == this.element) {
      damage *= 2;
    } else if (Element.getWeakness(element) == this.element) {
      damage *= 0.5;
    }
    this.life -= damage;
  }

  public boolean isDead() {
    return this.life <= 0;
  }

  public boolean isCountering() {
    return this.countering;
  }

  public void startCountering() {
    this.countering = true;
  }

  public void stopCountering() {
    this.countering = false;
  }

  public boolean equals(Dragon other) {
    return (this.element == other.getElement() && this.level == other.getLevel());
  }

  public String toString() {
    return "Dragon: {\n"
        + "  element: " + this.element + "\n"
        + "  level: " + this.level + "\n"
        + "  life: " + this.life + "/" + this.maxLife + "\n"
        + "  power: " + this.power + "\n"
        + "  boost: " + this.boost + "\n"
        + "}";
  }
}
