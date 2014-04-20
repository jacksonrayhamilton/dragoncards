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

    public PlayerGameData() {
      this.hand = new Hand();
      this.discardPile = new DiscardPile();
      this.dragons = new ArrayList<Dragon>();
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

  private ArrayList<Object> messages; // TODO: Find better types.
  private ArrayList<Object> attacks;

  public Game(Player... players) {
    this.players = players;
    this.playerCount = this.players.length;
    this.playerGameData = new PlayerGameData[this.playerCount];

    for (int i = 0; i < this.playerCount; i++) {
      this.playerGameData[i] = new PlayerGameData();
    }

    this.battleHasBegun = false;
    this.deck = new Deck();
    this.turnPlayer =
        RandomNumberGenerator.inclusiveRange(0, this.players.length - 1);
  }

  private Hand getHand(int player) {
    return this.playerGameData[player].hand;
  }

  private DiscardPile getDiscardPile(int player) {
    return this.playerGameData[player].discardPile;
  }

  private List<Dragon> getDragons(int player) {
    return this.playerGameData[player].dragons;
  }

  private void setDragons(int player, List<Dragon> dragons) {
    this.playerGameData[player].dragons = dragons;
  }

  private boolean isReadyToBattle(int player) {
    return this.playerGameData[player].readyToBattle;
  }

  private void makeReadyToBattle(int player) {
    this.playerGameData[player].readyToBattle = true;
  }

  private int getDeaths(int player) {
    return this.playerGameData[player].deaths;
  }

  /**
   * Determines which player is the winner using special codes.
   * 
   * @return Either -2 (a tie), -1 (no winner yet), or [0, N) corresponding to
   *         the number of the player who won.
   */
  public int getWinner() {
    if (this.deck.size() <= 0) {
      if (!this.isReadyToBattle(0) && !this.isReadyToBattle(1)) {
        return -2; // Tie.
      } else if (this.isReadyToBattle(0) && !this.isReadyToBattle(1)) {
        return 0; // Player 0 wins by default.
      } else if (!this.isReadyToBattle(0) && this.isReadyToBattle(1)) {
        return 1; // Player 1 wins by default.
      }
    }

    if (!battleHasBegun) {
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

  private Card pilfer(int player) {
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

  /**
   * This should be checked ahead-of-time, of course.
   */
  public void receiveSummonAction() {
    Hand hand = this.getHand(this.turnPlayer);
    int handSize = hand.size();
    ArrayList<Dragon> summonedDragons = new ArrayList<>(2);

    for (int i = 0; i < handSize; i++) {
      Card card = hand.get(i);
      if (summonedDragons.isEmpty() ||
          !summonedDragons.contains(card)) {
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

  public void receiveDiscardAction(Element element, int level) {
    Card discardedCard = this.getHand(this.turnPlayer).discard(element, level);
    this.getDiscardPile(this.turnPlayer).receiveDiscardedCard(discardedCard);
  }
}
