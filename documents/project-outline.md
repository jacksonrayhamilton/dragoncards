Dragon Cards!

Overview
--------
75 card shared deck
2 players
2 discard piles
6 cards to start
6 card hand limit
winner is the last person with a living dragon

Rules
-----
- At beginning of turn, either draw from deck or take top card off either
  discard pile.
- Afterwards, discard so that you have 6 cards again.
- Once you have 3 copies of 2 types of dragon, you may reveal them all and
  summon the appropriate dragons.
- The player without dragons "solitaires" with his cards revealed until
  he too has 2 sets of dragons. Immediately afterwards, his dragons are
  played. The dragons now battle.

Battling
--------
- Dragons have a level which affects their power. Attributes can make up the
  difference though.
- Damage is attributes-based.
  - The following attributes inflict increased damage on their adversaries:
    - Wood on Earth
    - Fire on Metal
    - Earth on Water
    - Metal on Wood
    - Water on Fire
  - Everything else is otherwise normal.
  - The following provide a boost to their partner:
    - Wood boosts Fire
    - Fire boosts Earth
    - Earth boosts Metal
    - Metal boosts Water
    - Water boosts Wood
- Players decide on their attacks in secret. The service confirms when both are
  ready and then performs combat.
- Dragons have life. When their life is reduced to 0, the dragon dies. Once both
  dragons die that player loses.
- You can either attack, switch or counter with each dragon.
- Attacking
  - If being countered by attack target, inflict damage to self.
  - Else inflict damage to attack target.
- Switch
  - Indices of both of a player's dragons are swapped to affect incoming attacks.
  - Has priority.
- Counter
  - Sets the counterer into a countering state in which an attack will be negated
  and damage will be inflicted onto the attacker.
  - Has priority.



Project Organization
--------------------
Client-side:
- JavaScript Canvas.
  - Resizes for mobile.
  - Match-making view.
    - Automatically pairs you.
  - Card drawing view.
    - Opponent hidden, you visible, until one player turns in his sets.
    - Deck count given.
    - Discard piles visible.
  - Battle view.
    - All dragons visible.
    - Attack / Switch / Counter options.
    - Attack shows "cutscene" of dragons.
- JSON messaging.
  - Initial match info, game and opponent id delivered via JSON.
  - Game updates for different states delivered to server and then to other
    player. Once both players have been confirmed to have made moves, a gamestate
    update is pushed to both.

Server-side:
- Websocket API to keep persistent connection with clients.

Index
- main websocket class
- handles new connections, player creation, delegation to lobby

Lobby
- singleton
- list of players seeking match
- handles match-making when there are multiple people seeking.
- delegates to Rooms.

Room
- new instance created for each match that is made
- has contains 2 players
- starts up a game between the 2 players
- could potentially do matches or have spectators

Game
- Manages gamestate, does heavy lifting.
- Handles messages between the two users and sends JSON to clients.
- id:
- players: 2 Player
  - Hands (consider ownership here)
  - Discard Pile
  - Dragons
- Deck
  - Cards
- Attack queue
- Message queue

Deck
- Contains all Cards shuffled
- Pops-off top cards for drawing (stack)

Discard pile
- LIFO stack

Hands
- Contains 6 cards
- Receives input on which to discard, returns discarded to be sent to
  the appropriate discard pile.

Dragon
- attribute
- level
- life
- boost

Player
- id: unique id
- name: user-specified
- state: MATCH_MAKING, DUELING
- roomId: null or some game id

Course of Action
----------------
- Define JSON API. DONE

- Implement headless game with testing. DONE
  - Expect certain inputs from the JSON API. DONE

- Implement lobby and matchmaking server-side. Test. Mostly-done

- Implement client side gamestate to mimmick server-side. Test.
- Implement client-to-server messaging to get gamestate info. Async test?

- Do decoding checking.

- Accumulate graphical assets.
- Implement client side canvas display.
- Cross-browser testing.

JSON API
--------

See JSON-API.json.


Sprites
-------

Water Dragon: brute.png
Metal Dragon: metal-circles.png
Wood Dragon: tree.png
Fire Dragon: awesome-dragons.png, awesomest light one
Earth Dragon: wilder.png
