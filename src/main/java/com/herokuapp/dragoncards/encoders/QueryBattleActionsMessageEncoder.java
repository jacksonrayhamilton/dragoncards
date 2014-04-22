package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.server.QueryBattleActionsMessage;

public class QueryBattleActionsMessageEncoder implements
    Encoder.Text<QueryBattleActionsMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(QueryBattleActionsMessage message)
      throws EncodeException {
    return Json.createObjectBuilder()
        .add("toClient", "queryBattleActions")
        .build()
        .toString();
  }

}
