package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.game.BattleAction;
import com.herokuapp.dragoncards.messages.server.OpponentBattleActionsMessage;

public class OpponentBattleActionsMessageEncoder implements
    Encoder.Text<OpponentBattleActionsMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(OpponentBattleActionsMessage message)
      throws EncodeException {

    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

    for (BattleAction action : message.getActions()) {
      arrayBuilder.add(action.toJson());
    }

    return Json.createObjectBuilder()
        .add("toClient", "opponentBattleActions")
        .add("actions", arrayBuilder)
        .build()
        .toString();
  }

}
