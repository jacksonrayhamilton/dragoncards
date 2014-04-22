package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.websocket.Room;

public class MovePlayerToRoomMessage {
  public Room room;

  public MovePlayerToRoomMessage(Room room) {
    this.room = room;
  }
}
