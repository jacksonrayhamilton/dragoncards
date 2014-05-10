package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.server.OpponentDiscardMessage;

public class OpponentDiscardMessageEncoder implements
    Encoder.Text<OpponentDiscardMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(OpponentDiscardMessage message) throws EncodeException {
    return Json.createObjectBuilder()
        .add("toClient", "opponentDiscard")
        .add("card", message.getCard().toJson())
        .build()
        .toString();
  }

}
