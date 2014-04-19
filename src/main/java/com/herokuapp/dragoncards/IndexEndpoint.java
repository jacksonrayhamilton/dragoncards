package com.herokuapp.dragoncards;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/echo")
public class IndexEndpoint {

  @OnMessage
  public void onMessage(Session session, String msg) {
    session.getAsyncRemote().sendText(msg);
  }

}
