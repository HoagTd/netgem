package tilegame.john.powers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;



public class TileGame{
	
	public String owner;
	public long accessed;
	
	private Generator generator;
	private ArrayList<Tile> tiles;
	Map<String, Integer> resources;
	
	public String name;
	public int clock = 0;
	public int lastTurn;
	public int score = 0;
	
	public TileGame(String user, long accessTime, String n, int turnLimit, int tileCount, int maxLinks) throws Exception {
		owner = user;
		accessed = accessTime;
		name = n;
		lastTurn = turnLimit;
		score = 0;
		
		generator = new Generator();
		tiles = new ArrayList<Tile>();
		resources = new HashMap<String, Integer>();
		
		generator.CreateUniverse(this, tiles, tileCount, maxLinks);
		
		AddResource(Generator.resourceNames[0], 10);
	}

	public Tile[] GetVisibleTiles() {
		ArrayList<Tile> list = new ArrayList<Tile>();
		
		for(Tile tile : tiles) {
			if(tile.IsVisible()) {
				list.add(tile);
			}
		}
		
		Tile[] result = new Tile[list.size()];
		return list.toArray(result);
	}
	
	public boolean ClickTile(long accessTime, int id) {
		accessed = accessTime;
		if(clock >= lastTurn) {//If the turn limit has been past.
			Communicator.message.add("No more turns permitted in this game. Start a new game.");
			EndGame();
			return false;
		}else {
			boolean result = false;
			for(Tile tile : tiles) {
				if(tile.identifier == id) {
					if(tile.Click()) {
						result = true;
						break;
					}
				}
			}
			if(result == true) {
				AdvanceTime();
			}
			return result;
		}
	}

	public String[] getResourceStrings() {
		Set<String> keys = resources.keySet();
		String[] result = new String[resources.size()];
		return keys.toArray(result);
	}

	public int[] getResourceInts() {
		Set<String> keys = resources.keySet();
		int[] result = new int[resources.size()];
		int index = 0;
		for(String key : keys) {
			result[index++] = resources.get(key);
		}
		return result;
	}
	
	public boolean AddResource(String name, int add) {
		if(add > 0) {
			if(Generator.IsScore(name)) {
				score += add;
			}else {
				if(resources.containsKey(name)) {
					int old = resources.get(name);
					add += old;
				}
				resources.put(name, add);
			}
			return true;
		}else {
			return false;
		}
	}
	
	public boolean PayResource(String name, int pay) {
		if(Generator.IsScore(name)) {
			score -= pay;
			return true;
		}else {
			if(resources.containsKey(name)) {
				int old = resources.get(name);
				if(old >= pay) {
					old = old - pay;
				}
				if(old == 0) {
					resources.remove(name);
				}else {
					resources.put(name, old);
				}
				return true;
			}else {
				return false;
			}
		}
	}
	
	public boolean CanPayResource(Map<String, Integer> cost) {
		Set<String> keys = cost.keySet();
		for(String key : keys) {
			if(CanPayResource(key, cost.get(key)) == false) {
				return false;
			}
		}
		return true;
	}
	
	public boolean CanPayResource(String name, int pay) {
		if(pay <= 0) {//Cost of zero or less is always payable.
			return true;
		}
		//Otherwise we need at least as much of the resource as pay.
		if(resources.containsKey(name)) {
			int old = resources.get(name);
			if(old >= pay) {
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
	}
	
	public void AdvanceTime() {
		clock++;
		for(Tile tile : tiles) {
			tile.AdvanceTime();
		}
		if(clock > lastTurn) {
			EndGame();
		}
	}

	public void EndGame() {
		TileServlet.RegisterHighscore(new Highscore(name, score), this);
	}

	
	
}
