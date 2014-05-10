/*jslint browser: true */
/*global $, _, chance, moment*/

(function() {

  var ws;

  var game;
  var room;
  var yourHand;
  var opponentHand;
  var yourDiscardPile;
  var opponentDiscardPile;

  var toServerSelect = $('#toServerSelect');
  var jsonInput = $('#jsonInput');
  var submitButton = $('#submitButton');

  var lastMessage = $('#lastMessage');

  var setPlayerNameInput = $('#setPlayerNameInput');
  var setPlayerNameButton = $('#setPlayerNameButton');
  var drawButton = $('#drawButton');
  var pilferButton = $('#pilferButton');

  var lobbyList = $('#lobbyList');
  var duelRequestsList = $('#duelRequestsList');
  var yourHandList = $('#yourHandList');
  var yourDiscardPileList = $('#yourDiscardPileList');
  var opponentDiscardPileList = $('#opponentDiscardPileList');

  /**
   * Establishes a websocket connection.
   */
  function connect() {
    ws = new WebSocket('ws://' + location.host + '/index');
    ws.onopen = onOpen;
    ws.onmessage = onMessage;
  }

  function onReady() {
    connect();
    setPlayerNameInput.val(chance.name());
  }

  $(document).ready(onReady);

  function onOpen(e) {
    console.log('Connected to websocket.');
  }

  function populateLobby(players) {
    lobbyList.empty();
    players.forEach(function (player) {
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
    var listItem = $(_.template('<li><a href="#">From: <%- name %></a></li>', duelRequest.requester));
    listItem.on('click', function (e) {
      e.preventDefault();
      sendMessage({
        toServer: 'answerDuelRequest',
        uuid: duelRequest.uuid,
        accept: true
      });
      listItem.remove();
    });
    duelRequestsList.append(listItem);
  }

  function updateYourHand() {
    yourHandList.empty();
    yourHand.forEach(function (card) {
      var listItem = $(_.template('<li><a href="#">Element: <%- element %><br>Level: <%- level %></a></li>', card));
      listItem.on('click', function (e) {
        e.preventDefault();
        sendMessage({
          toServer: 'discardAction',
          element: card.element,
          level: card.level
        });
        discardCard(card);
      });
      yourHandList.append(listItem);
    });
  }

  function updateADiscardPile(whose) {
    var list;
    var discardPile;

    if (whose === 'your') {
      list = yourDiscardPileList;
      discardPile = yourDiscardPile;
    } else if (whose === 'opponent') {
      list = opponentDiscardPileList;
      discardPile = opponentDiscardPile;
    }

    list.empty();
    discardPile.forEach(function (card) {
      var listItem = $(_.template('<li>Element: <%- element %><br>Level: <%- level %></li>', card));
      list.append(listItem);
    });
  }

  function updateYourDiscardPile() {
    updateADiscardPile('your');
  }

  function updateOpponentDiscardPile() {
    updateADiscardPile('opponent');
  }

  function initializeGame(data) {
    game = data.game;
    room = data.room;
    yourHand = data.yourHand.cards;
    opponentHand = data.opponentHand.cards;
    yourDiscardPile = [];
    opponentDiscardPile = [];

    updateYourHand();
    updateYourDiscardPile();
    updateOpponentDiscardPile();
  }

  function drawCard(card) {
    yourHand.push(card);
    updateYourHand();
  }

  function discardCard(card) {
    var index = _.findIndex(yourHand, function (inHand) {
      return (inHand.element === card.element) &&
        (inHand.level === card.level);
    });
    var discardedCards = yourHand.splice(index, 1);
    yourDiscardPile = yourDiscardPile.concat(discardedCards);
    updateYourHand();
    updateYourDiscardPile();
  }

  function pilferCard(whose) {
    var list;
    var discardPile;

    if (whose === 'your') {
      discardPile = yourDiscardPile;
    } else if (whose === 'opponent') {
      discardPile = opponentDiscardPile;
    }

    yourHand.push(discardPile.pop());
    updateYourHand();
    updateADiscardPile(whose);
  }

  function onMessage(e) {
    var data = JSON.parse(e.data);
    console.log(data);

    lastMessage.empty();
    lastMessage.html(_.template('<b><%- time %>:</b> <%- message %>', {
      time: moment().format('hh:mm:ss A'),
      message: data.toClient
    }));

    var type = data.toClient;
    if (type === 'movePlayerToLobby') {
      populateLobby(data.players);
    } else if (type === 'duelRequested') {
      addDuelRequest(data.duelRequest);
    } else if (type === 'movePlayerToRoom') {
      initializeGame(data);
    } else if (type === 'draw') {
      drawCard(data.card);
    } else if (type === 'opponentDraw') {
      // TODO: Implement.
    } else if (type === 'opponentPilfer') {
      // TODO: Implement along with the rest of the opponent's generalizations.
    }
  }

  function sendMessage(obj) {
    ws.send(JSON.stringify(obj));
  }

  setPlayerNameButton.on('click', function () {
    sendMessage({
      toServer: 'setPlayerName',
      name: setPlayerNameInput.val()
    });
  });

  drawButton.on('click', function () {
    sendMessage({
      toServer: 'preliminaryAction',
      action: 'draw'
    });
  });

  pilferButton.on('click', function () {
    var target = $('input:radio[name="pilfer"]:checked').val();
    sendMessage({
      toServer: 'preliminaryAction',
      action: 'pilfer',
      target: target
    });
    if (target === 'self') {
      pilferCard('your');
    } else {
      pilferCard('opponent');
    }
  });

  submitButton.on('click', function () {
    try {
      var json = JSON.parse(jsonInput.val());
    } catch (e) {
      console.error('JSON parsing error.');
      return;
    }

    var message = _.assign({
      toServer: toServerSelect.val()
    }, json);

    sendMessage(message);
  });

  toServerSelect.on('change', function (e) {
    var selectedValue = toServerSelect.val();
    switch (selectedValue) {
      case 'setPlayerName':
        jsonInput.val(JSON.stringify({
          name: 'a'
        }));
        break;
      case 'requestDuel':
        jsonInput.val(JSON.stringify({
          uuid: 'abcd'
        }));
        break;
      case 'answerDuelRequest':
        jsonInput.val(JSON.stringify({
          uuid: 'abcd',
          accept: true
        }));
        break;
      case 'preliminaryAction':
        jsonInput.val(JSON.stringify({
          action: 'draw',
          target: 'self'
        }));
        break;
      case 'discardAction':
        jsonInput.val(JSON.stringify({
          element: 'fire',
          level: 1
        }));
        break;
      case 'battleActions':
        jsonInput.val(JSON.stringify({
          actions: [
            {},
            {}
          ]
        }));
        break;
      case 'exitRoom':
        jsonInput.value(JSON.stringify({}));
        break;
      default:
        break;
    }
  });


}());
