var express = require('express');
var app = express();
var body = require("body-parser");
var fs = require("fs");
var mysql = require('mysql');
app.use(body.urlencoded({ extended: false }));
app.use(body.json());
var url = require('url');


var TABLENAME = "ComicTable2";

app.post('/postComic', function(req, res) {

    var updatingComicID = parseInt(req.body.comicID);
    var completedFrames = parseInt(req.body.putFrame) + 1;
    var updatingFrameString = "Frame" + req.body.putFrame;
    var imageString = req.body.frame;

    //console.log('errything' + updatingFrame );

    // update database with new image
    var connection = mysql.createConnection({
	  host     : 'localhost',
	  user     : 'root',
	  password : '',
	  database : 'ComicRoulette'
	});

	connection.connect();

	connection.query('UPDATE '+TABLENAME+' SET Locked=FALSE, '+updatingFrameString+'="'+imageString+'", FramesCompleted='+completedFrames+'  WHERE ID='+updatingComicID+'', function(err,rows,fields){
		if (err){
			console.log('error in update query in post', err);
		} else {
			console.log('updated comic ', updatingComicID);
		}
	});

	connection.end();

    res.writeHead(200, {'Content-Type': 'application/json'});
    res.end('Got it!');
});

function randomIntInc (low, high) {
	return Math.floor(Math.random() * (high - low + 1) + low);
}

app.get('/getComic', function (req, res) {

	/*res.writeHead(200, {'Content-Type': 'application/json'});
    res.end('Page One');*/

    var connection = mysql.createConnection({
	  host     : 'localhost',
	  user     : 'root',
	  password : '',
	  database : 'ComicRoulette'
	});

	connection.connect();

	connection.query('SELECT * from '+TABLENAME+' WHERE Locked is FALSE and FramesCompleted < 4 ORDER BY RAND() LIMIT 1', function(err, rows, fields) {
	  if (!err){
	    //console.log('output:', rows);

	    // Add some randomness, sometimes they work on an existing, sometimes they start a new one
	    var randomizer = randomIntInc(1, 100);
	    console.log("randomizer is: ", randomizer);

	    if (!rows.length || randomizer < 10){
	    	
	    	// if there are no comics in progress, make a new one
	    	connection.query('INSERT into '+TABLENAME+' (FramesCompleted,Frame0,Frame1,Frame2,Frame3,Locked) values (0, null,null,null,null,TRUE)', function(err1,rows1,fields1){
	    		if (!err1){

	    			var insertId = rows1.insertId;
	    			console.log('Created Comic', insertId);
	    			var result = {'comicID':insertId, 'framesCompleted':0, 'prevFrame':'null'};
	    			
	    			res.writeHead(200, {'Content-Type': 'application/json'});
    				res.end(JSON.stringify(result));


	    		} else {
	    			console.log('error in insert query');
	    			res.writeHead(404, {'Content-Type': 'application/json'});
    				res.end('Error');
	    		}
	    	
	    	});

	    	connection.end();
	    	
	    } else {

		    var comicID = rows[0].ID;
		    var framesCompleted = rows[0].FramesCompleted;
		   	var prevFrame = "null";
		   	if (framesCompleted == 1)
		   		prevFrame = rows[0].Frame0;
		   	if (framesCompleted == 2)
		   		prevFrame = rows[0].Frame1;
		   	if (framesCompleted == 3)
		   		prevFrame = rows[0].Frame2;

			connection.query('UPDATE '+TABLENAME+' SET Locked=TRUE WHERE ID='+comicID+'', function(err2,rows2,fields2){
				if (err2){
					console.log('error in update query');
				}
			});

			console.log('Returning Comic ', comicID);

			var result = {'comicID':comicID, 'framesCompleted':framesCompleted, 'prevFrame':prevFrame};
	    			
			res.writeHead(200, {'Content-Type': 'application/json'});
			res.end(JSON.stringify(result));

			connection.end();
		}
	  } else {
	    console.log('Error while performing Query.');
	    connection.end();
	  }
	});


   /*fs.readFile( __dirname + "/" + "users.json", 'utf8', function (err, data) {
       console.log( data );
       res.end( data );
   });*/
})


app.get('/getEntireComic', function (req, res) {

	var url_parts = url.parse(req.url, true);
	var query = url_parts.query;
	var fetchComicId = req.query.comicid;
	console.log('Getting Full Comic ' + req.query.comicid);
	/*res.writeHead(200, {'Content-Type': 'application/json'});
    res.end('Page One');*/

    var connection = mysql.createConnection({
	  host     : 'localhost',
	  user     : 'root',
	  password : '',
	  database : 'ComicRoulette'
	});

	connection.connect();
	connection.query('SELECT * from '+TABLENAME+' WHERE ID='+fetchComicId+'', function(err, rows, fields) {
		if (!err){
			var result = {'comicID':rows[0].ID, 'framesCompleted':rows[0].FramesCompleted, 'f0':rows[0].Frame0, 'f1':rows[0].Frame1, 'f2':rows[0].Frame2, 'f3':rows[0].Frame3};
	    			
			res.writeHead(200, {'Content-Type': 'application/json'});
			res.end(JSON.stringify(result));
		}
	});

	connection.end();



})

var server = app.listen(8080, function () {

  var host = server.address().address
  var port = server.address().port

  console.log("Example app listening at http://%s:%s", host, port)

})

/*//Dispatcher function
var HttpDispatcher = require('../httpdispatcher');
var dispatcher = new HttpDispatcher();
//Lets require/import the HTTP module
var http = require('http');

//Lets define a port we want to listen to
const PORT=8080; 



//Lets use our dispatcher
function handleRequest(request, response){
    try {
        //log the request on console
        console.log(request.url);
        //Disptach
        dispatcher.dispatch(request, response);
    } catch(err) {
        console.log(err);
    }
}

//For all your static (js/css/images/etc.) set the directory name (relative path).
dispatcher.setStatic('resources');

//A sample GET request    
dispatcher.onGet("/page1", function(req, res) {
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end('Page One');
});    

//A sample POST request
dispatcher.onPost("/post1", function(req, res) {
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end('Got Post Data');
});

//Create a server
var server = http.createServer(handleRequest);

//Lets start our server
server.listen(PORT, function(){
    //Callback triggered when server is successfully listening. Hurray!
    console.log("Server listening on: http://localhost:%s", PORT);
});*/
