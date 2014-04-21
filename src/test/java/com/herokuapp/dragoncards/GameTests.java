package com.herokuapp.dragoncards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
  public void Discard_ManyCards_Discarded() {

    Hand hand = this.game.getHand(0);
    DiscardPile discardPile = this.game.getDiscardPile(0);

    assertEquals(6, hand.size());
    assertEquals(0, discardPile.size());

    int cardsRemoved = 0;
    while (!hand.isEmpty()) {
      this.game.receiveDiscardAction(hand.get(0));

      cardsRemoved++;

      assertEquals(6 - cardsRemoved, hand.size());
      assertEquals(0 + cardsRemoved, discardPile.size());
    }

    assertEquals(0, hand.size());
    assertEquals(6, discardPile.size());
  }

  @Test
  public void Pilfer_ManyCards_Pilfered() {
    Hand hand = this.game.getHand(0);
    DiscardPile discardPile = this.game.getDiscardPile(0);

    Card firstDiscarded = hand.get(0);
    this.game.receiveDiscardAction(firstDiscarded);
    Card secondDiscarded = hand.get(0);
    this.game.receiveDiscardAction(secondDiscarded);

    assertEquals(4, hand.size());
    assertEquals(2, discardPile.size());

    this.game.receiveCollectAction(CollectAction.PILFER, 0);

    assertEquals(5, hand.size());
    assertEquals(1, discardPile.size());
    assertTrue(hand.get(hand.size() - 1) == secondDiscarded);

    this.game.receiveCollectAction(CollectAction.PILFER, 0);

    assertEquals(6, hand.size());
    assertEquals(0, discardPile.size());
    assertTrue(hand.get(hand.size() - 1) == firstDiscarded);
  }

  @Test
  public void Draw_ManyCards_Drawn() {
    Hand hand = this.game.getHand(0);
    Deck deck = this.game.getDeck();

    assertEquals(6, hand.size());
    assertEquals(75 - 12, deck.size());

    this.game.receiveCollectAction(CollectAction.DRAW);

    assertEquals(7, hand.size());
    assertEquals(75 - 12 - 1, deck.size());

    this.game.receiveCollectAction(CollectAction.DRAW);

    assertEquals(8, hand.size());
    assertEquals(75 - 12 - 2, deck.size());

    while (!deck.isEmpty()) {
      this.game.receiveCollectAction(CollectAction.DRAW);
    }

    assertEquals(75 - 6, hand.size());
    assertEquals(0, deck.size());
  }

  private void setupHandForSummoning(int player) {
    int otherPlayer = player == 0 ? 1 : 0;
    Hand otherHand = this.game.getHand(otherPlayer);
    DiscardPile otherDiscardPile = this.game.getDiscardPile(otherPlayer);
    Deck deck = this.game.getDeck();

    if (player == 1) {
      // Begin with player 1's turn.
      this.game.nextTurn();
    }
    // Else begin with player 0's turn.

    // Draw whole deck.
    while (!deck.isEmpty()) {
      this.game.receiveCollectAction(CollectAction.DRAW);
    }

    this.game.nextTurn();

    // Discard hand.
    while (!otherHand.isEmpty()) {
      Card card = otherHand.get(0);
      this.game.receiveDiscardAction(card);
    }

    this.game.nextTurn();

    // Pilfer whole discard pile of other player.
    while (!otherDiscardPile.isEmpty()) {
      this.game.receiveCollectAction(CollectAction.PILFER, otherPlayer);
    }
  }

  @Test
  public void Summon_SufficientCardsInHand_Summoned() {
    for (int i = 0; i < 2; i++) {
      this.setupHandForSummoning(i);
      this.game.receiveSummonAction();
      assertTrue(this.game.isReadyToBattle(i));
      assertEquals(2, this.game.getDragons(i).size());
    }
  }

  @Test
  public void GettingWinner_DifferentWinners_CorrectCode() {
    assertEquals(-1, this.game.getWinner());

    this.setupHandForSummoning(0); // Deck is empty now.
    this.setupHandForSummoning(1);
    this.game.nextTurn(); // Setup left it as player 1's turn so make it player
                          // 0's instead.

    assertEquals(-2, this.game.getWinner()); // Tied since deck is empty.

    this.game.receiveSummonAction(); // For player 0.
    assertEquals(0, this.game.getWinner());

    this.game.nextTurn();
    this.game.receiveSummonAction(); // For player 1.

    assertEquals(-1, this.game.getWinner());
  }
}
