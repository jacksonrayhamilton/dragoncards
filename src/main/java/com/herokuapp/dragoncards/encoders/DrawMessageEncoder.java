package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.server.DrawMessage;

public class DrawMessageEncoder implements Encoder.Text<DrawMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(DrawMessage message) throws EncodeException {
    return Json.createObjectBuilder()
        .add("toClient", "draw")
        .add("card", message.card.toJson())
        .build()
        .toString();
  }

}
