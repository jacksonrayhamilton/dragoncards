var wsocket;

function connect() {
  wsocket = new WebSocket('ws://' + location.host + '/echo');
  wsocket.onopen = onOpen;
  wsocket.onmessage = onMessage;
}

function onOpen(e) {
  wsocket.send('hey wazzup');
  belittleWithMessages();
}

function belittleWithMessages() {
  setInterval(function () {
    wsocket.send('heyoo ' + new Date().toISOString());
  }, 1000);
}

function onMessage(e) {
  console.log(e);
}

window.addEventListener("load", connect, false);