var ws;

function connect() {
  ws = new WebSocket('ws://' + location.host + '/index');
  ws.onopen = onOpen;
  ws.onmessage = onMessage;
}

function onOpen(e) {
  //ws.send('hey wazzup');
}

function onMessage(e) {
  console.log(e.message);
}

function sendMessage(obj) {
  ws.send(JSON.stringify(obj));
}

window.addEventListener("load", connect, false);