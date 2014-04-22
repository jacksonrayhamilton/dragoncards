package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.server.QueryPlayerNameMessage;

public class QueryPlayerNameMessageEncoder implements
    Encoder.Text<QueryPlayerNameMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(QueryPlayerNameMessage message) throws EncodeException {
    return Json.createObjectBuilder()
        .add("toClient", "queryPlayerName")
        .build()
        .toString();
  }

}
