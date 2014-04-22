package com.herokuapp.dragoncards.encoders;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.game.Dragon;
import com.herokuapp.dragoncards.messages.server.OpponentSummonMessage;

public class OpponentSummonMessageEncoder implements
    Encoder.Text<OpponentSummonMessage> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig arg0) {
  }

  @Override
  public String encode(OpponentSummonMessage message) throws EncodeException {
    JsonObjectBuilder objectBuilder = Json.createObjectBuilder()
        .add("toClient", "opponentSummon");

    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

    for (Dragon dragon : message.dragons) {
      arrayBuilder.add(dragon.toJson());
    }

    return objectBuilder
        .add("dragons", arrayBuilder)
        .build()
        .toString();
  }

}
