package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.websocket.Room;

public class MovePlayerToRoomMessage {
  private final Player player;
  private final Room room;

  public MovePlayerToRoomMessage(Player player, Room room) {
    this.player = player;
    this.room = room;
  }

  public Player getPlayer() {
    return this.player;
  }

  public Room getRoom() {
    return this.room;
  }
}
