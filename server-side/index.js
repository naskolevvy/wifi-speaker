const express = require('express');
const app = express();
const net = require('net');
const request = require('request');
const fs = require('fs');
//libraries used by the node app 
const webPort = 1080;
const androidPort = 1081;
const rpiPort = 1082;
//defining ports for communication 
var lastPlayed;
var playlist = [];
var clients = [];
var rpi;

fs.readFile('playlist.json', 'utf8', function (err, data) { //function to send the file with the playlist to the app
	if(err) throw err;
	playlist = JSON.parse(data);
});

app.get('/error/', function(req, res) { //send error message if the link entered is copyrighted 
	var problem = req.query.problem;
	if(problem === "error") {
		console.log("[ERROR] Copyrighted link!");
		broadcast("INVALID "+lastPlayed); //send error to the app 
	}
});

app.get('/add/', function(req, res) {
	id = req.query.id; // get the id of the query
	request('https://www.googleapis.com/youtube/v3/videos?key=AIzaSyCtx42SlNQQZT2_updzdzo7_9ikDhez2l0&id='+req.query.id+'&part=snippet&fields=items/snippet/title', function (error, response, body) {
		if(JSON.parse(body).items.length > 0) {

			title = JSON.parse(body).items[0].snippet.title; // get the title from YT API
			song = {"id":id, "title":title}; // create object
			console.log("ADDING: "+title);
			playlist.push(song); // add the  new object to the array
		}else{
			console.log("Somebody attempted to add invalid link : "+id);
		}

		broadcast(JSON.stringify(playlist));
		fs.writeFile('playlist.json', JSON.stringify(playlist), function (err){});
	});
	res.send("Page for adding any songs.");
});

app.get('/remove/', function(req, res){// function to remove a song from the playlist
	id = req.query.id; //create the object
	playlist.forEach(function(song){
		if(song.id === id) { //match the id from the file and the request
			console.log("DELETING: "+song.title);
			playlist.splice(playlist.indexOf(song),1);//delete the song
		}
	});

	broadcast(JSON.stringify(playlist));
	fs.writeFile('playlist.json', JSON.stringify(playlist), function (err){});

	res.send("Page for removing songs.");
});

app.get('/cmd/', function(req, res) {//function containing some commands 
	cmd = req.query.cmd;
	if(cmd === "UP") { //change volume
		console.log("Volume UP!");
		 if(rpi !== null) {
                        rpi.write("VOLUME UP");
                }
	}else if(cmd === "DOWN"){ //change volume
		console.log("VOLUME DOWN!");
		if(rpi !== null) {
                        rpi.write("VOLUME DOWN");
                }
	}else if(cmd === "STOP"){ // stop the stream 
            	console.log("STOP");
                if(rpi !== null) {
			rpi.write("STOP");
		}
        }else if(cmd === "PLAY") {//strart playing particular song
		id = req.query.id;
		console.log("PLAY "+ id);//use the id to find which song is supposed to be played
		if(rpi !== null) {
			lastPlayed = id;
			rpi.write("PLAY "+id);
		}else{
			cosnole.log("[ERROR] Missing speaker!");
		}
	}

	res.send("Page for controlling the volume.");
});

var server = app.listen(webPort, function(){ //define a server
    console.log("Web app listening at port "+ webPort);
});

var server_android = net.createServer(function(sock) {

	clients.push(sock);
	sock.write(JSON.stringify(playlist)+"\n");
	// We have a connection - a socket object is assigned to the connection automatically
	console.log('[ANDROID] CONNECTED: ' + sock.remoteAddress +':'+ sock.remotePort);

	// Add a 'data' event handler to this instance of socket
	sock.on('data', function(data) {
        	console.log('[ANDROID] DATA ' + sock.remoteAddress + ': ' + data);
    	});

	sock.on('close', function(data) { //closing the connection 
		clients.splice(clients.indexOf(sock),1);
        	console.log('[ANDROID] CLOSED: ' + sock.remoteAddress +' '+ sock.remotePort);
    	});

	sock.on('error', function(data) { //what to do if there is an error
                clients.splice(clients.indexOf(sock),1);
                console.log('[ANDROID] ERROR-CLOSED: ' + sock.remoteAddress +' '+ sock.remotePort);
        });
}).listen(androidPort); //handling some events with the server 

var server_rpi = net.createServer(function(sock) { //create a server to communicate with the raspberry pi

	rpi = sock;
	console.log('[RPI] CONNECTED: ' + sock.remoteAddress +':'+ sock.remotePort);

	sock.on('data', function(data) {
                console.log('DATA ' + sock.remoteAddress + ': ' + data);
                sock.write('[RPI] Said ' + data +'\n');

        });

        sock.on('close', function(data) { // close the connection 
                console.log('[RPI] CLOSED: ' + sock.remoteAddress +' '+ sock.remotePort);
        });

        sock.on('error', function(data) { //error close the connection
                console.log('[RPI] ERROR-CLOSED: ' + sock.remoteAddress +' '+ sock.remotePort);
        });


}).listen(rpiPort);

/* Utility functions */
function broadcast(message) { //function to display messages to all android app users 
	clients.forEach(function (client) {
        	client.write(message+"\n");
	});
}

