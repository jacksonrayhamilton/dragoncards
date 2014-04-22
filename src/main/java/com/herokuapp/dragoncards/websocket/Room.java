package com.herokuapp.dragoncards.websocket;

import javax.json.Json;
import javax.json.JsonValue;

import com.herokuapp.dragoncards.JsonSerializable;
import com.herokuapp.dragoncards.game.Game;

public class Room implements JsonSerializable {

  // TODO: Add collection of players.
  private long id;
  private Game game;

  public long getId() {
    return this.id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Game getGame() {
    return game;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  @Override
  public JsonValue toJson() {
    return Json.createObjectBuilder()
        .add("id", this.getId())
        .build();
  }
}
