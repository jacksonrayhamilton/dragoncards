package com.herokuapp.dragoncards.decoders;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.herokuapp.dragoncards.messages.Message;
import com.herokuapp.dragoncards.messages.client.AnswerDuelRequestMessage;
import com.herokuapp.dragoncards.messages.client.BattleActionsMessage;
import com.herokuapp.dragoncards.messages.client.ExitRoomMessage;
import com.herokuapp.dragoncards.messages.client.PreliminaryActionMessage;
import com.herokuapp.dragoncards.messages.client.RequestDuelMessage;
import com.herokuapp.dragoncards.messages.client.SetPlayerNameMessage;

public class MessageDecoder implements Decoder.Text<Message> {

  @Override
  public void destroy() {
  }

  @Override
  public void init(EndpointConfig endpointConfig) {
  }

  @Override
  public Message decode(String message) throws DecodeException {
    JsonReader reader = Json.createReader(new StringReader(message));
    JsonObject json = (JsonObject) reader.read(); // I HOPE it's an object!
    String messageType = json.getString("toServer");
    switch (messageType) {
      case "setPlayerName":
        return new SetPlayerNameMessage(json);
      case "requestDuel":
        return new RequestDuelMessage(json);
      case "answerDuelRequest":
        return new AnswerDuelRequestMessage(json);
      case "preliminaryAction":
        return new PreliminaryActionMessage(json);
      case "battleActions":
        return new BattleActionsMessage(json);
      case "exitRoom":
        return new ExitRoomMessage();
      default:
        return null;
    }
  }

  @Override
  public boolean willDecode(String message) {
    // TODO: Do hardcore error checking. Add error checkers as static methods
    // to the classes. Non-well-formed messages cause errors.
    return true;
  }

}
