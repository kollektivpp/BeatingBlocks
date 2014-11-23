$(document).ready(function() {
	var socket = io(),
		colors = ['red','pink','purple','deepPurple','indigo','blue','lightBlue','cyan','teal','green','lightGreen','lime','yellow','amber','orange','deepOrange','brown','grey','blueGrey'],
		colorsLength = colors.length -1,
		cubies = $('.cubie'),
		cubiesLength = cubies.length - 1;


	cubies.each(function(index){
		$(this).addClass(colors[getRandomInt(0, colorsLength)]);
	});

	function getRandomInt(min, max) {
		return Math.floor(Math.random() * (max - min + 1)) + min;
	}

	window.setInterval(function() {
		var cubie = $(cubies[getRandomInt(0, cubiesLength)]),
			color = colors[getRandomInt(0, colorsLength)];
		cubie.addClass(color);
	}, 600);



	socket.emit( 'connectClient');

	socket.on('heartbeat', function(pulse) {
		console.log(pulse);
	});

	socket.on('deviceorientation', function(event) {
		// event.absolute | event.alpha | event.beta | event.gamma
		console.log(event.beta);
	});
});