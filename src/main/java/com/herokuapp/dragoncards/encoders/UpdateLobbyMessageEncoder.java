package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.messages.server.UpdateLobbyMessage;

public class UpdateLobbyMessageEncoder implements
    Encoder.Text<UpdateLobbyMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(UpdateLobbyMessage message) throws EncodeException {
    JsonObjectBuilder objectBuilder = Json.createObjectBuilder()
        .add("toClient", "updateLobby");

    JsonArrayBuilder arrayBuilder1 = Json.createArrayBuilder();
    JsonArrayBuilder arrayBuilder2 = Json.createArrayBuilder();

    for (Player player : message.playersJoined) {
      arrayBuilder1.add(player.toJson());
    }

    for (Player player : message.playersLeft) {
      arrayBuilder2.add(player.toJson());
    }

    return objectBuilder
        .add("playersJoined", arrayBuilder1)
        .add("playersLeft", arrayBuilder2)
        .build()
        .toString();
  }

}
