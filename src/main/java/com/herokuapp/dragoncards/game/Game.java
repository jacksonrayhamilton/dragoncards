package com.herokuapp.dragoncards.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonValue;

import com.herokuapp.dragoncards.JsonSerializable;
import com.herokuapp.dragoncards.Player;

/**
 * Does the heavy lifting for the game's logic. Manages the state of the game
 * and relays messages between participating clients.
 * 
 * @author Jackson Hamilton
 */
public class Game implements JsonSerializable {

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

    public PlayerGameData() {
      this.hand = new Hand();
      this.discardPile = new DiscardPile();
      this.readyToBattle = false;
    }
  }

  public static final int DRAGONS_PER_PLAYER = 2;
  public static final int HAND_SIZE_LIMIT = 6;

  private Player[] players;
  private int playerCount;
  private Map<Player, Integer> playerIndexes;
  private PlayerGameData[] playerGameData;
  private Deck deck;
  private int turnPlayer;
  private boolean battleHasBegun;

  private List<BattleAction> battleActions;

  public Game(Player... players) {
    this.players = players;

    this.playerCount = this.players.length;
    this.playerIndexes = new HashMap<>(this.playerCount);
    this.playerGameData = new PlayerGameData[this.playerCount];

    for (int i = 0; i < this.playerCount; i++) {
      this.playerIndexes.put(this.players[i], i);
      this.playerGameData[i] = new PlayerGameData();
    }

    this.battleHasBegun = false;
    this.battleActions = new ArrayList<>();
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

  public int getPlayerIndex(Player player) {
    return this.playerIndexes.get(player);
  }

  public Hand getHand(int player) {
    return this.playerGameData[player].hand;
  }

  public Hand getHand(Player player) {
    return this.getHand(this.getPlayerIndex(player));
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
    List<Dragon> dragons = this.getDragons(player);
    int deaths = 0;
    for (Dragon dragon : dragons) {
      if (dragon.isDead()) {
        deaths++;
      }
    }
    return deaths;
  }

  public List<BattleAction> getBattleActions() {
    return this.battleActions;
  }

  public void addBattleActions(List<BattleAction> actions) {
    this.battleActions.addAll(actions);
  }

  public Deck getDeck() {
    return this.deck;
  }

  public int getTurnPlayer() {
    return this.turnPlayer;
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
    boolean[] deadPlayers = new boolean[this.playerCount]; // default false
    for (int player = 0; player < this.playerCount; player++) {
      if (this.getDeaths(player) == DRAGONS_PER_PLAYER) {
        deadPlayers[player] = true;
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

  // TODO: Maybe should be some hook to call this automatically once both
  // players can summon.
  public void beginBattle() {
    this.battleHasBegun = true;
  }

  public void queryBattleActions() {

  }

  public void attackWithDragon(int player, int attacker, int target) {
    Dragon attackingDragon = this.getDragons(player).get(attacker);
    int otherPlayer = player == 0 ? 1 : 0;
    Dragon targetDragon = this.getDragons(otherPlayer).get(target);

    if (targetDragon.isCountering()) {
      double damage = targetDragon.getDamage();
      attackingDragon.takeDamage(targetDragon.getElement(), damage);
    } else {
      double damage = attackingDragon.getDamage();
      targetDragon.takeDamage(attackingDragon.getElement(), damage);
    }
  }

  public void switchDragons(int player) {
    List<Dragon> dragons = this.getDragons(player);
    dragons.add(dragons.remove(0));
  }

  public void counterWithDragon(int player, int dragon) {
    this.getDragons(player).get(dragon).startCountering();
  }

  public void battle() {

    ArrayList<BattleAction> sortedActions = new ArrayList<>();

    // Sort the actions.
    for (BattleAction action : this.getBattleActions()) {
      // Add prioritized actions to the front.
      if (action.getType() == BattleAction.SWITCH ||
          action.getType() == BattleAction.COUNTER) {
        sortedActions.add(0, action);
      } else {
        sortedActions.add(action);
      }
    }

    // Execute all the actions.
    for (BattleAction action : sortedActions) {
      switch (action.getType()) {
        case BattleAction.ATTACK:
          this.attackWithDragon(action.getPlayer(), action.getInitiator(),
              action.getTarget());
          break;
        case BattleAction.SWITCH:
          this.switchDragons(action.getPlayer());
          break;
        case BattleAction.COUNTER:
          this.counterWithDragon(action.getPlayer(), action.getInitiator());
          break;
        default:
          break;
      }
    }

    this.battleCleanup();
  }

  public void battleCleanup() {
    for (int player = 0; player < this.playerCount; player++) {
      for (Dragon dragon : this.getDragons(player)) {
        dragon.stopCountering();
      }
    }
    this.battleActions.clear();
  }

  public void receiveBattleActions(List<BattleAction> actions) {
    this.addBattleActions(actions);
  }

  public int getPlayerCount() {
    return this.playerCount;
  }

  @Override
  public JsonValue toJson() {
    return Json.createObjectBuilder()
        .add("turnPlayer", this.turnPlayer)
        .add("deck", this.deck.toJson())
        .build();
  }

}
