# Research Report
## Walking Skeleton
### Summary of Work
#### Demo Server
I wrote a websocket demonstration client and server and uploaded the results to:
https://git.doit.wisc.edu/cdis/cs/courses/cs506/sp2026/team/t_14/research-websocket

The demonstration server uses Java’s built-in java.net.ServerSocket class.

The following line launches the server on port 8000 of the machine it’s running on:
ServerSocket server = new ServerSocket(8000);

Then, the server waits for a client to connect with the following line:
Socket clientSocket = server.accept();
Note that this is a blocking statement. Execution will not continue until a client has connected.

The server is able to communicate bidirectionally with the client through two data streams: one for input and one for output.
InputStream in = clientSocket.getInputStream();
OutputStream out = clientSocket.getOutputStream();

To communicate with multiple clients, concurrency is required and is not implemented here. This is due to the blocking behavior of server.accept().  

#### Handshake and Demo Client
Websocket connections are initiated via handshake in the form of an HTTP GET request from the client to the server. The required headers for this request are documented online and look like the following:

| key                   | value        |
|-----------------------|--------------|
| ec-WebSocket-Key      | [YOUR KEY]== |
| Sec-WebSocket-Version | 13           |
| Upgrade               | websocket    |


The JavaScript Websocket class will generate and send these headers for you when you attempt to connect with:
const socket = new WebSocket("ws://[SERVER IP]:8000");
Please note that the demo client is not pointing to localhost by default.

The server must respond to this request with the following, or the client will assume the connection was refused:

HTTP/1.1 101 Switching Protocols

| key                   | value      |
|-----------------------|------------|
| Connection:           | Upgrade    |
| Upgrade:              | websocket  |
| Sec-WebSocket-Accept: | [YOUR KEY] |

If everything is working correctly, you’ll see this code 101 response, which is the code for a protocol switch, in your browser’s network monitoring tools.

### Motivation
Our team wanted a better understanding of Websocket so we could properly gauge whether our project would require it.

### Time Spent
2 hours

### Results
After conducting this investigation, my opinion is that our REST API will suffice, but we are still willing to consider 
use of websockets if we find an appropriate use case.

### Sources
https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java
https://developer.mozilla.org/en-US/docs/Web/API/WebSocket
