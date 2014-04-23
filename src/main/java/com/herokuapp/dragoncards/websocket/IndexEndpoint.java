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

import com.herokuapp.dragoncards.DuelRequest;
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
import com.herokuapp.dragoncards.messages.client.AnswerDuelRequestMessage;
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

  /**
   * Player UUID to session mapping.
   */
  private static Map<String, Session> sessions = new ConcurrentHashMap<>();

  /**
   * UUID to players mapping.
   */
  private static Map<String, Player> players = new ConcurrentHashMap<>();

  /**
   * Mapping of duel request UUIDs to duel requests.
   */
  private static Map<String, DuelRequest> duelRequests =
      new ConcurrentHashMap<>();

  /**
   * Location for idle duelists to wait for games.
   */
  private static Lobby lobby = new Lobby();

  /**
   * Gets the player associated with the argument session.
   * 
   * @param session
   * @return
   */
  private static Player getPlayer(Session session) {
    return (Player) session.getUserProperties().get("player");
  }

  /**
   * Gets a player with the argument uuid.
   * 
   * @param uuid
   * @return
   */
  private static Player getPlayer(String uuid) {
    try {
      return players.get(uuid);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Gets a session for a requested duel given the uuid of the requestee.
   * 
   * @param uuid
   * @return
   */
  private static DuelRequest getDuelRequest(String uuid) {
    try {
      return duelRequests.get(uuid);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Sends an asyncronous JSON message to a a client.
   * 
   * @param session
   *          Session data for the client.
   * @param message
   *          JSON message to send.
   * @return
   */
  private static Future<Void> sendMessage(Session session, Object message) {
    return session.getAsyncRemote().sendObject(message);
  }

  /**
   * Sets a player's name and adds him to the lobby. Poorly named method. Should
   * only be called once.
   * 
   * @param session
   * @param message
   */
  private void onSetPlayerNameMessage(Session session,
      SetPlayerNameMessage message) {

    Player player = getPlayer(session);

    // Deny if already named.
    if (player.getState() != State.NAMING) {
      return;
    }

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
   * TODO: Make it so you can't spam more than one DuelRequest.
   * 
   * @param session
   * @param message
   */
  private void onRequestDuelMessage(Session session,
      RequestDuelMessage message) {

    Player requester = getPlayer(session);
    String requesteeUuid = message.getUuid();
    Player requestee = getPlayer(requesteeUuid);

    // Deny if the requestee doesn't exist.
    if (requestee == null) {
      return;
    }

    logger.log(
        Level.INFO,
        String.format("%s requested a duel with %s.",
            requester.getInformationalName(),
            requestee.getInformationalName()));

    // Deny if requester is self or not in lobby.
    if (requester.equals(requestee) ||
        requester.getState() != State.IN_LOBBY) {
      return;
    }

    // Automatically reject if the requestee is not in the lobby.
    if (requestee.getState() != State.IN_LOBBY) {
      sendMessage(session, new DuelRequestAnsweredMessage(requestee, false));
      return;
    }

    // Create a duel request with "contact info" for the other player.
    DuelRequest duelRequest = new DuelRequest(requester, session);
    duelRequests.put(duelRequest.getUuid(), duelRequest);
    requester.addDuelRequest(duelRequest);

    // Send the duel request to the requestee.
    Session requesteeSession = sessions.get(requesteeUuid);
    sendMessage(requesteeSession, new DuelRequestedMessage(duelRequest));
  }

  /**
   * Handles users' attempts to answer requests from other players to duel.
   * 
   * @param session
   * @param message
   */
  private void onAnswerDuelRequestMessage(Session session,
      AnswerDuelRequestMessage message) {

    Player requestee = getPlayer(session);
    String duelRequestUuid = message.getUuid();
    DuelRequest duelRequest = getDuelRequest(duelRequestUuid);

    // Deny if no duel request exists.
    if (duelRequest == null) {
      return;
    }

    Player requester = duelRequest.getRequester();
    Session requesterSession = duelRequest.getRequesterSession();

    if (message.isAccept()) {
      logger.log(
          Level.INFO,
          String.format("%s accepted a duel with %s.",
              requestee.getInformationalName(),
              requester.getInformationalName()));

      sendMessage(requesterSession, new DuelRequestAnsweredMessage(requestee,
          true));

      // Drop all other duel requests the participants had before.
      cleanUpDuelRequests(requester);
      cleanUpDuelRequests(requestee);
      requester.clearDuelRequests();
      requestee.clearDuelRequests();
      lobby.removePlayer(requester);
      lobby.removePlayer(requestee);

      Room room = new Room();
      room.addPlayer(requester);
      room.addPlayer(requestee);
      room.initializeGame();

      // TODO: Add room to a HashMap of rooms.
      // TODO: Send message about gamestate.
    } else {
      logger.log(
          Level.INFO,
          String.format("%s rejected a duel with %s.",
              requestee.getInformationalName(),
              requester.getInformationalName()));

      sendMessage(requesterSession, new DuelRequestAnsweredMessage(requestee,
          false));

      // Drop the duel request that was rejected.
      duelRequests.remove(duelRequestUuid);
      requester.removeDuelRequest(duelRequest);
    }
  }

  @OnMessage
  public void onMessage(Session session, Message message) {
    if (message instanceof SetPlayerNameMessage) {
      this.onSetPlayerNameMessage(session, (SetPlayerNameMessage) message);
    } else if (message instanceof RequestDuelMessage) {
      this.onRequestDuelMessage(session, (RequestDuelMessage) message);
    } else if (message instanceof AnswerDuelRequestMessage) {
      this.onAnswerDuelRequestMessage(session,
          (AnswerDuelRequestMessage) message);
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
    player.setState(State.NAMING);
    sendMessage(session, new QueryPlayerNameMessage());
  }

  /**
   * Removes all duel requests initiated by the argument player.
   * 
   * @param player
   */
  private static void cleanUpDuelRequests(Player player) {
    for (DuelRequest duelRequest : player.getDuelRequests()) {
      try {
        duelRequests.remove(duelRequest.getUuid());
      } catch (NullPointerException e) {
      }
    }
  }

  /**
   * Removes all traces of the argument session.
   * 
   * @param session
   */
  private static void cleanUpClient(Session session) {
    Player player = getPlayer(session);
    cleanUpDuelRequests(player);
    lobby.removePlayer(player);
    players.remove(player.getUuid());
    sessions.remove(player.getUuid());
  }

  @OnClose
  public void closedConnection(Session session) {
    cleanUpClient(session);
    logger.log(Level.INFO, "Connection closed.");
  }

  @OnError
  public void error(Session session, Throwable t) {
    cleanUpClient(session);
    logger.log(Level.INFO, t.toString());
    logger.log(Level.INFO, "Connection error.");
  }

}
