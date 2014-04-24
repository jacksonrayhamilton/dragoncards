var ws;
var otherWs;

function connect() {
  ws = new WebSocket('ws://' + location.host + '/index');
  ws.onopen = onOpen;
  ws.onmessage = onMessage;
}

window.addEventListener('load', connect, false);

function onOpen(e) {
  console.log('Connected to websocket.');
}

function onMessage(e) {
  var data = JSON.parse(e.data);
  console.log(data);
}

function sendMessage(obj) {
  ws.send(JSON.stringify(obj));
}

var toServerSelect = document.getElementById('toServerSelect');
var jsonInput = document.getElementById('jsonInput');
var submitButton = document.getElementById('submitButton');

toServerSelect.addEventListener('change', function (e) {
  var selectedValue = this.options[this.selectedIndex].value;
  switch (selectedValue) {
    case 'setPlayerName':
      jsonInput.value = JSON.stringify({
        name: 'a'
      });
      break;
    case "requestDuel":
      jsonInput.value = JSON.stringify({
        uuid: 'abcd'
      });
      break;
    case "answerDuelRequest":
      jsonInput.value = JSON.stringify({
        uuid: 'abcd',
        accept: true
      });
      break;
    default:
      break;
  }
}, false);

submitButton.addEventListener('click', function () {
  var message = {
    toServer: toServerSelect.options[toServerSelect.selectedIndex].value
  };

  try {
    var json = JSON.parse(jsonInput.value);
  } catch (e) {
    console.error('JSON parsing error.');
    return;
  }

  for (var prop in json) {
    if (json.hasOwnProperty(prop)) {
      message[prop] = json[prop];
    }
  }

  sendMessage(message);
}, false);
