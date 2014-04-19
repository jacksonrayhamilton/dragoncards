package com.herokuapp.dragoncards;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/echo")
public class IndexEndpoint {

  private static final Logger logger = Logger.getLogger("IndexEndpoint");

  static Queue<Session> queue = new ConcurrentLinkedQueue<>();

  @OnMessage
  public void onMessage(Session session, String msg) {
    session.getAsyncRemote().sendText(msg);
  }

  @OnOpen
  public void openConnection(Session session) {
    /* Register this connection in the queue */
    queue.add(session);
    logger.log(Level.INFO, "Connection opened.");
  }

  @OnClose
  public void closedConnection(Session session) {
    /* Remove this connection from the queue */
    queue.remove(session);
    logger.log(Level.INFO, "Connection closed.");
  }

  @OnError
  public void error(Session session, Throwable t) {
    /* Remove this connection from the queue */
    queue.remove(session);
    logger.log(Level.INFO, t.toString());
    logger.log(Level.INFO, "Connection error.");
  }

}
