package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.game.Game;
import com.herokuapp.dragoncards.messages.server.MovePlayerToRoomMessage;

public class MovePlayerToRoomMessageEncoder implements
    Encoder.Text<MovePlayerToRoomMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(MovePlayerToRoomMessage message) throws EncodeException {
    Game game = message.getRoom().getGame();
    return Json.createObjectBuilder()
        .add("toClient", "movePlayerToRoom")
        .add("room", message.getRoom().toJson())
        .add("game", game.toJson())
        .add("yourHand", game.getHand(message.getPlayer()).toJson())
        .add("opponentHand", Json.createObjectBuilder()
            .add("size", game.getHand(0).size())) // A little smelly but ok.
        .build()
        .toString();
  }

}
