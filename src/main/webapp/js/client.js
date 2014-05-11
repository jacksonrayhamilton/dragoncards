/*jslint browser: true */
/*global $, _, chance, moment*/

$(function() {

  // CONSTANTS
  // ---------

  var WEBSOCKET_URL = 'ws://' + location.host + '/index';

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
    WATER: 'WATER',
    getDominated: function (element) {
      switch (element) {
        case 'WOOD':
          return 'EARTH';
        case 'FIRE':
          return 'METAL';
        case 'EARTH':
          return 'WATER';
        case 'METAL':
          return 'WOOD';
        case 'WATER':
          return 'FIRE';
        default:
          return null;
      }
    },
    getWeakness: function (element) {
      switch (element) {
        case 'WOOD':
          return 'METAL';
        case 'FIRE':
          return 'WATER';
        case 'EARTH':
          return 'WOOD';
        case 'METAL':
          return 'FIRE';
        case 'WATER':
          return 'EARTH';
        default:
          return null;
      }
    }
  };

  var Symbols = {
    WOOD: '木',
    FIRE: '火',
    EARTH: '地',
    METAL: '金',
    WATER: '水'
  };

  // INSTANCE VARIABLES
  // ------------------

  var ws = null;

  var player = null;
  var playerIndex = 0;
  var state = State.IN_LIMBO;

  var lobby = {
    players: []
  };

  var duelRequests = [];
  var pendingDuelRequests = [];

  var room = null;
  var game = null;
  var yourHand = null;
  var opponentHand = null;
  var yourDiscardPile = null;
  var opponentDiscardPile = null;

  var yourDragons = null;
  var opponentDragons = null;
  var yourBattleActions = null;
  var opponentBattleActions = null;
  var lastOutcome = '';

  // DOM
  // ---

  var lastMessage = $('#lastMessage');
  var lastAlert = $('#lastAlert');

  var setPlayerNameInput = $('#setPlayerNameInput');
  var setPlayerNameButton = $('#setPlayerNameButton');
  var deckSpan = $('#deckSpan');
  var drawButton = $('#drawButton');
  var pilferButton = $('#pilferButton');
  var summonButton = $('#summonButton');

  var lobbyList = $('#lobbyList');
  var duelRequestsList = $('#duelRequestsList');
  var yourHandList = $('#yourHandList');
  var yourDiscardPileList = $('#yourDiscardPileList');
  var opponentDiscardPileList = $('#opponentDiscardPileList');

  var yourDragonsList = $('#yourDragonsList');
  var opponentDragonsList = $('#opponentDragonsList');
  var battleButton = $('#battleButton');
  var outcomeList = $('#outcomeList');

  // TEMPLATES
  // ---------

  var cardTemplate = (function() {
    var template = _.template('<span class="element <%- element %>"><%- symbol %><%- level %></span>');
    return function (card) {
      var data = {
        element: card.element.toLowerCase(),
        symbol: Symbols[card.element],
        level: card.level
      };
      return template(data);
    };
  }());

  var dragonTemplate = (function () {
    var template = _.template(
      '<span class="element <%- element %>">'
        + '<%- symbol %><%- level %>'
        + '</span><br>'
        + '<b><%- life %></b>/<b><%- maxLife %></b><br>'
        + 'Power: <b><%- power %></b><br>'
        + 'Boost: <b><%- boost %></b>');
    return function (dragon) {
      return template({
        element: dragon.element.toLowerCase(),
        symbol: Symbols[dragon.element],
        level: dragon.level,
        life: dragon.life.toFixed(2),
        maxLife: dragon.maxLife.toFixed(2),
        power: dragon.power.toFixed(2),
        boost: dragon.boost.toFixed(2)
      });
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

  function setMessage(message) {
    lastMessage.empty();
    lastMessage.html(_.template('<b><%- time %>:</b> <%- message %>', {
      time: moment().format('hh:mm:ss A'),
      message: message
    }));
    lastMessage
      .stop()
      .css("background-color", "#FFFF9C")
      .animate({backgroundColor: "#FFFFFF"}, 1500);
  }

  function setAlert(message) {
    lastAlert.empty();
    lastAlert.html(_.template('<b><%- time %>:</b> <%- message %>', {
      time: moment().format('hh:mm:ss A'),
      message: message
    }));
    lastAlert
      .stop()
      .css("background-color", "#FF9C9C")
      .animate({backgroundColor: "#FFFFFF"}, 1500);
  }

  /**
   * Handles incoming messages.
   */
  function onMessage(e) {
    var data = JSON.parse(e.data);
    console.log(data);

    setMessage(data.toClient);

    var type = data.toClient;
    if (type === 'queryPlayerName') {
      state = State.NAMING;
    } else if (type === 'createPlayer') {
      createPlayer(data);
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
    } else if (type === 'duelRequestAnswered') {
      resolvePendingDuelRequest(data.duelRequest);
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
      opponentDrawCard();
    } else if (type === 'opponentPilfer') {
      if (data.target === 'SELF') {
        pilferCardToOpponentHand('opponent');
      } else if (data.target === 'OPPONENT') {
        pilferCardToOpponentHand('your');
      }
    } else if (type === 'opponentDiscard') {
      discardOpponentCard(data.card);
    } else if (type === 'summon') {
      addYourDragons(data.dragons);
    } else if (type === 'opponentSummon') {
      addOpponentDragons(data.dragons);
    } else if (type === 'queryBattleActions') {
      state = State.CHOOSING_BATTLE_ACTIONS;
    } else if (type === 'opponentBattleActions') {
      addOpponentBattleActions(data.actions);
    } else if (type === 'gameover') {
      gameover(data);
    } else if (type === 'opponentDisconnect') {
      opponentDisconnect();
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

  function createPlayer(data) {
    player = {
      name: data.name,
      uuid: data.uuid
    };
  }

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
      var listItem = $(_.template(
        '<li><a href="#"><%- name %></a></li>', player));
      listItem.on('click', function (e) {
        e.preventDefault();
        if (_.find(pendingDuelRequests, {uuid: player.uuid})) {
          return;
        }
        sendMessage({
          toServer: 'requestDuel',
          uuid: player.uuid
        });
        pendingDuelRequests.push(player);
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

  function removePendingDuelRequest(duelRequest) {
    _.remove(pendingDuelRequests, {uuid: duelRequest.requestee.uuid});
  }

  function resolvePendingDuelRequest(duelRequest) {
    removePendingDuelRequest(duelRequest);
    if (duelRequest.accept) {
      // TODO: Alert accepted.
    } else {
      // TODO: Alert rejected.
    }
  }

  function updateDuelRequestsView() {
    duelRequestsList.empty();
    duelRequests.forEach(function (duelRequest) {
      var listItem = $(_.template(
        '<li><a href="#">From: <%- name %></a></li>',
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

  function updateDeckView() {
    deckSpan.empty();
    deckSpan.text(game.deck.size);
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

    updateDeckView();
    updateYourHandView();
    updateYourDiscardPileView();
    updateOpponentDiscardPileView();

    // NTS: NEVER use indexes. EVER again.
    playerIndex = _.findIndex(game.players, {uuid: player.uuid});
  }

  function drawCard(card) {
    yourHand.push(card);
    game.deck.size--;
    updateYourHandView();
    updateDeckView();
  }

  function opponentDrawCard() {
    game.deck.size--;
    updateDeckView();
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
   * Determines if the player has 2 sets (and therefore can summon).
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

  function addYourDragons(dragons) {
    yourDragons = dragons;
    updateDragonsView('your');
  }

  function addOpponentDragons(dragons) {
    opponentDragons = dragons;
    updateDragonsView('opponent');
  }

  function updateDragonsView(whose) {
    var dragons;
    var list;
    if (whose === 'your') {
      dragons = yourDragons;
      list = yourDragonsList;
    } else if (whose === 'opponent') {
      dragons = opponentDragons;
      list = opponentDragonsList;
    }

    list.empty();
    dragons.forEach(function (dragon) {
      var listItem = $('<li>' + dragonTemplate(dragon) + '</li>');
      list.append(listItem);
    });
  }

  function updateDragonsViews() {
    ['your', 'opponent'].forEach(function (whose) {
      updateDragonsView(whose);
    });
  }

  function addYourBattleActions(actions) {
    yourBattleActions = actions;
  }

  function addOpponentBattleActions(actions) {
    opponentBattleActions = actions;
    battle();
  }

  function getDragons(whose) {
    var dragons;
    if (whose === 'your') {
      dragons = yourDragons;
    } else if (whose === 'opponent') {
      dragons = opponentDragons;
    }
    return dragons;
  }

  function switchDragons(whose) {
    var dragons = getDragons(whose);
    dragons.unshift(dragons.pop());
  }

  function getDamageMultiplier(attackingElement, defendingElement) {
    var multiplier;
    if (Element.getDominated(attackingElement) === defendingElement) {
      multiplier = 2;
    } else if (Element.getWeakness(attackingElement) === defendingElement) {
      multiplier = 0.5;
    } else {
      multiplier = 1;
    }
    return multiplier;
  }

  function attackWithDragon(whose, attackerIndex, targetIndex) {
    var attacker = getDragons(whose)[attackerIndex];
    var other = whose === 'your' ? 'opponent' : 'your';
    var target = getDragons(other)[targetIndex];
    var damage;
    var multiplier;
    if (target.countering) {
      damage = target.power * target.boost;
      multiplier = getDamageMultiplier(target.element, attacker.element);
      attacker.life -= damage * multiplier;
    } else {
      damage = attacker.power * attacker.boost;
      multiplier = getDamageMultiplier(attacker.element, target.element);
      target.life -= damage * multiplier;
    }
  }

  function counterWithDragon(whose, index) {
    var dragons = getDragons(whose);
    dragons[index].countering = true;
  }

  function battleCleanup() {
    yourDragons.concat(opponentDragons).forEach(function (dragon) {
      dragon.countering = false;
    });
    yourBattleActions = null;
    opponentBattleActions = null;
  }

  function getWhose(index) {
    var whose;
    if (index === playerIndex) {
      whose = 'your';
    } else {
      whose = 'opponent';
    }
    return whose;
  }

  function getOtherWhose(whose) {
    return whose === 'your' ? 'opponent' : 'your';
  }

  function toTitleCase(str) {
    return str.replace(/\w\S*/g, function (txt) {
      return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
    });
  }

  function getOutcome(action) {
    var whose = getWhose(action.player);
    var dragons = getDragons(whose);
    var initiator = dragons[action.initiator];

    if (action.type === 'ATTACK') {
      var other = getOtherWhose(whose);
      var otherDragons = getDragons(other);
      var target = otherDragons[action.target];
    }

    if (action.type === 'ATTACK') {
      return toTitleCase(whose) + ' ' + cardTemplate(initiator) + ' attacks ' + cardTemplate(target) + '.';
    } else if (action.type === 'SWITCH') {
      return toTitleCase(whose) + ' ' + cardTemplate(initiator) + ' switches places with its partner.';
    } else if (action.type === 'COUNTER') {
      return toTitleCase(whose) + ' ' + cardTemplate(initiator) + ' prepares to counter incoming blows.';
    }
    return '';
  }

  function battle() {
    var battleActions = [];
    battleActions = battleActions.concat(yourBattleActions);
    battleActions = battleActions.concat(opponentBattleActions);

    // TODO: Figure out this null problem.
    console.log(battleActions, yourBattleActions, opponentBattleActions);

    var sortedActions = [];
    battleActions.forEach(function (action) {
      if (action.type === 'SWITCH' ||
          action.type === 'COUNTER') {
        sortedActions.unshift(action);
      } else {
        sortedActions.push(action);
      }
    });

    lastOutcome = '';
    sortedActions.forEach(function (action) {
      var whose;
      if (action.player === playerIndex) {
        whose = 'your';
      } else {
        whose = 'opponent';
      }
      if (action.type === 'ATTACK') {
        attackWithDragon(whose, action.initiator, action.target);
      } else if (action.type === 'SWITCH') {
        switchDragons(whose);
      } else if (action.type === 'COUNTER') {
        counterWithDragon(whose, action.initiator);
      }
      lastOutcome += '<li>' + getOutcome(action) + '</li><br>';
    });

    updateDragonsViews();
    updateOutcomeView();

    battleCleanup();
  }

  function updateOutcomeView() {
    outcomeList.empty();
    outcomeList.html(lastOutcome);
  }

  function gameover(data) {
    if (data.winner) {
      setAlert('Game over! The winner is ' + data.winner.name + '!');
    } else if (data.draw) {
      setAlert('A draw!');
    }
  }

  function opponentDisconnect() {
    setAlert('Opponent disconnected.');
    state = State.IN_LOBBY;
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

  battleButton.on('click', function () {
    if (state != State.CHOOSING_BATTLE_ACTIONS) {
      return;
    }
    var actions = [
      {
        type: $('input:radio[name="dragon0Type"]:checked').val()
          .toUpperCase(),
        player: playerIndex,
        initiator: 0,
        target: parseInt($('input:radio[name="dragon0Target"]:checked').val())
      },
      {
        type: $('input:radio[name="dragon1Type"]:checked').val()
          .toUpperCase(),
        player: playerIndex,
        initiator: 1,
        target: parseInt($('input:radio[name="dragon1Target"]:checked').val())
      }
    ];
    addYourBattleActions(actions);
    sendMessage({
      toServer: 'battleActions',
      actions: actions
    });
    state = State.WAITING_FOR_OPPONENT;
  });

  // INITIALIZATION
  // --------------

  init();

});
