package tilegame.john.powers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tilegame.john.powers.Tile.TileState;



public class Generator {

	public static final String[] resourceNames = new String[] {"Money", "Score", "Goods"};
	public static final int[] resourceBaseCosts = new int[] {1, 5, 3};
	public static final int scoreIndex = 1;
	
	public static boolean IsScore(String name) {
		if(name.equals(resourceNames[scoreIndex])) {
			return true;
		}else {
			return false;
		}
	}
	
	public static boolean IsScore(int index) {
		if(index == scoreIndex) {
			return true;
		}else {
			return false;
		}
	}
	
	TileGame game;
	
	private static Random random;
	private static int tileID = 0;
	private ArrayList<Tile> tileDeck;
	
	public Generator() {
		random = new Random();
	}
	
	public void CreateUniverse(TileGame g, List<Tile> list, int tilesInUniverse, int maxLinks) throws Exception {
		//
		game = g;
		
		//Create the initial tile.
		Tile tile = CreateTile(list);
		tile.state = TileState.claimable;
		
		//Initialize the deck of tiles.
		tileDeck = new ArrayList<Tile>();
		while(tileDeck.size() < (tilesInUniverse-1)) {
			CreateTile(tileDeck);
		}
		
		CreateLinkage(list, maxLinks);
		
		UpdateVisibility(list);
	}
	
	public Tile CreateTile(List<Tile> list) {
		Tile tile = new Tile(game, tileID, "Tile "+String.valueOf(tileID));
		tileID++;
		
		//Initialize tile.
		tile.state = TileState.hidden;
		AssignTileResources(tile);
		
		list.add(tile);
		return tile;
	}
	
	public void LinkTiles(Tile first, Tile second) {
		first.AddNeighbor(second);
		second.AddNeighbor(first);
	}
	
	public void CreateLinkage(List<Tile> list, int max) throws Exception {
		ArrayList<Tile> choices = new ArrayList<Tile>(list.size());
		for(Tile tile : list) {
			if(tile.IsOnBoard()) {
				choices.add(tile);
			}
		}
		Tile joiner;
		Tile candidate;
		int index;
		while(tileDeck.size() > 0) {
			//Pick random tile from tile deck to add to map.
			index = random.nextInt(tileDeck.size());
			joiner = tileDeck.get(index);
			
			//Pick random tile from list of choices to consider as parent.
			//We will abandon the choice and try a new one if it has too many neighbors already.
			candidate = choices.get(random.nextInt(choices.size()));
			while(candidate.neighbors.length >= max) {
				choices.remove(candidate);
				if(choices.size() <= 0) {
					throw new Exception("Exception: Ran out of choices for building linkage from in Generator.CreateLinkage().");
				}
				candidate = choices.get(random.nextInt(choices.size()));
			}
			
			//Link tiles together.
			LinkTiles(joiner, candidate);
			//Remove joined tile from tile deck. Add it to game board list.
			tileDeck.remove(joiner);
			list.add(joiner);
			//Update list of choices for parent to join new tiles to.
			if(!choices.contains(joiner)) {
				choices.add(joiner);
			}
			if(candidate.neighbors.length >= max) {
				choices.remove(candidate);
			}
		}
	}
	
	public void UpdateVisibility(List<Tile> list) {
		//Communicator.message.add("Update Visibility: list size: "+String.valueOf(list.size()));
		for(Tile first : list) {
			for(Tile second : first.neighbors) {
				UpdateVisibility(first, second);
			}
		}
	}
	
	public static void UpdateVisibility(Tile first, Tile second) {
		if(first == second) {
			return;
		}else {
			//Communicator.message.add("Visibility pair: t1 = "+first.name+", t2 = "+second.name);
			UpdateVisibilityPair(first, second);
			UpdateVisibilityPair(second, first);
		}
	}
	
	public static void UpdateVisibilityPair(Tile first, Tile second) {
		//String message = "Update visibility: t1 = "+first.name+": "+Tile.StateString(first.state)
		//+", t2 = "+second.name+": "+Tile.StateString(second.state)+".";
		
		//message += " [";
		if(!(first.state == TileState.outOfPlay)) {
			//message += "A";
			if(second.PropagatesVisibility()) {
				//message += ",B";
				if(first.state == TileState.hidden) {
					//message += ",C";
					first.state = TileState.explorable;
					//message += " Changed first to "+Tile.StateString(first.state);
				}
			}
		}
		//message += "]";
		
		//Communicator.message.add(message);
	}

	
	
	private void AssignTileResources(Tile tile) {
		//Explore cost and reward.
		//tile.exploreCost.put(Generator.resourceNames[0], 1);	//Explore cost should always be zero?
		Tile.AddResource(tile.exploreGain, Generator.resourceNames[0], random.nextInt(4)+1);
		
		//Randomly assign a world type.
		int roll = random.nextInt(5);
		if(roll <= 1) {//This is a score world.
			//Claim cost and reward.
			int score = random.nextInt(3);
			int scoreCost = 0;
			for(int i=0;i<score;i++) {
				scoreCost += random.nextInt(2)+1;
			}
			Tile.AddResource(tile.claimCost, Generator.resourceNames[2], scoreCost);
			Tile.AddResource(tile.claimGain, Generator.resourceNames[1], score+2);
		}else {//This is a goods world.
			//Claim cost and reward.
			int goods = random.nextInt(3);
			int goodsCost = 0;
			for(int i=0;i<goods;i++) {
				goodsCost += random.nextInt(2)+1;
			}
			Tile.AddResource(tile.claimCost, Generator.resourceNames[0], goodsCost);
			Tile.AddResource(tile.claimGain, Generator.resourceNames[2], goods);
			Tile.AddResource(tile.claimGain, Generator.resourceNames[1], 1);
		}
		
		
		
		
		
		//Recurring reward and interval.
		Tile.AddResource(tile.recurringGain, Generator.resourceNames[2], 1);
		tile.SetRecurringInterval(random.nextInt(12)+3);
	}
	
	public static int Random(int max) {
		return Random(0, max);
	}
	
	public static int Random(int min, int max) {
		return random.nextInt((max-min))+min;
	}
	
}
