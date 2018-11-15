package tilegame.john.powers;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Communicator {

	public static ArrayList<String> message = new ArrayList<String>();
	
	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	TransformerFactory traFactory = TransformerFactory.newInstance();
	
	public String StateToMessage() {
		return StateToMessage(null);
	}
	public String StateToMessage(TileGame game) {
		
		Document document = CreateDocument();
		
		Element root = document.createElement("Root");
		document.appendChild(root);
		
		//Append information on game state, server messages and highscores.
		if(game != null) {
			AppendGame(document, root, game);
		}
		AppendMessages(document, root);
		AppendScores(document, root);
		
		return DocumentToString(document);
	}
	
	private Document CreateDocument() {
		Document result = null;
		
		//Create a document to edit.
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(docBuilder != null) {
			result = docBuilder.newDocument();				
		}
			
		return result;
	}
	
	
	private void AppendGame(Document doc, Element parent, TileGame game) {
		Element root = doc.createElement("TileGame");
		parent.appendChild(root);
		
		//Create player information.
		Element playerRoot = AddNode(doc, root, "Player");
		AppendText(doc, playerRoot, "PlayerScore", game.score);
		
		//Create time information.
		Element timeRoot = AddNode(doc, root, "Time");
		AppendText(doc, timeRoot, "MaxTurn", game.lastTurn);
		AppendText(doc, timeRoot, "CurrentTurn", game.clock);
		
		//Create resource information
		Element resourceRoot = AddNode(doc, root, "PlayerResources");
		String[] resourceStrings = game.getResourceStrings();
		int[] resourceInts = game.getResourceInts();
		Element resource;
		for(int i=0; i<resourceStrings.length; i++) {
			resource = AddNode(doc, resourceRoot, "Resource");
			AppendText(doc, resource, "ResourceName", resourceStrings[i]);
			AppendText(doc, resource, "ResourceCount", resourceInts[i]);
		}
		
		//Create tiles.
		Element tileRoot = doc.createElement("Tiles");
		root.appendChild(tileRoot);
		Tile[] tiles = game.GetVisibleTiles();
		Element working;
		for(Tile tile : tiles) {
			working = ToXML(doc, tile);
			tileRoot.appendChild(working);
		}
	}

	private void AppendMessages(Document doc, Element parent) {
		//Append messages.
		if(message.size() > 0) {
			Element root = doc.createElement("Messages");
			parent.appendChild(root);
			for(String line : message) {
				AppendText(doc, root, "MessageLine", line);
			}
		}
	}
	
	private void AppendScores(Document doc, Element parent) {
		Highscore[] scores = TileServlet.GetHighscores();
		if(scores.length > 0) {
			Element root = doc.createElement("Highscores");
			parent.appendChild(root);
			
			//Append each highscore in root.
			Element working;
			for(Highscore score : scores) {
				working = ToXML(doc, score);
				root.appendChild(working);
			}
		}
	}
	
	private String DocumentToString(Document doc) {
		//Convert result to string.
        Transformer transformer = null;
		try {
			transformer = traFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		if(transformer != null) {
	        StringWriter writer = new StringWriter();
	        try {
				transformer.transform(new DOMSource(doc), new StreamResult(writer));
			} catch (TransformerException e) {
				e.printStackTrace();
			}
	        return writer.getBuffer().toString();
		}
		return null;
	}
	
	
	private Element ToXML(Document doc, Highscore score) {
		Element result = doc.createElement("Highscore");
		
		AppendText(doc, result, "Nickname", score.owner);
		AppendText(doc, result, "Points", score.score);
		
		return result;
	}

	private Element ToXML(Document doc, Tile tile) {
		Element working;
		Element result = doc.createElement("Tile");
		
		AppendText(doc, result, "ID", tile.identifier);
		AppendText(doc, result, "Name", tile.name);
		AppendText(doc, result, "State", tile.state.ordinal());
		//AppendText(doc, result, "StateString", Tile.StateString(tile.state));
		
		
		
		//Resources
		Element resources = AddNode(doc, result, "Resources");
		
		working = AddNode(doc, resources, "Explore");
		AppendResources(doc, working, "ExploreCost", tile.exploreCost);
		AppendResources(doc, working, "ExploreGain", tile.exploreGain);
		
		working = AddNode(doc, resources, "Claim");
		AppendResources(doc, working, "ClaimCost", tile.claimCost);
		AppendResources(doc, working, "ClaimGain", tile.claimGain);
		
		working = AddNode(doc, resources, "Recurring");
		AppendText(doc, working, "RecurringInterval", tile.GetRecurringInterval());
		AppendText(doc, working, "TimeToProduce", tile.timeToProduce);
		AppendResources(doc, working, "RecurringGain", tile.recurringGain);

		
		
		//Neighbors
		AppendText(doc, result, "NeighborCount", tile.neighbors.length);
		/*Element neighbors = AddNode(doc, result, "Neighbors");
		
		for(Tile neighbor : tile.neighbors) {
			working = AddNode(doc, neighbors, "Neighbor");
			AppendText(doc, working, "NeighborID", neighbor.identifier);
			AppendText(doc, working, "NeighborState", neighbor.state.ordinal());
			AppendText(doc, working, "NeighborStateString", Tile.StateString(neighbor.state));
		}
		*/
		
		return result;
	}
	
	private Element CreateNode(Document doc, String tag) {
		Element result = doc.createElement(tag);
		return result;
	}
	
	private Element AddNode(Document doc, Element parent, String tag) {
		Element child = CreateNode(doc, tag);
		parent.appendChild(child);
		return child;
	}
	
	private Element AppendText(Document doc, Element parent, String tag, String text) {
		Element child = AddNode(doc, parent, tag);
		child.appendChild(doc.createTextNode(text));
		return child;
	}
	
	private Element AppendText(Document doc, Element parent, String tag, int number) {
		return AppendText(doc, parent, tag, String.valueOf(number));
	}

	private Element AppendResources(Document doc, Element parent, String tag, Map<String, Integer> resources) {
		Element result = AddNode(doc, parent, tag);

		String[] resourceStrings = Tile.getResourceStrings(resources);
		int[] resourceInts = Tile.getResourceInts(resources);
		
		Element resource;
		for(int i=0; i<resourceStrings.length; i++) {
			resource = AddNode(doc, result, "Resource");
			AppendText(doc, resource, "ResourceName", resourceStrings[i]);
			AppendText(doc, resource, "ResourceCount", resourceInts[i]);
		}
		
		return result;
	}

	

}
