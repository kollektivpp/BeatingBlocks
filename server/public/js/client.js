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
		interval = null,
		untilEnd = false,
		nextOrientationCall = false;


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
		untilEnd = false;

	}


	socket.emit( 'connectClient');

	socket.on('heartbeat', function(pulse) {
		if (untilEnd || pulse <= 20) {
			// cubies.addClass('grey');
			return;
		}
		untilEnd = true;
		playHeartbeat();
		// cubies.addClass(colors[getRandomInt(0, colorsLength)]);
		if (interval) {
			clearInterval(interval);
		}
		interval = setInterval(playHeartbeat, (60000 / pulse));
	});

	socket.on('deviceorientation', function(event) {
		if (nextOrientationCall) {
			return;
		}
		setInterval(function(){
			nextOrientationCall = false;
		}, 200);

		var obj = JSON.parse(event);
		var x = obj.axisx,
			y = obj.axisy,
			z = obj.axisz;
		mainCube.css('-webkit-transform', 'rotateX(' + x + 'deg) rotateY(' + y + 'deg) rotateZ(' + z + 'deg)');
		nextOrientationCall = true;
	});
});