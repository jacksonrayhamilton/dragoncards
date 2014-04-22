package com.herokuapp.dragoncards.websocket;

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

import com.herokuapp.dragoncards.decoders.MessageDecoder;

@ServerEndpoint(
    value = "/index",
    encoders = {

    },
    decoders = {
        MessageDecoder.class
    })
public class IndexEndpoint {

  private static final Logger logger = Logger.getLogger("IndexEndpoint");
  static Queue<Session> queue = new ConcurrentLinkedQueue<>();

  @OnMessage
  public void onMessage(Session session, String msg) {
    session.getAsyncRemote().sendText(msg);
  }

  @OnOpen
  public void openConnection(Session session) {
    queue.add(session);
    logger.log(Level.INFO, "Connection opened.");
  }

  @OnClose
  public void closedConnection(Session session) {
    queue.remove(session);
    logger.log(Level.INFO, "Connection closed.");
  }

  @OnError
  public void error(Session session, Throwable t) {
    queue.remove(session);
    logger.log(Level.INFO, t.toString());
    logger.log(Level.INFO, "Connection error.");
  }

}