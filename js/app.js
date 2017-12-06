var express = require('express');
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var twitter = require('twitter');
var json2csv = require('json2csv');
var fs = require('fs');

app.use(express.static(__dirname + '/static'));

http.listen(3000, function(){
  console.log('listening on *:3000');
});

app.get('/', function(req, res){
  res.sendFile(__dirname + '/index.html');
});

app.post('/stream', function(req, res) {
  console.log("Received POST");
  var twit = new twitter({
    consumer_key: process.env.CONSUMER_KEY,
    consumer_secret: process.env.CONSUMER_SECRET,
    access_token_key: process.env.ACCESS_TOKEN_KEY,
    access_token_secret: process.env.ACCESS_TOKEN_SECRET
  });

  var twee = io.of('tweet');
  var count = 0;
  var jsonList = [];

  twit.stream('statuses/filter', { track: 'bitcoin', language: 'en' }, function(stream) {
    stream.on('data', function (data) {
      count++;
      io.sockets.emit('tweet', data.text);
      console.log('.');

      jsonList.push(data);

      if ((count % 100) === 0) {
        //var fields = ["time", "text", "userId", "userName", "location", "followerCount"];
        var fields = ["created_at", "text", "user.id_str", "user.name", "user.location", "user.followers_count"];
        var csv = json2csv({ data: jsonList, fields: fields });

        fs.writeFile('stream-live-'+count+'.csv', csv, function(err) {
          if (err) throw err;
          console.log('file saved');
          jsonList = [];
        });
      }
    });
    res.send("OK");
  });
});

if (process.env.NODE_ENV !== 'production') {
  require('dotenv').load();
}
