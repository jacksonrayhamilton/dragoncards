package com.herokuapp.dragoncards;

import javax.json.JsonValue;

public interface JsonSerializable {
  public JsonValue toJson();
}
