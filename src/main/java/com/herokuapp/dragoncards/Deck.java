package com.herokuapp.dragoncards;

import java.util.ArrayList;

public class Deck {
  private ArrayList<Card> cards;

  public Deck() {
    this.cards = new ArrayList<Card>(75);
    for (int i = 0; i < 75; i++) {
      // Generate all 3 copies of all 25 different dragons.
      Element element = Element.ELEMENTS[(i / 15) + 1];
      int level = ((i % 15) / 3) + 1;
      Card card = new Card(element, level);

      // An inside-out Fisher-Yates shuffle provides an automatically-sorted
      // deck.
      int j = RandomNumberGenerator.inclusiveRange(0, i);
      if (j != i) {
        this.cards.set(i, this.cards.get(j));
      }
      this.cards.set(j, card);
    }
  }

  public Card draw() {
    return this.cards.remove(this.cards.size() - 1);
  }
}
