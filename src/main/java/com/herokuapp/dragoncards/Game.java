package com.herokuapp.dragoncards;

import java.util.ArrayList;
import java.util.List;

/**
 * Does the heavy lifting for the game's logic. Manages the state of the game
 * and relays messages between participating clients.
 * 
 * @author Jackson Hamilton
 */
public class Game {

  /**
   * Container for all the game pieces owned by an individual player.
   * 
   * @author Jackson Hamilton
   */
  private class PlayerGameData {
    public Hand hand;
    public DiscardPile discardPile;
    public List<Dragon> dragons;
    public boolean readyToBattle;
    public int deaths;
    public List<BattleAction> battleActions;

    public PlayerGameData() {
      this.hand = new Hand();
      this.discardPile = new DiscardPile();
      this.readyToBattle = false;
      this.deaths = 0;
    }
  }

  private static final int DRAGONS_PER_PLAYER = 2;
  private static final int HAND_SIZE_LIMIT = 6;

  private Player[] players;
  private int playerCount;
  private PlayerGameData[] playerGameData;
  private Deck deck;
  private int turnPlayer;
  private boolean battleHasBegun;

  public Game(Player... players) {
    this.players = players;
    this.playerCount = this.players.length;
    this.playerGameData = new PlayerGameData[this.playerCount];

    for (int i = 0; i < this.playerCount; i++) {
      this.playerGameData[i] = new PlayerGameData();
    }

    this.battleHasBegun = false;
    this.deck = new Deck();
    this.turnPlayer = 0; // Should probably be set by
                         // RandomNumberGenerator.inclusiveRange(0,
                         // this.players.length - 1);

    // Each player draws 6 cards.
    for (int i = 0; i < HAND_SIZE_LIMIT * this.playerCount; i++) {
      Card card = this.deck.draw();
      this.getHand(i / 6).add(card);
    }
  }

  public Hand getHand(int player) {
    return this.playerGameData[player].hand;
  }

  public DiscardPile getDiscardPile(int player) {
    return this.playerGameData[player].discardPile;
  }

  public List<Dragon> getDragons(int player) {
    return this.playerGameData[player].dragons;
  }

  public void setDragons(int player, List<Dragon> dragons) {
    this.playerGameData[player].dragons = dragons;
  }

  public boolean isReadyToBattle(int player) {
    return this.playerGameData[player].readyToBattle;
  }

  public void makeReadyToBattle(int player) {
    this.playerGameData[player].readyToBattle = true;
  }

  public int getDeaths(int player) {
    return this.playerGameData[player].deaths;
  }

  public List<BattleAction> getBattleActions(int player) {
    return this.playerGameData[player].battleActions;
  }

  public void setBattleActions(int player, List<BattleAction> actions) {
    this.playerGameData[player].battleActions = actions;
  }

  public Deck getDeck() {
    return this.deck;
  }

  /**
   * Determines which player is the winner using special codes.
   * 
   * @return Either -2 (a tie), -1 (no winner yet), or [0, N) corresponding to
   *         the number of the player who won.
   */
  public int getWinner() {
    if (this.deck.isEmpty()) {
      if (!this.isReadyToBattle(0) && !this.isReadyToBattle(1)) {
        return -2; // Tie.
      } else if (this.isReadyToBattle(0) && !this.isReadyToBattle(1)) {
        return 0; // Player 0 wins by default.
      } else if (!this.isReadyToBattle(0) && this.isReadyToBattle(1)) {
        return 1; // Player 1 wins by default.
      }
    }

    if (!this.battleHasBegun) {
      return -1; // No winner yet.
    }

    // Battle must have begun to reach the following code.
    boolean[] deadPlayers = new boolean[this.players.length]; // default false
    for (int i = 0, length = this.players.length; i < length; i++) {
      if (this.getDeaths(i) == DRAGONS_PER_PLAYER) {
        deadPlayers[i] = true;
      }
    }

    if (deadPlayers[0] && deadPlayers[1]) {
      return -2; // Tie.
    } else if (deadPlayers[0] && !deadPlayers[1]) {
      return 1; // Player 1 is that last one standing.
    } else if (!deadPlayers[0] && deadPlayers[1]) {
      return 0; // Player 0 is that last one standing.
    }

    return -1; // No winner yet.
  }

  public void nextTurn() {
    this.turnPlayer = this.turnPlayer == 0 ? 1 : 0;
  }

  public Card pilfer(int player) {
    return this.getDiscardPile(player).collectTop();
  }

  public void queryCollectAction() {

  }

  public void receiveCollectAction(CollectAction action, int... target) {
    if (action == CollectAction.DRAW) {
      this.getHand(this.turnPlayer).add(this.deck.draw());
    } else if (action == CollectAction.PILFER) {
      this.getHand(this.turnPlayer).add(this.pilfer(target[0]));
    }
  }

  // The conditions for summoning should be checked ahead-of-time, of course.
  public void receiveSummonAction() {
    Hand hand = this.getHand(this.turnPlayer);
    ArrayList<Dragon> summonedDragons = new ArrayList<>(2);

    for (int i = 0, handSize = hand.size(); i < handSize; i++) {
      Card card = hand.get(i);
      if (summonedDragons.isEmpty() || !summonedDragons.contains(card)) {
        summonedDragons.add(new Dragon(card));
        if (summonedDragons.size() == DRAGONS_PER_PLAYER) {
          break;
        }
      }
    }

    this.setDragons(this.turnPlayer, summonedDragons);
    this.makeReadyToBattle(this.turnPlayer);
  }

  public void queryDiscardAction() {

  }

  public void receiveDiscardAction(Card card) {
    this.receiveDiscardAction(card.getElement(), card.getLevel());
  }

  public void receiveDiscardAction(Element element, int level) {
    Card discardedCard = this.getHand(this.turnPlayer).discard(element, level);
    this.getDiscardPile(this.turnPlayer).add(discardedCard);
  }

  public void queryBattleActions() {

  }

  public void attackWithDragon(int attacker, int target) {
    Dragon attackingDragon = this.getDragons(this.turnPlayer).get(attacker);
    int otherPlayer = this.turnPlayer == 0 ? 1 : 0;
    Dragon targetDragon = this.getDragons(otherPlayer).get(target);

    if (targetDragon.isCountering()) {
      double damage = targetDragon.getDamage();
      attackingDragon.takeDamage(targetDragon.getElement(), damage);
    } else {
      double damage = attackingDragon.getDamage();
      targetDragon.takeDamage(attackingDragon.getElement(), damage);
    }
  }

  public void switchDragons() {
    List<Dragon> dragons = this.getDragons(this.turnPlayer);
    dragons.add(dragons.remove(0));
  }

  public void counterWithDragon(int dragon) {
    this.getDragons(this.turnPlayer).get(dragon).startCountering();
  }

  public void battle() {

    ArrayList<BattleAction> actions = new ArrayList<>();

    // Sort the actions.
    for (int i = 0; i < this.playerCount; i++) {
      for (BattleAction action : this.getBattleActions(i)) {
        // Add prioritized actions to the front.
        if (action.getType() == BattleAction.SWITCH ||
            action.getType() == BattleAction.COUNTER) {
          actions.add(0, action);
        } else {
          actions.add(action);
        }
      }
    }

    // Execute all the actions.
    for (BattleAction action : actions) {
      switch (action.getType()) {
        case BattleAction.ATTACK:
          this.attackWithDragon(action.getInitiator(), action.getTarget());
          break;
        case BattleAction.SWITCH:
          this.switchDragons();
          break;
        case BattleAction.COUNTER:
          this.counterWithDragon(action.getInitiator());
          break;
        default:
          break;
      }
    }

    this.battleCleanup();
  }

  public void battleCleanup() {
    for (int i = 0; i < this.playerCount; i++) {
      for (Dragon dragon : this.getDragons(i)) {
        dragon.stopCountering();
      }
    }
  }

  public void receiveBattleActions(List<BattleAction> actions) {
    this.setBattleActions(this.turnPlayer, actions);
  }

}
