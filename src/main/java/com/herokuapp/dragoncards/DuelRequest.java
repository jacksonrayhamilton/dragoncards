package com.herokuapp.dragoncards;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonValue;
import javax.websocket.Session;

public class DuelRequest implements JsonSerializable {

  private final Player requester;
  private final Session requesterSession;
  private final String uuid;

  public DuelRequest(Player requester, Session requesterSession) {
    this.requester = requester;
    this.requesterSession = requesterSession;
    this.uuid = UUID.randomUUID().toString();
  }

  public Player getRequester() {
    return this.requester;
  }

  public Session getRequesterSession() {
    return this.requesterSession;
  }

  public String getUuid() {
    return this.uuid;
  }

  public boolean equals(DuelRequest duelRequest) {
    return this.getUuid().equals(duelRequest.getUuid());
  }

  @Override
  public JsonValue toJson() {
    return Json.createObjectBuilder()
        .add("requester", this.getRequester().toJson())
        .add("uuid", this.getUuid())
        .build();
  }

}
