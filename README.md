# Collaborative Whiteboard (Java RMI)

A shared whiteboard for several users over a network. One user hosts. Others
ask to join and the host approves them. Everyone then draws on the same
canvas live, chats, and sees where the others' cursors are.

*Built for the Distributed Systems subject at the University of Melbourne
(2025), and improved since.*

## Features

- Free draw, lines, rectangles, circles, triangles, text and eraser,
  with colour and stroke-size controls
- Host approves or rejects join requests; usernames must be unique; the host
  can remove users
- Chat, with history sent to users when they join
- Live cursor bubbles showing where each user is working
- Host file menu: New, Open, Save, Save As and Close, with a prompt to save
  unsaved changes
- Clients are told when the host closes the board, and lost clients are
  removed without disturbing the others

## Architecture

```
  JoinWhiteBoard (client)              CreateWhiteBoard (host)
 ┌───────────────────────┐   RMI    ┌──────────────────────────────────────┐
 │ ClientGUI (Swing)     │ ───────▶ │ WhiteBoardServer                     │
 │ WhiteBoardClient      │  draw,   │  · authoritative canvas (bitmap)     │
 │  (exported callback)  │  chat,   │  · users, pending joins, chat log    │
 │                       │  cursor  │  · one outbound queue per client ────┼──▶ other clients
 │                       │ ◀─────── │ ServerGUI (Swing, host's own board)  │
 └───────────────────────┘ callbacks└──────────────────────────────────────┘
```

- **Client → server:** each user action in a GUI becomes a small serialisable
  message (`DrawAction`, `ChatMessage`, `CursorPosition`) sent by an RMI call.
- **Server → clients:** the server applies the action to its canvas and
  forwards it to every client through an exported callback interface. A
  client that joins late gets a PNG snapshot of the canvas and the chat
  history.
- The host and client GUIs share a base class (`AbstractWhiteBoardGUI`); the
  remote interfaces live in `shared/`.

## Design decisions

**Java RMI rather than raw sockets.** RMI turns the protocol into typed Java
interfaces and handles serialisation and threading, so the work goes into
behaviour instead of message framing. The costs are that it is Java-only,
uses a dynamic port range that firewalls dislike, and has no built-in
authentication. For a LAN tool that is acceptable. A WebSocket server would
be the choice for browser clients or the internet.

**One authoritative server rather than peer-to-peer.** A single owner of the
canvas gives every user the same order of operations without a consensus
protocol, and fits the host-approves-joins model. The trade-off is that the
host is a single point of failure: when it leaves, the session ends, and
clients are told so cleanly.

**The canvas is a bitmap, not a history of operations.** Each action is
painted straight onto the server's image, so joining costs one snapshot no
matter how long the session has run, and saving is a PNG. The price is no
undo, no selection and no per-object editing. An operation log (or a CRDT, if
the central server were removed) would be needed for those.

**Concurrency.** RMI runs each incoming call on its own thread, so concurrent
edits from many clients are normal. The server lock guards only shared state
(users, chat history) and the order in which updates are queued. It is never
held during a network call. Every client has its **own single-threaded
outbound queue**:

- all clients receive updates in the same order as the server applied them;
- a slow or unreachable client delays only itself, not the broadcast, the
  other clients or the host's GUI;
- a client whose queue fails is removed and everyone else gets a new user list.

The first version used `synchronized` server methods that called every client
in turn while holding the lock. That is correct, but one slow client stalled
everybody, and because the host's own drawing ran on the Swing event thread,
the host's window froze during network delays. The per-client queues remove
both problems at the cost of one thread per client, which is fine for the
tens of users a whiteboard has. Swing state is only touched on the event
dispatch thread (`SwingUtilities.invokeLater`).

`WhiteBoardServerTest` checks these properties with in-process fake clients:
broadcasting 50 actions returns at once even with a client that takes 100 ms
per call, every client receives the actions in order, and a dead client is
dropped while the others carry on.

## Build and run

Requires JDK 21+ and Maven.

```sh
mvn clean package            # builds both JARs and runs the tests

java -jar target/CreateWhiteBoard.jar <port> <username>          # host
java -jar target/JoinWhiteBoard.jar <serverAddress> <port>       # each client
```

## Possible extensions

- Operation log with undo/redo and object selection
- Replicated or hand-over hosting so the session survives the host leaving
- A WebSocket transport with authentication for browser clients

## Icon credits

Toolbar and UI icons are from [Icons8](https://icons8.com/icons), used under
their free licence.
