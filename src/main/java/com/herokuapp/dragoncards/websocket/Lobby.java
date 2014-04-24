package com.herokuapp.dragoncards.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.State;

public class Lobby {
  private Map<String, Player> players;

  public Lobby() {
    this.players = new ConcurrentHashMap<>();
  }

  /**
   * Adds a player to this room and updates his state.
   * 
   * @param player
   *          Player to add.
   */
  public void addPlayer(Player player) {
    this.players.put(player.getUuid(), player);
    player.setState(State.IN_LOBBY);
  }

  /**
   * Determines if the player with the argument uuid is in this lobby. Should be
   * called before getting or removing a player.
   * 
   * @param uuid
   * @return Whether or not the player is in the lobby.
   */
  public boolean hasPlayer(String uuid) {
    return this.players.containsKey(uuid);
  }

  public boolean hasPlayer(Player player) {
    return this.hasPlayer(player.getUuid());
  }

  /**
   * Gets the player with the argument uuid.
   * 
   * @param uuid
   *          UUID of a player.
   * @return A player.
   */
  public Player getPlayer(String uuid) {
    return this.players.get(uuid);
  }

  /**
   * Removes the player with the argument uuid from this room, and sets him to a
   * limbo state. His state should be set to something else afterwards.
   * 
   * @param uuid
   *          UUID of a player.
   * @return The removed player.
   */
  public Player removePlayer(String uuid) {
    Player player = this.players.remove(uuid);
    player.setState(State.IN_LIMBO);
    return player;
  }

  public Player removePlayer(Player player) {
    return this.removePlayer(player.getUuid());
  }

  /**
   * @return A list of all players in this room.
   */
  public List<Player> getPlayerList() {
    return new ArrayList<Player>(this.players.values());
  }
}
