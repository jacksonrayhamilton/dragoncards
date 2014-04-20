package com.herokuapp.dragoncards;

import java.util.ArrayList;
import java.util.Arrays;

public class Deck {
  private ArrayList<Card> cards;

  public Deck() {
    Card[] array = new Card[75];
    for (int i = 0; i < 75; i++) {
      // Generate all 3 copies of all 25 different dragons.
      Element element = Element.ELEMENTS[i / 15];
      int level = ((i % 15) / 3) + 1;
      Card card = new Card(element, level);

      // An inside-out Fisher-Yates shuffle provides an automatically-sorted
      // deck.
      int j = RandomNumberGenerator.inclusiveRange(0, i);
      if (j != i) {
        array[i] = array[j];
      }
      array[j] = card;
    }
    this.cards = new ArrayList<Card>(Arrays.asList(array));
  }

  public Card draw() {
    return this.cards.remove(this.cards.size() - 1);
  }

  public boolean isEmpty() {
    return this.cards.isEmpty();
  }

  public int size() {
    return this.cards.size();
  }

  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("[ \n");
    for (Card card : this.cards) {
      stringBuilder.append("  ");
      stringBuilder.append(card);
      stringBuilder.append("\n");
    }
    stringBuilder.append("]");
    return stringBuilder.toString();
  }
}
