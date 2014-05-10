package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.messages.server.GameoverMessage;

public class GameoverMessageEncoder implements
    Encoder.Text<GameoverMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(GameoverMessage message) throws EncodeException {
    JsonObjectBuilder objectBuilder = Json.createObjectBuilder()
        .add("toClient", "gameover");

    Player winner = message.getWinner();

    if (winner != null) {
      objectBuilder.add("winner", winner.toJson());
    } else {
      objectBuilder.add("draw", true);
    }

    return objectBuilder
        .build()
        .toString();
  }

}
