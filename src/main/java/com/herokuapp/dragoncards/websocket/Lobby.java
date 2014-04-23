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

  public void addPlayer(Player player) {
    this.players.put(player.getUuid(), player);
    player.setState(State.IN_LOBBY);
  }

  public Player getPlayer(String uuid) {
    Player player = null;
    try {
      player = this.players.get(uuid);
    } catch (NullPointerException e) {
    }
    return player;
  }

  public void removePlayer(String uuid) {
    Player removed = this.players.remove(uuid);
    removed.setState(State.IN_LIMBO);
  }

  public void removePlayer(Player player) {
    this.removePlayer(player.getUuid());
  }

  public List<Player> getPlayerList() {
    return new ArrayList<Player>(this.players.values());
  }
}
