var webSocketServer = require('websocket').server;
var http = require('http');

// Global variables
var webSocketsServerPort = 1337;
var messages = [];
var clients = [];

// HTTP server
var server = http.createServer(function (request, response) {
});
server.listen(webSocketsServerPort, function () {
	console.log((new Date()) + " Server is listening on port "
		+ webSocketsServerPort);
});

// WebSocket server
var wsServer = new webSocketServer({
	// WebSocket server is tied to a HTTP server. WebSocket
	// request is just an enhanced HTTP request:
	// http://tools.ietf.org/html/rfc6455#page-6
	httpServer: server
});

// This callback function is called every time someone
// tries to connect to the WebSocket server
wsServer.on('request', function (request) {
	console.log((new Date()) + ' Connection from origin ' + request.origin
		+ '.');

	// later we maybe allow cross-origin requests
	var connection = request.accept(null, request.origin);

	// we need to know client index to remove them on 'close' event
	var index = clients.push(connection) - 1;

	console.log((new Date()) + ' Connection accepted.');

	// send something back
	// connection.sendUTF(JSON.stringify({
	//       foo : 'bar',
	//       data : 'blablub'
	// }));

	// user sent some message
	connection.on('message',
		function (message) {
			if (message.type === 'utf8') {
				var json = message.utf8Data;

				// log and broadcast the message
				console.log((new Date()) + ' Received Message '
					+ json);
				// broadcast message to all connected clients
				for (var i = 0; i < clients.length; i++) {
					clients[i].sendUTF(json);
				}
			}
		});

	// user disconnected
	connection.on('close', function (connection) {
		console.log((new Date()) + " Peer " + connection.remoteAddress
			+ " disconnected.");
		// remove user from the list of connected clients
		clients.splice(index, 1);
	});
});