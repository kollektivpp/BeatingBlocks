var path = require('path'),
	express = require('express'),
	app = express(),
	http = require('http').Server(app),
	colors = require('colors'),
	port = process.env.PORT || 8888,
	io = require('socket.io')(http),
	bodyParser = require('body-parser'),
	fs = require('fs-extra'),
	renderClients = [];



// Routing
app.use( express.static(__dirname + '/public') );
app.use( bodyParser.json() );
app.use( bodyParser.urlencoded() );
app.set('views', __dirname + '/public');
app.engine('html', require('ejs').renderFile);


app.get('/controller', function(req, res) {
	res.render('controller.html');
});
app.get('/client', function(req, res) {
	res.render('renderClient.html');
});

app.get('/heartbeat', function(req, res) {
	renderClients.forEach(function(client) {
	console.log("got it")
		client.emit('heartbeat', req.param('beat'));
	});
	res.send(200);
});
app.get('/gyro', function(req, res) {
	renderClients.forEach(function(client) {
		client.emit('heartbeat', {
			x: req.param('x'),
			y: req.param('y'),
			z: req.param('z')
		});
	});
	res.send(200);
});


// Socket Stuff
io.on('connection', function(socket) {
	
	socket.on('connectClient', function() {
		console.log('client connected'.green);
		renderClients.push(this);
	});
	socket.on('connectController', function() {
		console.log('controller connected'.green);
	});

	socket.on('heartbeat', function(pulse) {
		renderClients.forEach(function(client) {
			client.emit('heartbeat', pulse);
		});
		// res.send(200);
	});
	socket.on('deviceorientation', function(event) {
		// event.absolute | event.alpha | event.beta | event.gamma
		renderClients.forEach(function(client) {
			client.emit('deviceorientation', event);
		});
	});
});



// HTTP Server
http.listen(8888, function() {
	console.log('listen on port ' + '8888'.rainbow);
});