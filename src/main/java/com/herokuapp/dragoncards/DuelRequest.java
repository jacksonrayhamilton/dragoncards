package com.herokuapp.dragoncards;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonValue;

public class DuelRequest implements JsonSerializable {

  private final String uuid;
  private final Player requester;
  private final Player requestee;

  public DuelRequest(Player requester, Player requestee) {
    this.uuid = UUID.randomUUID().toString();
    this.requester = requester;
    this.requestee = requestee;
  }

  public String getUuid() {
    return this.uuid;
  }

  public Player getRequester() {
    return this.requester;
  }

  public Player getRequestee() {
    return this.requestee;
  }

  public boolean equals(DuelRequest duelRequest) {
    return this.getUuid().equals(duelRequest.getUuid());
  }

  @Override
  public JsonValue toJson() {
    return Json.createObjectBuilder()
        .add("uuid", this.getUuid())
        .add("requester", this.getRequester().toJson())
        .add("requestee", this.getRequestee().toJson())
        .build();
  }

}
