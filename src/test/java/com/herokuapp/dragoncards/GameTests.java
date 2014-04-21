package com.herokuapp.dragoncards;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class GameTests {

  private Player a;
  private Player b;
  private Game game;

  @Before
  public void setUp() {
    this.a = new Player("A", State.DUELING);
    this.b = new Player("B", State.DUELING);
    this.game = new Game(a, b);
  }

  @Test
  public void test_Discard_ManyCards_Discarded() {

    Hand hand = this.game.getHand(0);
    DiscardPile discardPile = this.game.getDiscardPile(0);

    assertEquals(6, hand.size());
    assertEquals(0, discardPile.size());

    int cardsRemoved = 0;
    while (!hand.isEmpty()) {
      Card card = hand.get(0);
      this.game.receiveDiscardAction(card.getElement(),
          card.getLevel());

      cardsRemoved++;

      assertEquals(6 - cardsRemoved, hand.size());
      assertEquals(0 + cardsRemoved, discardPile.size());
    }

    assertEquals(0, hand.size());
    assertEquals(6, discardPile.size());
  }
}
