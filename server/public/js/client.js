$(document).ready(function() {
	var socket = io(),
		colors = ['red','pink','purple','deepPurple','indigo','blue','lightBlue','cyan','teal','green','lightGreen','lime','yellow','amber','orange','deepOrange','brown','grey','blueGrey'],
		colorsLength = colors.length -1,
		mainCube = $('#cube'),
		cubies = $('.cubie'),
		cubiesLength = cubies.length - 1,
		left = $('.left .cubie'),
		back = $('.back .cubie'),
		down = $('.down .cubie'),
		up = $('.up .cubie'),
		front = $('.front .cubie'),
		right = $('.right .cubie'),
		interval = null;


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

	function playHeartbeat() {
		left.addClass('beat');
		back.addClass('beat');
		down.addClass('beat');
		up.addClass('beat');
		front.addClass('beat');
		right.addClass('beat');
		cubies.addClass('beat');
		setTimeout(heartDown, 200);
	}
	function heartDown() {
		left.removeClass('beat');
		back.removeClass('beat');
		down.removeClass('beat');
		up.removeClass('beat');
		front.removeClass('beat');
		right.removeClass('beat');
		cubies.removeClass('beat');
	}


	socket.emit( 'connectClient');

	socket.on('heartbeat', function(pulse) {
		playHeartbeat();
		if (interval) {
			clearInterval(interval);
		}
		interval = setInterval(playHeartbeat, (60000 / pulse.data));
	});

	socket.on('deviceorientation', function(event) {
		var obj = JSON.parse(event);
		var x = obj.axisx,
			y = obj.axisy,
			z = obj.axisz;
		mainCube.css('-webkit-transform', 'rotateX(' + x + 'deg) rotateY(' + y + 'deg) rotateZ(' + z + 'deg)');
	});
});