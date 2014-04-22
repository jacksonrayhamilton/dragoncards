package com.herokuapp.dragoncards.messages.server;

import com.herokuapp.dragoncards.game.Card;
import com.herokuapp.dragoncards.messages.Message;

public class DrawMessage extends Message {
  public Card card;

  public DrawMessage(Card card) {
    this.card = card;
  }
}
