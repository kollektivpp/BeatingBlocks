$(document).ready(function() {
	var socket = io(),
		promise = new FULLTILT.getDeviceOrientation({ 'type': 'world' }),
		deviceOrientation;

	socket.emit( 'connectController' );
	// socket.emit( 'heartbeat', 68 );


	// function getOrientation() {
		promise
			.then(function(orientationData) {
				// Store the returned FULLTILT.DeviceOrientation object
				// socket.emit('deviceorientation', orientationData);
				console.log(orientationData.getScreenAdjustedQuaternion());
				console.log(orientationData.getScreenAdjustedMatrix());
				console.log(orientationData.getScreenAdjustedEuler());
			})
			.catch(function(message) {
				console.error(message);
			});
	// }
	// setInterval(getOrientation, 500);
});