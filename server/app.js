var path = require('path'),
	express = require('express'),
	app = express(),
	http = require('http').Server(app),
	colors = require('colors'),
	port = process.env.PORT || 8888,
	io = require('socket.io')(http),
	fs = require('fs-extra');



// Routing
app.use( express.static(__dirname + '/public') );
app.use( bodyParser.json() );
app.use( bodyParser.urlencoded() );
app.set('views', __dirname + '/public');
app.engine('html', require('ejs').renderFile);

app.get('/', function(req, res) {
	
});


// Socket Stuff
io.on('connection', function(socket) {
	socket.on('???', function(user) {

	});
});

// function notifyConnectedClients(masterSocketID, filePath) {
// 	connectedUser[masterSocketID].forEach(function(item) {
// 		io.sockets.connected[item].emit('successfullyUploadedImage', filePath);
// 	});
// }


// HTTP Server
http.listen(8888, function() {
	console.log('listen on port ' + '8888'.rainbow);
});