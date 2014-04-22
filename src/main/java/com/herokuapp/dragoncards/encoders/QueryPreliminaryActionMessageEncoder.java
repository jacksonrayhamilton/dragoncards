package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.server.QueryPreliminaryActionMessage;

public class QueryPreliminaryActionMessageEncoder implements
    Encoder.Text<QueryPreliminaryActionMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(QueryPreliminaryActionMessage message)
      throws EncodeException {
    return Json.createObjectBuilder()
        .add("toClient", "queryPreliminaryAction")
        .build()
        .toString();
  }

}
