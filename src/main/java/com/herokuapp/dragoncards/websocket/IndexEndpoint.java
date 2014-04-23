package com.herokuapp.dragoncards.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.herokuapp.dragoncards.Player;
import com.herokuapp.dragoncards.State;
import com.herokuapp.dragoncards.decoders.MessageDecoder;
import com.herokuapp.dragoncards.encoders.CreatePlayerMessageEncoder;
import com.herokuapp.dragoncards.encoders.DrawMessageEncoder;
import com.herokuapp.dragoncards.encoders.DuelRequestAnsweredMessageEncoder;
import com.herokuapp.dragoncards.encoders.DuelRequestedMessageEncoder;
import com.herokuapp.dragoncards.encoders.MovePlayerToLobbyMessageEncoder;
import com.herokuapp.dragoncards.encoders.MovePlayerToRoomMessageEncoder;
import com.herokuapp.dragoncards.encoders.OpponentDrawMessageEncoder;
import com.herokuapp.dragoncards.encoders.OpponentPilferMessageEncoder;
import com.herokuapp.dragoncards.encoders.OpponentSummonMessageEncoder;
import com.herokuapp.dragoncards.encoders.QueryBattleActionsMessageEncoder;
import com.herokuapp.dragoncards.encoders.QueryPlayerNameMessageEncoder;
import com.herokuapp.dragoncards.encoders.QueryPreliminaryActionMessageEncoder;
import com.herokuapp.dragoncards.encoders.UpdateLobbyMessageEncoder;
import com.herokuapp.dragoncards.messages.Message;
import com.herokuapp.dragoncards.messages.client.RequestDuelMessage;
import com.herokuapp.dragoncards.messages.client.SetPlayerNameMessage;
import com.herokuapp.dragoncards.messages.server.CreatePlayerMessage;
import com.herokuapp.dragoncards.messages.server.DuelRequestAnsweredMessage;
import com.herokuapp.dragoncards.messages.server.DuelRequestedMessage;
import com.herokuapp.dragoncards.messages.server.MovePlayerToLobbyMessage;
import com.herokuapp.dragoncards.messages.server.QueryPlayerNameMessage;

@ServerEndpoint(
    value = "/index",
    encoders = {
        CreatePlayerMessageEncoder.class,
        DrawMessageEncoder.class,
        DuelRequestAnsweredMessageEncoder.class,
        DuelRequestedMessageEncoder.class,
        MovePlayerToLobbyMessageEncoder.class,
        MovePlayerToRoomMessageEncoder.class,
        OpponentDrawMessageEncoder.class,
        OpponentPilferMessageEncoder.class,
        OpponentSummonMessageEncoder.class,
        QueryBattleActionsMessageEncoder.class,
        QueryPlayerNameMessageEncoder.class,
        QueryPreliminaryActionMessageEncoder.class,
        UpdateLobbyMessageEncoder.class
    },
    decoders = {
        MessageDecoder.class
    })
public class IndexEndpoint {

  private static final Logger logger = Logger.getLogger("IndexEndpoint");
  private static Map<String, Session> sessions = new ConcurrentHashMap<>();
  private static Map<String, Player> players = new ConcurrentHashMap<>();
  private static Lobby lobby = new Lobby();

  private static Player getPlayer(Session session) {
    return (Player) session.getUserProperties().get("player");
  }

  private static Future<Void> sendMessage(Session session, Object message) {
    return session.getAsyncRemote().sendObject(message);
  }

  /**
   * Handles users' attempts to set their names.
   * 
   * Has the side effect of adding the player to the lobby.
   * 
   * @param session
   * @param message
   */
  private void onSetPlayerNameMessage(Session session,
      SetPlayerNameMessage message) {

    Player player = getPlayer(session);
    String newName = message.getName();

    if (newName.length() > 0) {
      player.setName(newName);
    }

    // Alert the client of his player's creation.
    sendMessage(session, new CreatePlayerMessage(player));

    // Add the client's player to the lobby.
    lobby.addPlayer(player);

    // Alert the client of the players currently in the lobby.
    sendMessage(session, new MovePlayerToLobbyMessage(lobby.getPlayerList()));
  }

  /**
   * Handles users' attempts to request duels of other players.
   * 
   * @param session
   * @param message
   */
  private void onRequestDuelMessage(Session session,
      RequestDuelMessage message) {

    Player requester = getPlayer(session);
    String requesteeUuid = message.getUuid();
    Player requestee = players.get(requesteeUuid);

    logger.log(
        Level.INFO,
        String.format("%s requested a duel with %s.",
            requester.getInformationalName(), requestee.getInformationalName()));

    // Deny if requester is self or not in lobby.
    if (requester.equals(requestee) ||
        requester.getState() != State.IN_LOBBY) {
      return;
    }

    // Automatically reject if the requestee is not in the lobby.
    if (requestee.getState() != State.IN_LOBBY) {
      sendMessage(session, new DuelRequestAnsweredMessage(false));
      return;
    }

    // Send the duel request to the requestee.
    Session requesteeSession = sessions.get(requesteeUuid);
    sendMessage(requesteeSession, new DuelRequestedMessage(requester));
  }

  @OnMessage
  public void onMessage(Session session, Message message) {
    if (message instanceof SetPlayerNameMessage) {
      this.onSetPlayerNameMessage(session, (SetPlayerNameMessage) message);
    } else if (message instanceof RequestDuelMessage) {
      this.onRequestDuelMessage(session, (RequestDuelMessage) message);
    }
  }

  @OnOpen
  public void openConnection(Session session) {

    // Create a player so that its UUID can be used for mapping.
    Player player = new Player();
    session.getUserProperties().put("player", player);
    players.put(player.getUuid(), player);
    sessions.put(player.getUuid(), session);

    logger.log(Level.INFO, "Connection opened.");

    // Ask the player for his name.
    sendMessage(session, new QueryPlayerNameMessage());
  }

  /**
   * Removes all traces of the user associated with a given session.
   * 
   * @param session
   */
  private void cleanUpClient(Session session) {
    Player player = getPlayer(session);
    lobby.removePlayer(player);
    players.remove(player.getUuid());
    sessions.remove(session);
  }

  @OnClose
  public void closedConnection(Session session) {
    this.cleanUpClient(session);
    logger.log(Level.INFO, "Connection closed.");
  }

  @OnError
  public void error(Session session, Throwable t) {
    this.cleanUpClient(session);
    logger.log(Level.INFO, t.toString());
    logger.log(Level.INFO, "Connection error.");
  }

}
