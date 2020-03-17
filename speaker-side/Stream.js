const net = require('net');
var yap = require('youtube-audio-player');
var intercept = require("intercept-stdout");
//download some libraries used from the raspberry pi 
var client = new net.Socket(); //open new client
client.connect(1082, '164.132.56.199', function() { //connect to the web server
    console.log('Connected');
});

client.on('data', function(data) { //when the client recieves some data
        data = data.toString();
    console.log('Received: ' + data);
        if(data === "STOP"){ //if the command is stop
                yap.stop(); //stop the current stream
                console.log("STOPPED");
          //      restart();
        }else if(data === "NEXT"){ //we haven't developed this function yet

        }else if(data.includes("PLAY")){ //play specific song 
		yap.stop(); //stop previous stream
                var id = data.split(" ")[1];
                console.log("Playing ID: "+ id);
                yap.play({ url: 'https://www.youtube.com/watch?v='+id}); //start new stream
        }
});

client.on('close', function() { //close the connection 
    console.log('Connection closed');
});
