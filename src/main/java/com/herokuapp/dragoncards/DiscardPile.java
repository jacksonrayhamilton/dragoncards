package com.herokuapp.dragoncards;

import java.util.LinkedList;

public class DiscardPile {
  private LinkedList<Card> cards;

  public DiscardPile() {
    this.cards = new LinkedList<Card>();
  }

  public void receiveDiscardedCard(Card card) {
    this.cards.add(card);
  }

  public Card peekTop() {
    return this.cards.peekLast();
  }

  public Card collectTop() {
    return this.cards.removeLast();
  }
}
