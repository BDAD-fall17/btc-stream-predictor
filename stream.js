var Twitter = require('twitter');
_ = require('lodash')
var fs = require('fs');

var client = new Twitter({
  consumer_key: '<Key Redacted>',
  consumer_secret: '<Key Redacted>',
  access_token_key: '<Key Redacted>',
  access_token_secret: '<Key Redacted>'
});

// Sample test route
client.get('favorites/list', function(error, tweets, response) {
  if (error) {
    throw error;
  }
  console.log(tweets);
  console.log(response);
});

//Specifying keyword to track in the stream 'bitcoin'
var stream = client.stream('statuses/filter', {track: 'bitcoin'});

stream.on('data', function(event) {
  var obj = {
    time: event.created_at,
    text: event.text,
    userId: event.user.id,
    userName: event.user.name,
    location: event.user.location,
    followerCount: event.user.followers_count
  }

  console.log(obj);

  fs.appendFile('stream-list-full.json', JSON.stringify(event) + "\n", function (err) {
    if (err) {
      throw err;
    }
    console.log("Stream appended to file!");
  });
});
 
stream.on('error', function(error) {
  throw error;
});

/*

Keeping this as comment if required later:

// You can also get the stream in a callback if you prefer. 
client.stream('statuses/filter', {track: 'javascript'}, function(stream) {
  stream.on('data', function(event) {
    console.log(event && event.text);
  });
 
  stream.on('error', function(error) {
    throw error;
  });
});

*/
