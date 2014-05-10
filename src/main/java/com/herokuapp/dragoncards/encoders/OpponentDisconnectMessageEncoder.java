package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.server.OpponentDisconnectMessage;

public class OpponentDisconnectMessageEncoder implements
    Encoder.Text<OpponentDisconnectMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(OpponentDisconnectMessage message)
      throws EncodeException {
    return Json.createObjectBuilder()
        .add("toClient", "opponentDisconnect")
        .build()
        .toString();
  }

}
