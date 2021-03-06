package com.herokuapp.dragoncards.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonValue;

import com.herokuapp.dragoncards.JsonSerializable;
import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.State;
import com.herokuapp.dragoncards.game.Game;
import com.herokuapp.dragoncards.game.RandomNumberGenerator;

public class Room implements JsonSerializable {

  private final String uuid;
  private Game game;
  private final List<Player> players;

  public Room() {
    this.uuid = UUID.randomUUID().toString();
    this.players = new ArrayList<Player>();
  }

  public void addPlayer(Player player) {
    this.players.add(player);
    player.setState(State.DUELING);
    player.setRoomUuid(this.uuid);
  }

  public void removePlayer(Player player) {
    this.players.remove(player);
    player.leaveRoom();
  }

  public void initializeGame() {
    Player[] playerArgs = this.players.toArray(new Player[this.players.size()]);
    this.game =
        new Game(playerArgs, RandomNumberGenerator.inclusiveRange(0,
            playerArgs.length - 1));
  }

  public String getUuid() {
    return this.uuid;
  }

  public Game getGame() {
    return this.game;
  }

  public Player getTurnPlayer() {
    return this.players.get(this.game.getTurnPlayer());
  }

  public Player getNonTurnPlayer() {
    return this.players.get(this.game.getNonTurnPlayer());
  }

  @Override
  public JsonValue toJson() {
    return Json.createObjectBuilder()
        .add("uuid", this.uuid)
        .build();
  }

}
