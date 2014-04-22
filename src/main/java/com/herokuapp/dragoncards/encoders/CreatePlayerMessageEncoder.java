package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.server.CreatePlayerMessage;

public class CreatePlayerMessageEncoder implements
    Encoder.Text<CreatePlayerMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(CreatePlayerMessage message) throws EncodeException {
    return Json.createObjectBuilder()
        .add("toClient", "createPlayer")
        .add("name", message.name)
        .add("uuid", message.uuid)
        .build()
        .toString();
  }

}
