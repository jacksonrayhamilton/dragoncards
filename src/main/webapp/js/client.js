var ws;

function connect() {
  ws = new WebSocket('ws://' + location.host + '/index');
  ws.onopen = onOpen;
  ws.onmessage = onMessage;
}

function onOpen(e) {
  console.log('Connected to websocket.')
}

function onMessage(e) {
  console.log(JSON.parse(e.data));
}

function sendMessage(obj) {
  ws.send(JSON.stringify(obj));
}

var setPlayerNameInput = document.getElementById('setPlayerNameInput');
var setPlayerNameButton = document.getElementById('setPlayerNameButton');

setPlayerNameButton.addEventListener('click', function () {
  sendMessage({
    toServer: 'setPlayerName',
    name: setPlayerNameInput.value
  });
}, false)

window.addEventListener('load', connect, false);