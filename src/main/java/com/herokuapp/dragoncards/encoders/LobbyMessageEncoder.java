package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.messages.server.LobbyMessage;

public class LobbyMessageEncoder implements
    Encoder.Text<LobbyMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(LobbyMessage message) throws EncodeException {
    JsonObjectBuilder objectBuilder = Json.createObjectBuilder()
        .add("toClient", "lobby");
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

    for (Player player : message.getPlayers()) {
      arrayBuilder.add(player.toJson());
    }

    return objectBuilder
        .add("players", arrayBuilder)
        .build()
        .toString();
  }

}
