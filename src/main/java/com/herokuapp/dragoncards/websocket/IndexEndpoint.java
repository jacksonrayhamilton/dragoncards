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
   * Player UUID to player mapping.
   */
  private static Map<String, Player> players = new ConcurrentHashMap<>();

  /**
   * Duel request UUID to duel request mapping.
   */
  private static Map<String, DuelRequest> duelRequests =
      new ConcurrentHashMap<>();

  /**
   * Location for idle duelists to wait for games.
   */
  private static Lobby lobby = new Lobby();

  /**
   * @param session
   * @return The player associated with the argument session.
   */
  private static Player getPlayer(Session session) {
    return (Player) session.getUserProperties().get("player");
  }

  /**
   * @param session
   * @return The UUID associated with the argument session.
   */
  private static String getUuid(Session session) {
    return (String) session.getUserProperties().get("uuid");
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

    // Deny if already named (malicious).
    if (player.getState() != State.NAMING) {
      return;
    }

    String newName = message.getName();

    if (newName.length() > 0) {
      player.setName(newName);
    }

    logger.log(
        Level.INFO,
        String.format("Client `%s' set player name to `%s'.",
            player.getUuid(), player.getName()));

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

    // Ensure the requestee exists.
    Player requestee = players.get(requesteeUuid);
    if (requestee == null) {
      logger.log(Level.WARNING,
          String.format("Requestee `%s' player not found.", requesteeUuid));
      // TODO: Alert requester.
      return;
    }

    logger.log(
        Level.INFO,
        String.format("%s requested a duel with %s.",
            requester.getInformationalName(),
            requestee.getInformationalName()));

    // Deny if requester is self or not in lobby (malicious).
    // TODO: Make it so you can't spam more than one DuelRequest to the same
    // requestee.
    if (requester.equals(requestee) ||
        requester.getState() != State.IN_LOBBY) {
      return;
    }

    DuelRequest duelRequest = new DuelRequest(requester, requestee);

    // Automatically reject if the requestee is not in the lobby.
    // Not necessarily malicious, the requestee could have moved.
    if (requestee.getState() != State.IN_LOBBY) {
      sendMessage(session, new DuelRequestAnsweredMessage(duelRequest, false));
      return;
    }

    // Send the duel request to the requestee.
    Session requesteeSession = sessions.get(requesteeUuid);
    if (requesteeSession == null) {
      logger.log(Level.WARNING,
          String.format("Requestee `%s' session not found.", requesteeUuid));
      // TODO: Alert requester.
      return;
    }
    sendMessage(requesteeSession, new DuelRequestedMessage(duelRequest));

    // Store the duel request for later.
    duelRequests.put(duelRequest.getUuid(), duelRequest);
    requester.addDuelRequest(duelRequest);
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

    DuelRequest duelRequest = duelRequests.get(duelRequestUuid);
    if (duelRequest == null) {
      logger.log(Level.WARNING,
          String.format("Duel request `%s' not found.", duelRequestUuid));
      // TODO: Should probably let the requestee know that the
      // duel request is no longer available. Not necessarily malicious.
      return;
    }

    // Deny if the requestee is not the real requestee (malicious).
    if (!duelRequest.getRequestee().equals(requestee)) {
      return;
    }

    Player requester = duelRequest.getRequester();
    String requesterUuid = requester.getUuid();
    Session requesterSession = sessions.get(requesterUuid);
    if (requesterSession == null) {
      logger.log(Level.WARNING,
          String.format("Session for `%s' not found.", requesterUuid));
      // TODO: Again probably should alert requestee of unavailability.
      return;
    }

    if (message.isAccept()) {
      logger.log(
          Level.INFO,
          String.format("%s accepted a duel with %s.",
              requestee.getInformationalName(),
              requester.getInformationalName()));

      sendMessage(requesterSession, new DuelRequestAnsweredMessage(duelRequest,
          true));

      // Drop all other duel requests the participants had before.
      cleanUpDuelRequests(requester);
      cleanUpDuelRequests(requestee);
      requester.clearDuelRequests();
      requestee.clearDuelRequests();
      // lobby.removePlayer(requester);
      // lobby.removePlayer(requestee);

      // Room room = new Room();
      // room.addPlayer(requester);
      // room.addPlayer(requestee);
      // room.initializeGame();

      // TODO: Add room to a HashMap of rooms.
      // TODO: Send message about gamestate.
    } else {
      logger.log(
          Level.INFO,
          String.format("%s rejected a duel with %s.",
              requestee.getInformationalName(),
              requester.getInformationalName()));

      sendMessage(requesterSession, new DuelRequestAnsweredMessage(duelRequest,
          false));

      // Drop the duel request that was rejected.
      duelRequests.remove(duelRequestUuid); // Existence confirmed.
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

  /**
   * Asks a client to name his player.
   * 
   * @param session
   */
  private static void queryPlayerName(Session session) {
    Player player = getPlayer(session);
    logger.log(
        Level.INFO,
        String.format("Requesting client `%s' for a player name.",
            player.getUuid()));
    player.setState(State.NAMING);
    sendMessage(session, new QueryPlayerNameMessage());
  }

  /**
   * Creates a player object to be associated with the argument session, along
   * with mappings using the newly-created player's uuid.
   * 
   * @param session
   */
  private static void createPlayer(Session session) {
    // Create a player and associate it with this session.
    Player player = new Player();
    session.getUserProperties().put("player", player);

    // Use the newly-created player's UUID for mapping.
    String uuid = player.getUuid();
    session.getUserProperties().put("uuid", uuid);
    players.put(uuid, player);
    sessions.put(uuid, session);

    logger.log(Level.INFO,
        String.format("Created player for client `%s'.", uuid));
  }

  @OnOpen
  public void openConnection(Session session) {
    logger.log(Level.INFO, "Connection opened.");
    createPlayer(session);
    queryPlayerName(session);
  }

  /**
   * Removes all duel requests initiated by the argument player.
   * 
   * @param player
   */
  private static void cleanUpDuelRequests(Player player) {
    for (DuelRequest duelRequest : player.getDuelRequests()) {
      if (duelRequests.containsKey(duelRequest.getUuid())) {
        duelRequests.remove(duelRequest.getUuid());
      }
    }
  }

  /**
   * Removes all traces of the argument session, including all references to the
   * session's player.
   * 
   * @param session
   */
  private static void cleanUpSession(Session session) {
    Player player = getPlayer(session);
    cleanUpDuelRequests(player);
    if (lobby.hasPlayer(player)) {
      lobby.removePlayer(player);
    }
    players.remove(player.getUuid());
    sessions.remove(player.getUuid());
  }

  @OnClose
  public void closedConnection(Session session) {
    cleanUpSession(session);
    logger.log(Level.INFO,
        String.format("Connection closed with client `%s'.", getUuid(session)));
  }

  @OnError
  public void error(Session session, Throwable throwable) {
    cleanUpSession(session);
    logger.log(Level.INFO,
        String.format("Connection error with client `%s'.", getUuid(session)));
    logger.log(Level.INFO, throwable.toString());
  }

}
