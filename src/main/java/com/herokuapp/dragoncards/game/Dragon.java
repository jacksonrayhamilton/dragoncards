package com.herokuapp.dragoncards.game;

import javax.json.Json;
import javax.json.JsonValue;

import com.herokuapp.dragoncards.JsonSerializable;

/**
 * A dragon which a player can battle with. A dragons is synonymous with the
 * card that summoned it.
 * 
 * @author Jackson Hamilton
 */
public class Dragon extends Card implements JsonSerializable {
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

  public double getMaxLife() {
    return this.maxLife;
  }

  public double getLife() {
    return this.life;
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
    String life = String.format("%.3f", this.life);
    String maxLife = String.format("%.3f", this.maxLife);
    String power = String.format("%.3f", this.power);
    return "Dragon: {\n"
        + "  element: " + this.element + "\n"
        + "  level: " + this.level + "\n"
        + "  life: " + life + "/" + maxLife + "\n"
        + "  power: " + power + "\n"
        + "  boost: " + this.boost + "\n"
        + "}";
  }

  @Override
  public JsonValue toJson() {
    return Json.createObjectBuilder()
        .add("element", this.element.toString())
        .add("level", this.level)
        .add("maxLife", this.maxLife)
        .add("life", this.life)
        .add("power", this.power)
        .add("boost", this.boost)
        .build();
  }
}
