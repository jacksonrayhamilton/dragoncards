/*jslint browser: true */
/*global $, _, chance, moment*/

$(function() {

  var WEBSOCKET_URL = 'ws://' + location.host + '/index';
  var ws;

  var state;

  var State = {
    IN_LIMBO: 1,
    NAMING: 2,
    IN_LOBBY: 3,
    DUELING: 4,
    WAITING_FOR_OPPONENT: 5,
    CHOOSING_PRELIMINARY_ACTION: 6,
    CHOOSING_DISCARD_ACTION: 7,
    CHOOSING_BATTLE_ACTIONS: 8
  };

  var Element = {
    WOOD: 'WOOD',
    FIRE: 'FIRE',
    EARTH: 'EARTH',
    METAL: 'METAL',
    WATER: 'WATER'
  };

  var lobby = {
    players: []
  };

  var duelRequests = [];

  var room;
  var game;
  var yourHand;
  var opponentHand;
  var yourDiscardPile;
  var opponentDiscardPile;

  var lastMessage = $('#lastMessage');

  var setPlayerNameInput = $('#setPlayerNameInput');
  var setPlayerNameButton = $('#setPlayerNameButton');
  var drawButton = $('#drawButton');
  var pilferButton = $('#pilferButton');
  var summonButton = $('#summonButton');

  var lobbyList = $('#lobbyList');
  var duelRequestsList = $('#duelRequestsList');
  var yourHandList = $('#yourHandList');
  var yourDiscardPileList = $('#yourDiscardPileList');
  var opponentDiscardPileList = $('#opponentDiscardPileList');

  // TEMPLATES
  // ---------

  var cardTemplate = (function() {
    var template = _.template('<span class="element <%- element %>"><%- symbol %><%- level %></span>');
    var symbols = {
      WOOD: '木',
      FIRE: '火',
      EARTH: '地',
      METAL: '金',
      WATER: '水'
    };
    return function (card) {
      var data = {
        element: card.element.toLowerCase(),
        symbol: symbols[card.element],
        level: card.level
      };
      return template(data);
    };
  }());

  // MESSAGING
  // ---------

  function sendMessage(obj) {
    ws.send(JSON.stringify(obj));
  }

  function onOpen(e) {
    console.log('Connected to websocket at `' + WEBSOCKET_URL + '\'.');
  }

  function onClose(e) {
    console.log('Connection closed.');
  }

  function onError(e) {
    console.error('Connection error! See the logs for more info.');
  }

  /**
   * Handles incoming messages.
   */
  function onMessage(e) {
    var data = JSON.parse(e.data);
    console.log(data);

    lastMessage.empty();
    lastMessage.html(_.template('<b><%- time %>:</b> <%- message %>', {
      time: moment().format('hh:mm:ss A'),
      message: data.toClient
    }));
    lastMessage
      .stop()
      .css("background-color", "#FFFF9C")
      .animate({backgroundColor: "#FFFFFF"}, 1500);

    var type = data.toClient;
    if (type === 'queryPlayerName') {
      state = State.NAMING;
    } else if (type === 'movePlayerToLobby') {
      movePlayerToLobby();
    } else if (type === 'lobby') {
      if (state === State.IN_LOBBY) {
        populateLobby(data.players);
      }
    } else if (type === 'updateLobby') {
      if (state === State.IN_LOBBY) {
        updateLobby(data.playersJoined, data.playersLeft);
      }
    } else if (type === 'duelRequested') {
      addDuelRequest(data.duelRequest);
    } else if (type === 'movePlayerToRoom') {
      initializeGame(data);
    } else if (type === 'queryPreliminaryAction') {
      state = State.CHOOSING_PRELIMINARY_ACTION;
    } else if (type === 'queryDiscardAction') {
      state = State.CHOOSING_DISCARD_ACTION;
    } else if (type === 'queryBattleActions') {
      state = State.CHOOSING_BATTLE_ACTIONS;
    } else if (type === 'draw') {
      drawCard(data.card);
    } else if (type === 'opponentDraw') {
      // TODO: Implement.
    } else if (type === 'opponentPilfer') {
      if (data.target === 'SELF') {
        pilferCardToOpponentHand('opponent');
      } else if (data.target === 'OPPONENT') {
        pilferCardToOpponentHand('your');
      }
    } else if (type === 'opponentDiscard') {
      discardOpponentCard(data.card);
    }
  }

  /**
   * Establishes a websocket connection.
   */
  function connect() {
    ws = new WebSocket(WEBSOCKET_URL);
    ws.onopen = onOpen;
    ws.onclose = onClose;
    ws.onerror = onError;
    ws.onmessage = onMessage;
  }

  /**
   * Initializes the client.
   */
  function init() {
    state = State.IN_LIMBO;
    setPlayerNameInput.val(chance.name());
    connect();
  }

  // GAME
  // ----

  function movePlayerToLobby() {
    state = State.IN_LOBBY;
  }

  function sortLobby() {
    lobby.players = _.sortBy(lobby.players, 'name');
  }

  function populateLobby(players) {
    lobby.players = players;
    sortLobby();
    updateLobbyView();
  }

  function updateLobby(playersJoined, playersLeft) {
    lobby.players = lobby.players.concat(playersJoined);
    playersLeft.forEach(function (player) {
      _.remove(lobby.players, {uuid: player.uuid});
    });
    sortLobby();
    updateLobbyView();
  }

  function updateLobbyView() {
    lobbyList.empty();
    lobby.players.forEach(function (player) {
      var listItem = $(_.template('<li><a href="#"><%- name %></a></li>', player));
      listItem.on('click', function (e) {
        e.preventDefault();
        sendMessage({
          toServer: 'requestDuel',
          uuid: player.uuid
        });
      });
      lobbyList.append(listItem);
    });
  }

  function addDuelRequest(duelRequest) {
    duelRequests.push(duelRequest);
    updateDuelRequestsView();
  }

  function removeDuelRequest(duelRequest) {
    _.remove(duelRequests, {uuid: duelRequest.uuid});
    updateDuelRequestsView();
  }

  function updateDuelRequestsView() {
    duelRequestsList.empty();
    duelRequests.forEach(function (duelRequest) {
      var listItem = $(_.template('<li><a href="#">From: <%- name %></a></li>',
                                  duelRequest.requester));
      listItem.on('click', function (e) {
        e.preventDefault();
        sendMessage({
          toServer: 'answerDuelRequest',
          uuid: duelRequest.uuid,
          accept: true
        });
        removeDuelRequest(duelRequest);
      });
      duelRequestsList.append(listItem);
    });
  }

  function updateYourHandView() {
    yourHandList.empty();
    yourHand.forEach(function (card) {
      var listItem = $('<li><a href="#">' + cardTemplate(card) + '</a></li>');
      listItem.on('click', function (e) {
        e.preventDefault();
        if (state != State.CHOOSING_DISCARD_ACTION) {
          return;
        }
        sendMessage({
          toServer: 'discardAction',
          element: card.element,
          level: card.level
        });
        discardYourCard(card);
        state = State.WAITING_FOR_OPPONENT;
      });
      yourHandList.append(listItem);
    });
  }

  function updateDiscardPileView(whichPile) {
    var list;
    var discardPile;

    if (whichPile === 'your') {
      list = yourDiscardPileList;
      discardPile = yourDiscardPile;
    } else if (whichPile === 'opponent') {
      list = opponentDiscardPileList;
      discardPile = opponentDiscardPile;
    }

    list.empty();
    discardPile.forEach(function (card) {
      var listItem = $('<li>' + cardTemplate(card) + '</li>');
      list.append(listItem);
    });
  }

  function updateYourDiscardPileView() {
    updateDiscardPileView('your');
  }

  function updateOpponentDiscardPileView() {
    updateDiscardPileView('opponent');
  }

  function initializeGame(data) {
    game = data.game;
    room = data.room;
    yourHand = data.yourHand.cards;
    opponentHand = data.opponentHand.cards;
    yourDiscardPile = [];
    opponentDiscardPile = [];

    updateYourHandView();
    updateYourDiscardPileView();
    updateOpponentDiscardPileView();
  }

  function drawCard(card) {
    yourHand.push(card);
    updateYourHandView();
  }

  function discardYourCard(card) {
    var index = _.findIndex(yourHand, function (inHand) {
      return (inHand.element === card.element) &&
        (inHand.level === card.level);
    });
    var discardedCard = yourHand.splice(index, 1)[0];
    updateYourHandView();
    receiveDiscardedCard(discardedCard, 'your');
  }

  function discardOpponentCard(card) {
    receiveDiscardedCard(card, 'opponent');
  }

  function receiveDiscardedCard(card, whichPile) {
    var pile;
    if (whichPile === 'your') {
      pile = yourDiscardPile;
    } else if (whichPile === 'opponent') {
      pile = opponentDiscardPile;
    }
    pile.push(card);
    updateDiscardPileView(whichPile);
  }

  function pilferCardToYourHand(whichPile) {
    yourHand.push(pilferCard(whichPile));
    updateYourHandView();
  }

  function pilferCardToOpponentHand(whichPile) {
    pilferCard(whichPile);
  }

  function pilferCard(whichPile) {
    var list;
    var discardPile;

    if (whichPile === 'your') {
      discardPile = yourDiscardPile;
    } else if (whichPile === 'opponent') {
      discardPile = opponentDiscardPile;
    }

    var pilferedCard = discardPile.pop();
    updateDiscardPileView(whichPile);
    return pilferedCard;
  }

  /**
   * Determines the player has 2 sets (and therefore can summon).
   */
  function canSummon() {
    var sets = 0;
    var i;
    var length;
    var j;

    for (i = 0, length = yourHand.length; i < length; i++) {
      var card = yourHand[i];
      var count = 1;
      for (j = i + 1; j < length; j++) {
        var otherCard = yourHand[j];
        if (card.element === otherCard.element &&
            card.level === otherCard.level) {
          count++;
          if (count === 3) {
            sets++;
          }
        }
      }
    }

    return sets === 2;
  }

  // EVENT LISTENERS
  // ---------------

  setPlayerNameButton.on('click', function () {
    if (state != State.NAMING) {
      return;
    }
    sendMessage({
      toServer: 'setPlayerName',
      name: setPlayerNameInput.val()
    });
  });

  drawButton.on('click', function () {
    if (state != State.CHOOSING_PRELIMINARY_ACTION) {
      return;
    }
    sendMessage({
      toServer: 'preliminaryAction',
      action: 'draw'
    });
  });

  pilferButton.on('click', function () {
    if (state != State.CHOOSING_PRELIMINARY_ACTION) {
      return;
    }
    var target = $('input:radio[name="pilfer"]:checked').val();
    var whichPile;
    if (target === 'self') {
      if (yourDiscardPile.length === 0) {
        return;
      }
      whichPile = 'your';
    } else {
      if (opponentDiscardPile.length === 0) {
        return;
      }
      whichPile = 'opponent';
    }
    sendMessage({
      toServer: 'preliminaryAction',
      action: 'pilfer',
      target: target
    });
    pilferCardToYourHand(whichPile);
  });

  summonButton.on('click', function () {
    if (!canSummon()) {
      return;
    }
    sendMessage({
      toServer: 'preliminaryAction',
      action: 'summon'
    });
  });

  // INITIALIZATION
  // --------------

  init();

});
