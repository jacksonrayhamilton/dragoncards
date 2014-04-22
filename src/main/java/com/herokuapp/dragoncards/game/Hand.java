package com.herokuapp.dragoncards.game;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * The contents of a player's hand.
 * 
 * @author Jackson Hamilton
 */
public class Hand extends CardCollection {

  public Hand() {
    this.cards = new ArrayList<Card>(7);
  }

  /**
   * Removes a card matching the criteria from this hand.
   * 
   * @param element
   *          Element of the card to be removed.
   * @param level
   *          Level of the card to be removed.
   * @return The removed card, or null if none removed.
   */
  public Card discard(Element element, int level) {
    Iterator<Card> iterator = this.cards.iterator();
    while (iterator.hasNext()) {
      Card card = iterator.next();
      if (card.getElement() == element && card.getLevel() == level) {
        iterator.remove();
        return card;
      }
    }
    return null;
  }
}
