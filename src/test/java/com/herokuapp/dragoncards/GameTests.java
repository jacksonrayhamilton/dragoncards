package com.herokuapp.dragoncards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.herokuapp.dragoncards.game.BattleAction;
import com.herokuapp.dragoncards.game.Card;
import com.herokuapp.dragoncards.game.CollectAction;
import com.herokuapp.dragoncards.game.Deck;
import com.herokuapp.dragoncards.game.DiscardPile;
import com.herokuapp.dragoncards.game.Dragon;
import com.herokuapp.dragoncards.game.Game;
import com.herokuapp.dragoncards.game.Hand;

public class GameTests {

  private Player a;
  private Player b;
  private Game game;

  @Before
  public void setUp() {
    this.a = new Player("A", State.DUELING);
    this.b = new Player("B", State.DUELING);
    this.game = new Game(this.a, this.b);
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

  private void setupHandForSummoning() {
    int otherPlayer = this.game.getTurnPlayer() == 0 ? 1 : 0;
    Hand otherHand = this.game.getHand(otherPlayer);
    DiscardPile otherDiscardPile = this.game.getDiscardPile(otherPlayer);
    Deck deck = this.game.getDeck();

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
      this.setupHandForSummoning();
      this.game.receiveSummonAction();
      assertTrue(this.game.isReadyToBattle(i));
      assertEquals(2, this.game.getDragons(i).size());
      this.game.nextTurn();
    }
  }

  @Test
  public void GettingWinner_DifferentWinners_CorrectCode() {
    assertEquals(-1, this.game.getWinner());

    this.setupHandForSummoning(); // Deck is empty now.
    this.game.nextTurn(); // Player 1's turn.
    this.setupHandForSummoning();
    this.game.nextTurn(); // Player 0's turn.

    assertEquals(-2, this.game.getWinner()); // Tied since deck is empty.

    this.game.receiveSummonAction(); // For player 0.
    assertEquals(0, this.game.getWinner());

    this.game.nextTurn();
    this.game.receiveSummonAction(); // For player 1.

    assertEquals(-1, this.game.getWinner());
  }

  private void setupForBattling() {
    this.setupHandForSummoning();
    this.game.receiveSummonAction();
    this.game.nextTurn(); // Player 1's turn.
    this.setupHandForSummoning();
    this.game.receiveSummonAction();
    this.game.nextTurn(); // Player 0's turn.
    this.game.beginBattle();
  }

  @Test
  public void Battling_Attacking_PlayerWins() {
    this.setupForBattling();

    assertEquals(4, this.game.getLivingDragonCount());

    int count = 0;
    while (this.game.getWinner() == -1 && count++ < 1000) {
      List<BattleAction> actions = new ArrayList<>();

      int[] targets = new int[] {
          this.game.getDragons(1).get(0).isDead() == true ? 1 : 0,
          this.game.getDragons(0).get(0).isDead() == true ? 1 : 0
      };

      for (int player = 0; player < this.game.getPlayerCount(); player++) {
        for (int dragon = 0; dragon < Game.DRAGONS_PER_PLAYER; dragon++) {
          if (!this.game.getDragons(player).get(dragon).isDead()) {
            actions.add(new BattleAction("ATTACK", player, dragon,
                targets[player]));
          }
        }
      }

      this.game.receiveBattleActions(actions);
      this.game.battle();
    }

    assertTrue(this.game.getWinner() != -1);
  }

  @Test
  public void Battling_Countering_AttackerTakesDamage() {
    this.setupForBattling();
    List<BattleAction> actions = new ArrayList<>();
    actions.add(new BattleAction("ATTACK", 0, 0, 0));
    actions.add(new BattleAction("COUNTER", 1, 0));
    this.game.receiveBattleActions(actions);
    this.game.battle();
    Dragon counteredDragon = this.game.getDragons(0).get(0);
    assertTrue(counteredDragon.getLife() < counteredDragon.getMaxLife());
  }

  @Test
  public void Battling_Switching_SwitchedTakesDamage() {
    this.setupForBattling();
    List<BattleAction> actions = new ArrayList<>();
    actions.add(new BattleAction("ATTACK", 0, 0, 0));
    actions.add(new BattleAction("SWITCH", 1, 0));
    Dragon undamagedDragon = this.game.getDragons(1).get(0); // Apparently the
                                                             // target of the
                                                             // attack but he
                                                             // will swap.
    Dragon damagedDragon = this.game.getDragons(1).get(1); // Dragon 1 will move
                                                           // to spot 0 and thus
                                                           // take the damage.
    this.game.receiveBattleActions(actions);
    this.game.battle();
    assertTrue(undamagedDragon.getLife() == undamagedDragon.getMaxLife());
    assertTrue(damagedDragon.getLife() < damagedDragon.getMaxLife());
  }
}
