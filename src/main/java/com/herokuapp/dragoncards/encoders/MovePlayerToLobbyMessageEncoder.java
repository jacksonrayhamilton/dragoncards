package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.server.MovePlayerToLobbyMessage;

public class MovePlayerToLobbyMessageEncoder implements
    Encoder.Text<MovePlayerToLobbyMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(MovePlayerToLobbyMessage message) throws EncodeException {
    return Json.createObjectBuilder()
        .add("toClient", "movePlayerToLobby")
        .build()
        .toString();
  }

}
