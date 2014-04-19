var wsocket;

function connect() {
  wsocket = new WebSocket("ws://localhost:8080/dragoncards/echo");
  wsocket.onopen = onOpen;
  wsocket.onmessage = onMessage;
}

function onOpen(e) {
  wsocket.send('hey wazzup');
  belittleWithMessages();
}

function belittleWithMessages() {
  setTimeout(function () {
    wsocket.send('heyoo ' + Math.random());
  }, 1000);
}

function onMessage(e) {
  console.log(e);
}

window.addEventListener("load", connect, false);