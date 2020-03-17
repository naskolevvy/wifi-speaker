const net = require('net');
var yap = require('youtube-audio-player');
var system = require('system-control')();
//download some libraries used from the raspberry pi 
var client = new net.Socket();//open new client
client.connect(1082, '164.132.56.199', function() {//connect to the web server
    console.log('Connected');
});

client.on('data', function(data) {//when the client recieves some data
        data = data.toString();
    console.log('Received: ' + data);
        if(data === "STOP"){
                yap.stop();//stop the current stream
                console.log("STOPPED");
        }else if(data === "VOLUME UP"){ //change volume of raspberry pi up
		system.audio.getSystemVolume().then(function(volume) { 
  			 system.audio.setSystemVolume(volume+2).then(function() {
				console.log('Volume: '+volume);
                	});
		});
        }else if(data === "VOLUME DOWN"){// change volume down
                system.audio.getSystemVolume().then(function(volume) {
                         system.audio.setSystemVolume(volume-2).then(function() {
				 console.log('Volume: '+volume);
                        });
                });
        }else if(data.includes("PLAY")){ //play selected from the app song 
		yap.stop();//stop previous stream
                var id = data.split(" ")[1]; //use id send from the app
                console.log("Playing ID: "+ id);
                yap.play({ url: 'https://www.youtube.com/watch?v='+id});//start new stream
        }
});

client.on('close', function() { //close the connection
    console.log('Connection closed');
});
