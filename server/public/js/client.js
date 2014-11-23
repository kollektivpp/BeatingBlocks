$(document).ready(function() {
	var socket = io();

	socket.emit( 'connectClient');

	socket.on('heartbeat', function(pulse) {
		console.log(pulse);
	});

	socket.on('deviceorientation', function(event) {
		// event.absolute | event.alpha | event.beta | event.gamma
		console.log(event.beta);
	});
});