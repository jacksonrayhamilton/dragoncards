package com.herokuapp.dragoncards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Hand {
  private int size;
  private List<Card> cards;

  public Hand() {
    this.size = 0;
    this.cards = new ArrayList<Card>(7);
  }

  public void add(Card card) {
    this.cards.add(card);
  }

  public void discard(Element element, int level) {
    Iterator<Card> iterator = this.cards.iterator();
    while (iterator.hasNext()) {
      Card card = iterator.next();
      if (card.getElement() == element && card.getLevel() == level) {
        iterator.remove();
        return;
      }
    }
  }
}
