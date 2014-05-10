package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.server.QueryDiscardActionMessage;

public class QueryDiscardActionMessageEncoder implements
    Encoder.Text<QueryDiscardActionMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(QueryDiscardActionMessage message)
      throws EncodeException {
    return Json.createObjectBuilder()
        .add("toClient", "queryDiscardAction")
        .build()
        .toString();
  }

}
