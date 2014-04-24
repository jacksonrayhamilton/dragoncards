package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.server.DuelRequestAnsweredMessage;

public class DuelRequestAnsweredMessageEncoder implements
    Encoder.Text<DuelRequestAnsweredMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(DuelRequestAnsweredMessage message)
      throws EncodeException {
    return Json.createObjectBuilder()
        .add("toClient", "duelRequestAnswered")
        .add("duelRequest", message.getDuelRequest().toJson())
        .add("accept", message.isAccept())
        .build()
        .toString();
  }

}
