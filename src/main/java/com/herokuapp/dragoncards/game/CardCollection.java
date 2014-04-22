package com.herokuapp.dragoncards.game;

import java.util.List;

/**
 * A collection of many Card objects.
 * 
 * @author Jackson Hamilton
 */
public abstract class CardCollection {

  protected List<Card> cards;

  public List<Card> getCards() {
    return this.cards;
  }

  public void add(Card card) {
    this.getCards().add(card);
  }

  public Card get(int index) {
    return this.getCards().get(index);
  }

  public boolean isEmpty() {
    return this.getCards().isEmpty();
  }

  public int size() {
    return this.getCards().size();
  }

  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(this.getClass().getSimpleName());
    stringBuilder.append(": [\n");
    for (Card card : this.getCards()) {
      stringBuilder.append("  ");
      // Add another level of indentation. Suspicious, error-prone.
      stringBuilder.append(card.toString().replace("\n", "\n  "));
      stringBuilder.append("\n");
    }
    stringBuilder.append("]");
    return stringBuilder.toString();
  }

}
