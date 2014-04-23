package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.server.DuelRequestedMessage;

public class DuelRequestedMessageEncoder implements
    Encoder.Text<DuelRequestedMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(DuelRequestedMessage message) throws EncodeException {
    return Json.createObjectBuilder()
        .add("toClient", "duelRequested")
        .add("duelRequest", message.getDuelRequest().toJson())
        .build()
        .toString();
  }

}
