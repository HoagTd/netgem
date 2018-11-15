/**
 *	By JS, 2018. 
 */

var servletURL = "/TileServlet";

var nickID = "nickname";
var contactStatusLine = "statusline";
var responseLine = "responseline"
var messageLine = "messageline"
var tileRoot = "tileroot";
var timeBarID = "timebar";
var timeMessageID = "timemessage";
var resourceBoxID = "resourceroot";
var scoreMessageID = "scoremessage";
var normalRowID = "row1";
var scoreRowID = "row1b";
var highscoresID = "highscores";

var exploreStateString = "Explore";
var exploreStateInstruction = "Click to Explore.";
var claimableStateString = "Claim";
var claimableStateInstruction = "Click to Claim.";
var claimedStateString = "Claimed";
var claimedStateInstruction = "Already Claimed.";
var tileBoxHeightGain = 30;
var tileLineHeightGain = 10;
var tileBoxUpperHeightString = window.getComputedStyle(document.body).getPropertyValue('--tile-upper-height');
var tileBoxUpperHeight = Number(tileBoxUpperHeightString.substring(0, tileBoxUpperHeightString.indexOf("p")));

var str = "xxx_456";
var str_sub = str.substr(str.lastIndexOf("_")+1);



//Use a GET function to retrieve data from server.
function RequestGet(url, nextTask){
	var xhttp = new XMLHttpRequest();
	xhttp.open("GET", url, true);
	
	xhttp.onreadystatechange = function(){
		DisplayRequestStatus(this);
		if (this.readyState == 4) {
			nextTask(this);
		}
	}
	
	xhttp.send();
}

//Use a POST function to deliver instruction to server.
function RequestPost(url, nextTask, data){
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", url, true);
	xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	
	xhttp.onreadystatechange = function(){
		DisplayRequestStatus(this);
		if (this.readyState == 4) {
			nextTask(this);
		}
	}
	
	xhttp.send(data);
}

//Show the status of the request in the contact status area.
function DisplayRequestStatus(response){
	var html = "<h4>Connection</h4>" +
			"<p>Response: status "+response.status+", "+response.statusText+".</p>";
	document.getElementById(contactStatusLine).innerHTML = html;	
}

Highscore();
function Highscore(){
	var element = document.getElementById(scoreRowID);
    if (element.style.display === "none") {
        element.style.display = "block";
    } else {
        element.style.display = "none";
    }
}

function NewGame(){
	//First order of business: make sure we got a sane input from the user.
	//Get value
	var nick = document.getElementById(nickID).value;
	nick = nick.trim();
	nick = nick.replace(/[^0-9a-z]/gi, '')
	nick = nick.trim();
	
	if(nick.length>0){
		var commands = "?";
		commands += "Command=NewGame";
		commands += "&Nick="+nick;
		
		var url = servletURL + commands;
		
		var html = "<h4>Connection</h4>" +"<p>New game.</p>";
		document.getElementById(responseLine).innerHTML = html;
		
		var xhttp = new XMLHttpRequest();
		xhttp.open("GET", url, true);
		
		xhttp.onreadystatechange = function(){
			DisplayRequestStatus(this);
			if (this.readyState == 4) {
				ProcessBoardState(this);
			}
		}
		
		xhttp.send();
	}
}

//1. Use a http get to retrieve board state from server.
//2. Turn response into XML.
//3. Turn XML into HTML.
//4. Place HTML in response area.
function LoadBoardState(){
	RequestGet(servletURL, ProcessBoardState);
}

function ProcessBoardState(response){
	if(response.status == 200){//If the response is OK.
		var xml = response.responseXML;
		ClearPreviousElements();
		DisplayBoardState(xml);
		DisplayMessages(xml);
		DisplayHighscores(xml);
	}else{//If the response is anything else...
		var html = "<h4>Connection</h4>" +
				"<p>ERROR: Unable to retrieve game state.</p>" +
				"<p>Code:"+response.status+", "+response.statusText+"</p>";
		document.getElementById(responseLine).innerHTML = html;
	}
}

function ClearPreviousElements(){
	//Clear previous elements.
	var oldResources = document.getElementById(resourceBoxID);
	while (oldResources.firstChild) {
		oldResources.removeChild(oldResources.firstChild);
	}
	var oldTiles = document.getElementById(tileRoot);
	while (oldTiles.firstChild) {
		oldTiles.removeChild(oldTiles.firstChild);
	}
}

function DisplayBoardState(xml){
	var html ="<h4>Connection</h4>" + "<p>Displaying board state.</p>";
	
	//Set time bar.
	var maxTime = xml.getElementsByTagName("MaxTurn")[0].innerHTML;
	var currentTime = xml.getElementsByTagName("CurrentTurn")[0].innerHTML;
	SetTimeBar(currentTime, maxTime);

	//Set score.
	var score = xml.getElementsByTagName("PlayerScore")[0].innerHTML;
	document.getElementById(scoreMessageID).innerHTML = "Score: "+score+".";
	
	//Set player resources.
	var playerResources = xml.getElementsByTagName("PlayerResources")[0].childNodes;
	for(var i=0; i<playerResources.length; i++){
		CreatePlayerResource(playerResources[i]);
	}
	
	
	//We use the XML to create new elements.
	var tiles = xml.getElementsByTagName("Tile");
	html += "<p>Tiles to show: "+tiles.length+"</p>";
	for(var i=0; i<tiles.length; i++){
		CreateTile(tiles[i]);
	}
	document.getElementById(responseLine).innerHTML = html;
	
	
}

function DisplayMessages(xml){
	//Display server messages.
	var messages = xml.getElementsByTagName("MessageLine");
	if(messages.length > 0){
		html = "<p>Messages:</p>";
	}
	for(var i=0; i<messages.length; i++){
		html += "<p>"+messages[i].innerHTML+"</p>";
	}
	document.getElementById(messageLine).innerHTML = html;
}

function DisplayHighscores(xml){
	//Display highscores, if we have any..
	var scores = xml.getElementsByTagName("Highscore");
	var html;
	if(scores.length > 0){
		html = "<p>Player: score.</p>";
		var name;
		var points;
		for(var i=0; i<scores.length; i++){
			name = scores[i].getElementsByTagName("Nickname")[0].innerHTML;
			points = scores[i].getElementsByTagName("Points")[0].innerHTML;
			html += "<p>"+(i+1)+". "+name+": "+points+" points.</p>";
		}
	}else{
		html = "<p>No highscores to show.</p>";
	}
	document.getElementById(highscoresID).innerHTML = html;
}


function SetTimeBar(current, max){
	var timebar = document.getElementById(timeBarID);
	var timemessage = document.getElementById(timeMessageID);
	
	var timePercentage = current / max * 100;
	timePercentage = Math.min(timePercentage, 100);
	timePercentage += "%";
	timebar.style.width = timePercentage;
	
	timemessage.innerHTML = "Turn "+current+"/"+max;
}

function CreatePlayerResource(xml){
	//Root into which the resource will be ultimately added.
	var resourceBox = document.getElementById(resourceBoxID);
	var resource = CreateResourceFromXML(xml);
	resourceBox.appendChild(resource);
}

function CreateResourceFromXML(xml){
	var name = xml.getElementsByTagName("ResourceName")[0].innerHTML;
	var count = xml.getElementsByTagName("ResourceCount")[0].innerHTML;
	
	return CreateResource(name, count);
}

//WARNING: CreateResource, CreateResourceAdd and CreateResourcePay should always
//be identical, with exception of setting of class name.
//Calling CreateResource first, then setting the class in Add or Pay throws error over recursion.
function CreateResourceAdd(name, count){
	var resource = document.createElement("span");
	resource.className = "resource resourceof"+name;
	resource.style.zIndex = "15";
	resource.innerHTML = ""+count+" x "+name+"";
	resource.className += " resourceadd";
	
	return resource;
}

function CreateResourcePay(name, count){
	var resource = document.createElement("span");
	resource.className = "resource resourceof"+name;
	resource.style.zIndex = "15";
	resource.innerHTML = ""+count+" x "+name+"";
	resource.className += " resourcepay";
	
	return resource;
}

function CreateResource(name, count){
	var resource = document.createElement("span");
	resource.className = "resource resourceof"+name;
	resource.style.zIndex = "15";
	resource.innerHTML = ""+count+" x "+name+"";
	
	return resource;
}

function ResourcesFromArray(parent, array){
	var resource;
	for(var i=0; i<array.length; i++){
		resource = CreateResource(array[i].type, array[i].count);
		parent.appendChild(resource);
	}
}

function ResourcesFromArrayAdd(parent, array){
	var resource;
	for(var i=0; i<array.length; i++){
		resource = CreateResourceAdd(array[i].type, array[i].count);
		parent.appendChild(resource);
	}
}

function ResourcesFromArrayPay(parent, array){
	var resource;
	for(var i=0; i<array.length; i++){
		resource = CreateResourcePay(array[i].type, array[i].count);
		parent.appendChild(resource);
	}
}

function CreateTile(xml){
	//Find root into which we add this tile.
	var root = document.getElementById(tileRoot);
	//var heights = [];
	var height = tileBoxUpperHeight;
	
	//Read variables from XML.
	var t_id = xml.getElementsByTagName("ID")[0].innerHTML;
	var t_name = xml.getElementsByTagName("Name")[0].innerHTML;
	var t_state = xml.getElementsByTagName("State")[0].innerHTML;
	var t_neighbors = xml.getElementsByTagName("NeighborCount")[0].innerHTML;
	
	var exploreCostXML = xml.getElementsByTagName("ExploreCost")[0].childNodes;
	var exploreGainXML = xml.getElementsByTagName("ExploreGain")[0].childNodes;
	var claimCostXML = xml.getElementsByTagName("ClaimCost")[0].childNodes;
	var claimGainXML = xml.getElementsByTagName("ClaimGain")[0].childNodes;
	var recurringGainXML = xml.getElementsByTagName("RecurringGain")[0].childNodes;
	
	var t_recurringInterval = xml.getElementsByTagName("RecurringInterval")[0].innerHTML;
	var t_timeToProduce = xml.getElementsByTagName("TimeToProduce")[0].innerHTML;

	//Resources are tricky and require additional steps to prepare them for use.
	//Create arrays to hold the resource objects we will be creating.
	var exploreCostArray = XMLToResourceArray(exploreCostXML);
	var exploreGainArray = XMLToResourceArray(exploreGainXML);
	var claimCostArray = XMLToResourceArray(claimCostXML);
	var claimGainArray = XMLToResourceArray(claimGainXML);
	var recurringGainArray = XMLToResourceArray(recurringGainXML);
	
	
	//Create the actual document elements of the token.
	//Create root element of the tile.
	var tile = AddElement("div", root, "tile", "Tile"+t_id);
	tile.style.zIndex = "10";
	tile.setAttribute("data-id", t_id);
	tile.setAttribute("data-state", t_state);
	tile.onclick = function (){ClickTile(this)};
	
	//Upper and lower sections.
	var upper = AddElement("div", tile, "upper", "Tile"+t_id+"Upper");
	upper.style.zIndex = "11";
	
	var lower = AddElement("div", tile, "lower", "Tile"+t_id+"Lower");
	lower.style.zIndex = "11";
	
	//Create text lines.
	var line1 = AddElement("h4", upper, "tiletitle", "Tile"+t_id+"Line1")
	var line2 = AddTextLine(upper, "tileline", "Tile"+t_id+"Line2")
	var line3 = AddTextLine(upper, "tileline", "Tile"+t_id+"Line3")
	
	//heights.push(line1);
	//heights.push(line2);
	//heights.push(line3);
	//height += 50;
	
	//Set values.
	//General values.
	if(t_state == 2){
		tile.style.setProperty("--tile-bg-color", "var(--tile-bg-color-explore)");
		tile.setAttribute("data-clickable", "true");
		tile.className += " clickable";
	}else if(t_state == 3){
		tile.style.setProperty("--tile-bg-color", "var(--tile-bg-color-claim)");
		tile.setAttribute("data-clickable", "true");
		tile.className += " clickable";
	}else if(t_state == 4){
		tile.style.setProperty("--tile-bg-color", "var(--tile-bg-color-claimed)");
		tile.setAttribute("data-clickable", "false");
	}else{//This should not happen.
		tile.style.setProperty("--tile-bg-color", "var(--tile-bg-color-default)");
		tile.setAttribute("data-clickable", "false");
	}
	
	//Line 1
	line1.innerHTML = t_name;
	
	//Lines 2 and 3 (current state and instruction to user).
	if(t_state == 2){
		line2.innerHTML = exploreStateString;
		line3.innerHTML = exploreStateInstruction;
		
	}else if(t_state == 3){
		line2.innerHTML = claimableStateString;
		line3.innerHTML = claimableStateInstruction;
		
	}else if(t_state == 4){
		line2.innerHTML = claimedStateString;
		line3.innerHTML = claimedStateInstruction;
		
	}else{//This should not happen.
		line2.innerHTML = "Default";
		line3.innerHTML = "Please report error.";
		
	}
	
	//STATES: We add all reward boxes of current state or later.
	var box;
	var working;
	//Explore rewards are still relevant.
	if(t_state <= 2){
		if((exploreCostArray.length > 0) | (exploreGainArray.length > 0) | (t_neighbors > 1)){
			var box = AddElement("div", lower, "resourcebox explorebox", "Tile"+t_id+"Explorebox");
			box.style.zIndex = "13";
			//heights.push(box);
			height += tileBoxHeightGain;
			
			//Explore cost
			if(exploreCostArray.length > 0){
				working = AddTextLine(box, "tileline", "Tile"+t_id+"ExploreCostLine");
				working.innerHTML = "Exploration Cost";
				ResourcesFromArrayPay(box, exploreCostArray);
				height += tileBoxHeightGain;
			}
			
			//Explore reward
			if(exploreGainArray.length > 0){
				working = AddTextLine(box, "tileline", "Tile"+t_id+"ExploreGainLine");
				working.innerHTML = "Exploration Rewards:";
				ResourcesFromArrayAdd(box, exploreGainArray);
				height += tileBoxHeightGain;
			}
			
			//Neighbors
			working = AddTextLine(box, "tileline", "Tile"+t_id+"ExploreNeighborLine");
			if(t_neighbors > 1){
				working.innerHTML = "Reveals "+(t_neighbors-1)+" tiles.";
				height += tileBoxHeightGain;
			}else{
				working.innerHTML = "Reveals no tiles.";
			}
		}
	}
	//Claim rewards are still relevant.
	if(t_state <= 3){
		if((claimCostArray.length > 0) | (claimGainArray.length > 0)){
			var box = AddElement("div", lower, "resourcebox claimbox", "Tile"+t_id+"Claimbox");
			box.style.zIndex = "13";
			height += tileBoxHeightGain;
			//heights.push(box);
			
			//Claim cost
			if(claimCostArray.length > 0){
				working = AddTextLine(box, "tileline", "Tile"+t_id+"ClaimCostLine");
				working.innerHTML = "Claim Cost";
				ResourcesFromArrayPay(box, claimCostArray);
				height += tileBoxHeightGain;
				height += tileLineHeightGain;
			}
			
			//Claim reward
			if(claimGainArray.length > 0){
				working = AddTextLine(box, "tileline", "Tile"+t_id+"ClaimGainLine");
				working.innerHTML = "Claim Rewards:";
				ResourcesFromArrayAdd(box, claimGainArray);
				height += tileBoxHeightGain;
				height += tileLineHeightGain;
			}
		}
	}
	//Production rewards are still relevant.
	if(t_state <= 4){
		if(recurringGainArray.length > 0){
			var box = AddElement("div", lower, "resourcebox producebox", "Tile"+t_id+"Producebox");
			box.style.zIndex = "13";
			//heights.push(box);
			height += tileBoxHeightGain;
			
			//Line 4: Recurring gain.
			working = AddTextLine(box, "tileline", "Tile"+t_id+"RecurringGainLine");
			if(recurringGainArray.length > 0){
				working.innerHTML = "Production:";
				ResourcesFromArrayPay(box, recurringGainArray);
				height += tileBoxHeightGain;
				height += tileLineHeightGain;
			}else{
				working.innerHTML = "No production.";
				height += tileLineHeightGain;
			}
			
			//Line 5: Time to next production (future information).
			working = AddTextLine(box, "tileline", "Tile"+t_id+"RecurringETALine");
			working.innerHTML = "Production ETA: "+t_timeToProduce+".";
			height += tileLineHeightGain;
			
			//Line 6.
			working = AddTextLine(box, "tileline", "Tile"+t_id+"RecurringIntervalLine");
			working.innerHTML = "Production interval "+t_recurringInterval+".";
			height += tileLineHeightGain;
		}
	}
	
	//Calculate height of all objects inside the tile.
	
	//height = heights.length * 40;
	/*
	for(var i=0; i<heights.length; i++){
		height += heights[i].style.height;
	}
	*/
	
	tile.style.height = ""+height+"px";
	upper.style.height = ""+tileBoxUpperHeight+"px";
	lower.style.height = ""+(height-tileBoxUpperHeight)+"px";
}

function XMLToResourceArray(xml){
	var result = [];
	var working;
	var x_type;
	var x_count;
	for(var i=0; i<xml.length; i++){
		x_type = xml[i].getElementsByTagName("ResourceName")[0].innerHTML;
		x_count = xml[i].getElementsByTagName("ResourceCount")[0].innerHTML;
		working = {type: x_type, count: x_count};
		result[i] = working;
	}
	
	return result;
}

function AddTextLine(parent, cname, id){
	return AddElement("p", parent, cname, id);
}

function AddElement(type, parent, cname, id){
	var line = document.createElement(type);
	line.className = cname;
	line.id = id;
	line.style.zIndex = "14";
	parent.appendChild(line);
	return line;
}



//1. Figure out which item we clicked on.
//2. Send the click to server as a POST.
//3. After the server replies, process it as if we had used GET.

function ClickTile(tile){
	var clickable = tile.getAttribute("data-clickable");
	if(clickable === "true"){
		//console.log("Click tile. TILE: "+tile);
		var id = tile.getAttribute("data-id");
		//console.log("Click tile. ID: "+id);
		
		var url = servletURL + "?ID="+id;
		
		var html ="<h4>Connection</h4>" + "<p>Clicked on tile with id "+id+".</p>";
		document.getElementById(responseLine).innerHTML = html;
		
		var xhttp = new XMLHttpRequest();
		xhttp.open("GET", url, true);
		
		xhttp.onreadystatechange = function(){
			DisplayRequestStatus(this);
			if (this.readyState == 4) {
				ProcessBoardState(this);
			}
		}
		
		xhttp.send();	
	}
}
