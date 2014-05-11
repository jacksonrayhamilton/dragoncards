package com.herokuapp.dragoncards.websocket;

import java.util.ArrayList;
import java.util.List;
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
import com.herokuapp.dragoncards.encoders.GameoverMessageEncoder;
import com.herokuapp.dragoncards.encoders.LobbyMessageEncoder;
import com.herokuapp.dragoncards.encoders.MovePlayerToLobbyMessageEncoder;
import com.herokuapp.dragoncards.encoders.MovePlayerToRoomMessageEncoder;
import com.herokuapp.dragoncards.encoders.OpponentBattleActionsMessageEncoder;
import com.herokuapp.dragoncards.encoders.OpponentDiscardMessageEncoder;
import com.herokuapp.dragoncards.encoders.OpponentDisconnectMessageEncoder;
import com.herokuapp.dragoncards.encoders.OpponentDrawMessageEncoder;
import com.herokuapp.dragoncards.encoders.OpponentPilferMessageEncoder;
import com.herokuapp.dragoncards.encoders.OpponentSummonMessageEncoder;
import com.herokuapp.dragoncards.encoders.QueryBattleActionsMessageEncoder;
import com.herokuapp.dragoncards.encoders.QueryDiscardActionMessageEncoder;
import com.herokuapp.dragoncards.encoders.QueryPlayerNameMessageEncoder;
import com.herokuapp.dragoncards.encoders.QueryPreliminaryActionMessageEncoder;
import com.herokuapp.dragoncards.encoders.SummonMessageEncoder;
import com.herokuapp.dragoncards.encoders.UpdateLobbyMessageEncoder;
import com.herokuapp.dragoncards.game.ActionTarget;
import com.herokuapp.dragoncards.game.BattleAction;
import com.herokuapp.dragoncards.game.Card;
import com.herokuapp.dragoncards.game.CollectAction;
import com.herokuapp.dragoncards.game.Element;
import com.herokuapp.dragoncards.game.Game;
import com.herokuapp.dragoncards.game.PreliminaryAction;
import com.herokuapp.dragoncards.game.SummonAction;
import com.herokuapp.dragoncards.messages.Message;
import com.herokuapp.dragoncards.messages.client.AnswerDuelRequestMessage;
import com.herokuapp.dragoncards.messages.client.BattleActionsMessage;
import com.herokuapp.dragoncards.messages.client.DiscardActionMessage;
import com.herokuapp.dragoncards.messages.client.ExitRoomMessage;
import com.herokuapp.dragoncards.messages.client.PreliminaryActionMessage;
import com.herokuapp.dragoncards.messages.client.RequestDuelMessage;
import com.herokuapp.dragoncards.messages.client.SetPlayerNameMessage;
import com.herokuapp.dragoncards.messages.server.CreatePlayerMessage;
import com.herokuapp.dragoncards.messages.server.DrawMessage;
import com.herokuapp.dragoncards.messages.server.DuelRequestAnsweredMessage;
import com.herokuapp.dragoncards.messages.server.DuelRequestedMessage;
import com.herokuapp.dragoncards.messages.server.GameoverMessage;
import com.herokuapp.dragoncards.messages.server.LobbyMessage;
import com.herokuapp.dragoncards.messages.server.MovePlayerToLobbyMessage;
import com.herokuapp.dragoncards.messages.server.MovePlayerToRoomMessage;
import com.herokuapp.dragoncards.messages.server.OpponentBattleActionsMessage;
import com.herokuapp.dragoncards.messages.server.OpponentDiscardMessage;
import com.herokuapp.dragoncards.messages.server.OpponentDrawMessage;
import com.herokuapp.dragoncards.messages.server.OpponentPilferMessage;
import com.herokuapp.dragoncards.messages.server.OpponentSummonMessage;
import com.herokuapp.dragoncards.messages.server.QueryBattleActionsMessage;
import com.herokuapp.dragoncards.messages.server.QueryDiscardActionMessage;
import com.herokuapp.dragoncards.messages.server.QueryPlayerNameMessage;
import com.herokuapp.dragoncards.messages.server.QueryPreliminaryActionMessage;
import com.herokuapp.dragoncards.messages.server.SummonMessage;
import com.herokuapp.dragoncards.messages.server.UpdateLobbyMessage;

@ServerEndpoint(
    value = "/index",
    encoders = {
        CreatePlayerMessageEncoder.class,
        DrawMessageEncoder.class,
        DuelRequestAnsweredMessageEncoder.class,
        DuelRequestedMessageEncoder.class,
        GameoverMessageEncoder.class,
        LobbyMessageEncoder.class,
        MovePlayerToLobbyMessageEncoder.class,
        MovePlayerToRoomMessageEncoder.class,
        OpponentBattleActionsMessageEncoder.class,
        OpponentDiscardMessageEncoder.class,
        OpponentDisconnectMessageEncoder.class,
        OpponentDrawMessageEncoder.class,
        OpponentPilferMessageEncoder.class,
        OpponentSummonMessageEncoder.class,
        QueryBattleActionsMessageEncoder.class,
        QueryDiscardActionMessageEncoder.class,
        QueryPlayerNameMessageEncoder.class,
        QueryPreliminaryActionMessageEncoder.class,
        SummonMessageEncoder.class,
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
   * Room UUID to room mapping.
   */
  private static Map<String, Room> rooms = new ConcurrentHashMap<>();

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
   * Sends an asyncronous JSON message to a client.
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
   * Emits a message to all clients.
   * 
   * @param message
   */
  private static void emitMessage(Object message) {
    for (Session session : sessions.values()) {
      sendMessage(session, message);
    }
  }

  /**
   * Determines if a room exists.
   * 
   * @param uuid
   * @return
   */
  private static boolean roomExists(String uuid) {
    return uuid != null && rooms.containsKey(uuid);
  }

  /**
   * Alert all clients of changes to the lobby.
   * 
   * @param playersJoined
   * @param playersLeft
   */
  private static void alertUpdateLobby(List<Player> playersJoined,
      List<Player> playersLeft) {
    emitMessage(new UpdateLobbyMessage(playersJoined, playersLeft));
  }

  /**
   * Sets a player's name and adds him to the lobby. Poorly named method because
   * it has side effects. Should only be called once.
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

    // Alert all other clients of the joined player.
    List<Player> playersJoined = new ArrayList<>(1);
    playersJoined.add(player);
    alertUpdateLobby(playersJoined, new ArrayList<Player>());

    // Instruct the client that he may now move into the lobby.
    // (If you didn't do this he would have received the previous lobby delta
    // and done something with it, which would be bad.)
    sendMessage(session, new MovePlayerToLobbyMessage());

    // Alert the client of the players currently in the lobby.
    sendMessage(session, new LobbyMessage(lobby.getPlayerList()));
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

    // Deny if requester is self or not in lobby (malicious).
    // TODO: Make it so you can't spam more than one DuelRequest to the same
    // requestee.
    if (requester.equals(requestee) || requester.getState() != State.IN_LOBBY) {
      return;
    }

    DuelRequest duelRequest = new DuelRequest(requester, requestee);

    logger.log(
        Level.INFO,
        String.format("%s requested a duel with %s.",
            requester.getInformationalName(),
            requestee.getInformationalName()));

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
   * Requests a player to make a preliminary action.
   * 
   * @param session
   */
  private static void queryPreliminaryAction(Session session) {
    Player player = getPlayer(session);
    logger.log(
        Level.INFO,
        String.format("Requesting player %s for a preliminary action.",
            player.getInformationalName()));
    player.setState(State.CHOOSING_PRELIMINARY_ACTION);
    sendMessage(session, new QueryPreliminaryActionMessage());
  }

  /**
   * Handles users' attempts to answer requests from other players to duel.
   * 
   * @param session
   * @param message
   */
  private void onAnswerDuelRequestMessage(Session session,
      AnswerDuelRequestMessage message) {

    // It is implied that the requestee's session still exists because he
    // intiated this message.

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
      lobby.removePlayer(requester);
      lobby.removePlayer(requestee);

      Room room = new Room();
      room.addPlayer(requester);
      room.addPlayer(requestee);
      room.initializeGame();

      rooms.put(room.getUuid(), room);

      logger.log(
          Level.INFO,
          String.format("Room `%s` created for a duel between %s and %s.",
              room.getUuid(),
              requestee.getInformationalName(),
              requester.getInformationalName()));

      // Alert both clients that they should move to the new room.
      sendMessage(requesterSession,
          new MovePlayerToRoomMessage(requester, room));
      sendMessage(session, new MovePlayerToRoomMessage(requestee, room));

      // Ask for the first preliminary action from the turn player.
      room.getNonTurnPlayer().setState(State.WAITING_FOR_OPPONENT);
      queryPreliminaryAction(sessions.get(room.getTurnPlayer().getUuid()));

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

  private static void queryDiscardAction(Session session) {
    Player player = getPlayer(session);
    logger.log(
        Level.INFO,
        String.format("Requesting player %s for a discard action.",
            player.getInformationalName()));
    player.setState(State.CHOOSING_DISCARD_ACTION);
    sendMessage(session, new QueryDiscardActionMessage());
  }

  private static void queryBattleActions(Session session) {
    Player player = getPlayer(session);
    logger.log(
        Level.INFO,
        String.format("Requesting player %s for battle actions.",
            player.getInformationalName()));
    player.setState(State.CHOOSING_BATTLE_ACTIONS);
    sendMessage(session, new QueryBattleActionsMessage());
  }

  private static void alertGameover(Player winner, Session... sessions) {
    GameoverMessage gameoverMessage = new GameoverMessage(winner);
    for (Session session : sessions) {
      sendMessage(session, gameoverMessage);
    }
    // TODO: Cleanup after the game.
  }

  /**
   * Handles player input on a "preliminary action" he is taking in-game. (Can
   * be drawing, pilfering, or summoning.)
   * 
   * @param session
   * @param message
   */
  private void onPreliminaryActionMessage(Session session,
      PreliminaryActionMessage message) {

    Player player = getPlayer(session);

    // Deny if the player is not making a preliminary action (malicious).
    if (!player.stateIs(State.CHOOSING_PRELIMINARY_ACTION)) {
      return;
    }

    // TODO: Make sure opponent is still connected. Probably as soon as he
    // leaves kill the room and send a gameover message to other player.

    String roomUuid = player.getRoomUuid();

    // Deny if player is not in a room or his room does not exist.
    if (!roomExists(roomUuid)) {
      return;
    }

    Room room = rooms.get(roomUuid);

    // Deny if the player is not the turn player (malicious).
    if (!player.equals(room.getTurnPlayer())) {
      return;
    }

    Player opponent = room.getNonTurnPlayer();
    Session opponentSession = sessions.get(opponent.getUuid());

    PreliminaryAction action = message.getAction();
    Game game = room.getGame();

    if (action instanceof CollectAction) {
      if (action.equals(CollectAction.DRAW)) {
        Card card = game.receiveCollectAction((CollectAction) action);
        sendMessage(session, new DrawMessage(card));
        sendMessage(opponentSession, new OpponentDrawMessage());
        // TODO: Check for a deckout.
      } else if (action.equals(CollectAction.PILFER)) {
        ActionTarget actionTarget = message.getTarget();
        int playerIndex =
            game.actionTargetToPlayerIndex(actionTarget, player);
        // Deny if the target discard pile doesn't have a card to pilfer
        // (malicious).
        if (game.getDiscardPile(playerIndex).isEmpty()) {
          return;
        }
        game.receiveCollectAction((CollectAction) action, playerIndex);
        sendMessage(opponentSession, new OpponentPilferMessage(actionTarget));
      }

      // Now that the turn player has collected, he must discard.
      queryDiscardAction(session);

    } else if (action instanceof SummonAction) {
      if (action.equals(SummonAction.SUMMON)) {
        // Deny if the player cannot summon (malicious).
        if (!game.canSummon()) {
          return;
        }
        game.receiveSummonAction();
        // Alert the summoner and the opponent what the summoner summoned.
        sendMessage(session, new SummonMessage(game.getDragons(player)));
        sendMessage(opponentSession,
            new OpponentSummonMessage(game.getDragons(player)));
        game.nextTurn();
        game.obligatorySummon();
        int winner = game.getWinner();
        if (winner >= 0) {
          alertGameover(game.getPlayerByIndex(winner), session, opponentSession);
        } else if (winner == -1) {
          // Alert both the player who obligatorily summoned and his opponent
          // of what was obligatorily summoned.
          sendMessage(opponentSession,
              new SummonMessage(game.getDragons(opponent)));
          sendMessage(session,
              new OpponentSummonMessage(game.getDragons(opponent)));
          game.beginBattle();
          queryBattleActions(session);
          queryBattleActions(opponentSession);
        }
      }
    }
  }

  private void onDiscardActionMessage(Session session,
      DiscardActionMessage message) {

    Player player = getPlayer(session);

    // Deny if the player is not making a discard action (malicious).
    if (!player.stateIs(State.CHOOSING_DISCARD_ACTION)) {
      return;
    }

    String roomUuid = player.getRoomUuid();

    // Deny if player is not in a room or his room does not exist.
    if (!roomExists(roomUuid)) {
      return;
    }

    Room room = rooms.get(roomUuid);

    // Deny if the player is not the turn player (malicious).
    if (!player.equals(room.getTurnPlayer())) {
      return;
    }

    Element element = message.getElement();
    int level = message.getLevel();
    Game game = room.getGame();

    Card card = game.receiveDiscardAction(element, level);

    Player opponent = room.getNonTurnPlayer();
    Session opponentSession = sessions.get(opponent.getUuid());

    // Alert the opponent of the card that the turn player dicarded.
    sendMessage(opponentSession, new OpponentDiscardMessage(card));

    // Advance to the next turn.
    player.setState(State.WAITING_FOR_OPPONENT);
    game.nextTurn();
    queryPreliminaryAction(opponentSession);
  }

  private void onBattleActionsMessage(Session session,
      BattleActionsMessage message) {

    Player player = getPlayer(session);

    // Deny if the player is not making discard actions (malicious).
    if (!player.stateIs(State.CHOOSING_BATTLE_ACTIONS)) {
      return;
    }

    player.setState(State.WAITING_FOR_OPPONENT);

    String roomUuid = player.getRoomUuid();

    // Deny if player is not in a room or his room does not exist.
    if (!roomExists(roomUuid)) {
      return;
    }

    Room room = rooms.get(roomUuid);
    Game game = room.getGame();

    // Deny if the player is not who he says he is (malicious).
    if (game.getPlayerIndex(player) != message.getActions().get(0).getPlayer()) {
      return;
    }

    Player opponent = room.getNonTurnPlayer();
    Session opponentSession = sessions.get(opponent.getUuid());

    game.addBattleActions(message.getActions());

    if (!game.isReadyToBattle()) {
      return;
    }

    List<BattleAction> actions = game.getBattleActions();

    // The first 2 actions were made by the current submitter's opponent.
    sendMessage(session,
        new OpponentBattleActionsMessage(actions.subList(0, 2)));

    // The rest of the actions, just made in this method invokation, were
    // made by the submitter and must be relayed to his opponent.
    sendMessage(opponentSession,
        new OpponentBattleActionsMessage(actions.subList(2, 4)));

    game.battle();

    int winner = game.getWinner();
    if (game.getWinner() >= 0) {
      alertGameover(game.getPlayerByIndex(winner), session, opponentSession);
    } else if (game.getWinner() == -2) {
      alertGameover(null, session, opponentSession);
    } else if (winner == -1) {
      queryBattleActions(session);
      queryBattleActions(opponentSession);
    }
  }

  private void onExitRoomMessage(Session session, ExitRoomMessage message) {
    // TODO: Implement this.
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
    } else if (message instanceof PreliminaryActionMessage) {
      this.onPreliminaryActionMessage(session,
          (PreliminaryActionMessage) message);
    } else if (message instanceof DiscardActionMessage) {
      this.onDiscardActionMessage(session,
          (DiscardActionMessage) message);
    } else if (message instanceof BattleActionsMessage) {
      this.onBattleActionsMessage(session, (BattleActionsMessage) message);
    } else if (message instanceof ExitRoomMessage) {
      this.onExitRoomMessage(session, (ExitRoomMessage) message);
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

    // Remove the player from these collections first so that messages aren't
    // erroneously sent to him.
    players.remove(player.getUuid());
    sessions.remove(player.getUuid());

    cleanUpDuelRequests(player);
    if (lobby.hasPlayer(player)) {
      lobby.removePlayer(player);
      List<Player> playersLeft = new ArrayList<>(1);
      playersLeft.add(player);
      alertUpdateLobby(new ArrayList<Player>(), playersLeft);
    }
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
