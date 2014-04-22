package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.server.OpponentPilferMessage;

public class OpponentPilferMessageEncoder implements
    Encoder.Text<OpponentPilferMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(OpponentPilferMessage message) throws EncodeException {
    return Json.createObjectBuilder()
        .add("toClient", "opponentPilfer")
        .add("target", message.target)
        .build()
        .toString();
  }

}
