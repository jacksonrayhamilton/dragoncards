package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.websocket.Room;

public class MovePlayerToRoomMessage {
  public Player player;
  public Room room;

  public MovePlayerToRoomMessage(Player player, Room room) {
    this.player = player;
    this.room = room;
  }
}
