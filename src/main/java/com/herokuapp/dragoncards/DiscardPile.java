package com.herokuapp.dragoncards;

import java.util.LinkedList;
import java.util.List;

/**
 * The location where cards go when they are ejected from one's hand.
 * 
 * @author Jackson Hamilton
 */
public class DiscardPile extends CardCollection {

  private LinkedList<Card> cards;

  public DiscardPile() {
    this.cards = new LinkedList<Card>();
  }

  public Card peekTop() {
    return this.cards.peekLast();
  }

  public Card collectTop() {
    return this.cards.removeLast();
  }

  @Override
  public List<Card> getCards() {
    return this.cards;
  }

}
