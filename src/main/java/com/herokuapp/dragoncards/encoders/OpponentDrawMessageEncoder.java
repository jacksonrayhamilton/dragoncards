package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.server.OpponentDrawMessage;

public class OpponentDrawMessageEncoder implements
    Encoder.Text<OpponentDrawMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(OpponentDrawMessage message) throws EncodeException {
    return Json.createObjectBuilder()
        .add("toClient", "opponentDraw")
        .build()
        .toString();
  }

}
