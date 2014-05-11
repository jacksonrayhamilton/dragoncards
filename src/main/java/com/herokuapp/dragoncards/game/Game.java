package com.herokuapp.dragoncards.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
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

  /**
   * Constructs a game with a specific turn player (probably set by
   * `RandomNumberGenerator.inclusiveRange(0, players.length - 1)`).
   * 
   * @param players
   * @param turnPlayer
   */
  public Game(Player[] players, int turnPlayer) {
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
    this.turnPlayer = turnPlayer;

    // Each player draws 6 cards.
    for (int i = 0; i < HAND_SIZE_LIMIT * this.playerCount; i++) {
      Card card = this.deck.draw();
      this.getHand(i / 6).add(card);
    }
  }

  /**
   * Constructs a game with the first player being player 0. (Deterministic.)
   * 
   * @param players
   */
  public Game(Player... players) {
    this(players, 0);
  }

  public int getPlayerIndex(Player player) {
    return this.playerIndexes.get(player);
  }

  public Player getPlayerByIndex(int index) {
    return this.players[index];
  }

  /**
   * Translates ActionTarget.SELF or ActionTarget.OPPONENT to the correct player
   * index.
   * 
   * @param target
   * @param player
   * @return
   */
  public int actionTargetToPlayerIndex(ActionTarget target, Player player) {
    int index = this.getPlayerIndex(player);
    if (target.equals(ActionTarget.SELF)) {
      return index;
    } else if (target.equals(ActionTarget.OPPONENT)) {
      return index == 0 ? 1 : 0;
    }
    return -1; // Should never happen.
  }

  public ActionTarget playerIndexToActionTarget(int index, Player player) {
    Player indexedPlayer = this.players[index];
    if (indexedPlayer.equals(player)) {
      return ActionTarget.SELF;
    } else {
      return ActionTarget.OPPONENT;
      // TODO: Update the message class
    }
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

  public List<Dragon> getDragons(Player player) {
    return this.getDragons(this.getPlayerIndex(player));
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

  public int getNonTurnPlayer() {
    return this.turnPlayer == 0 ? 1 : 0;
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

  public Card receiveCollectAction(CollectAction action, int... target) {
    Card card = null; // Should never be null.
    if (action == CollectAction.DRAW) {
      card = this.deck.draw();
      this.getHand(this.turnPlayer).add(card);
    } else if (action == CollectAction.PILFER) {
      card = this.pilfer(target[0]);
      this.getHand(this.turnPlayer).add(card);
    }
    return card;
  }

  /**
   * @return Whether or not the turn player has 2 sets (and therefore can
   *         summon).
   */
  public boolean canSummon() {
    int sets = 0;
    Hand hand = this.getHand(this.turnPlayer);
    List<Card> unsummonableCards = new ArrayList<>(hand.size());

    for (int i = 0, size = hand.size(); i < size; i++) {
      Card card = hand.get(i);

      if (unsummonableCards.contains(card)) {
        continue;
      }

      int count = 1;
      for (int j = i + 1; j < size; j++) {
        Card otherCard = hand.get(j);
        if (card.equals(otherCard)) {
          count++;
          if (count == 3) {
            sets++;
            break;
          }
        }
      }

      if (count < 3) {
        unsummonableCards.add(card);
      }
    }

    return sets == 2;
  }

  /**
   * Summons the turn player's dragons. Assumes that the hand has already been
   * checked for 2 sets of dragons. The the first 2 sets that are found are
   * summoned.
   */
  public void turnPlayerSummon() {
    Hand hand = this.getHand(this.turnPlayer);
    List<Card> unsummonableCards = new ArrayList<>(hand.size());
    ArrayList<Dragon> summonedDragons = new ArrayList<>(2);

    for (int i = 0, size = hand.size(); i < size; i++) {
      Card card = hand.get(i);

      if (unsummonableCards.contains(card)) {
        continue;
      }

      int count = 1;
      for (int j = i + 1; j < size; j++) {
        Card otherCard = hand.get(j);
        if (card.equals(otherCard)) {
          count++;
          if (count == 3) {
            summonedDragons.add(new Dragon(card));
          }
        }
      }

      // Don't check the same card again.
      unsummonableCards.add(card);

      if (summonedDragons.size() == DRAGONS_PER_PLAYER) {
        // End early if 2 dragons were found.
        break;
      }
    }

    this.setDragons(this.turnPlayer, summonedDragons);
    this.makeReadyToBattle(this.turnPlayer);
  }

  // The conditions for summoning should be checked ahead-of-time.
  public void receiveSummonAction() {
    this.turnPlayerSummon();
  }

  /**
   * Draws the remainder of the deck into the turn player's hand in an attempt
   * to get him a set of dragons.
   */
  public void obligatorySummon() {
    Hand hand = this.getHand(this.turnPlayer);
    while (!this.canSummon() && !this.deck.isEmpty()) {
      hand.add(this.deck.draw());
    }
    if (this.canSummon()) {
      this.turnPlayerSummon();
    }
  }

  public void queryDiscardAction() {

  }

  public Card receiveDiscardAction(Card card) {
    return this.receiveDiscardAction(card.getElement(), card.getLevel());
  }

  public Card receiveDiscardAction(Element element, int level) {
    Card discardedCard = this.getHand(this.turnPlayer).discard(element, level);
    this.getDiscardPile(this.turnPlayer).add(discardedCard);
    return discardedCard;
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

  public boolean isReadyToBattle() {
    return this.battleActions.size() >= this.playerCount * DRAGONS_PER_PLAYER;
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
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

    for (Player player : this.players) {
      arrayBuilder.add(player.toJson());
    }

    return Json.createObjectBuilder()
        .add("players", arrayBuilder)
        .add("turnPlayer", this.players[this.turnPlayer].toJson())
        .add("deck", this.deck.toJson())
        .build();
  }

}
