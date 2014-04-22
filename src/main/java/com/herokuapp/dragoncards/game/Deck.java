package com.herokuapp.dragoncards.game;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Container of all yet-undrawn cards in the game.
 * 
 * @author Jackson Hamilton
 */
public class Deck extends CardCollection {

  /**
   * Constructs a pre-shuffled deck of cards.
   */
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

  /**
   * Removes a card from the top of the deck.
   * 
   * @return The top card of the deck (no longer the top after this method has
   *         executed.
   */
  public Card draw() {
    return this.cards.remove(this.cards.size() - 1);
  }

}
