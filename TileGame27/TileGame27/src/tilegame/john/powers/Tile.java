package tilegame.john.powers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Tile {

	public enum TileState{
		outOfPlay, hidden, explorable, claimable, claimed
	}
	
	public static String StateString(TileState state) {
		switch(state) {
			case outOfPlay:
				return "Out of play";
			case hidden:
				return "Hidden";
			case explorable:
				return "Explorable";
			case claimable:
				return "Claimable";
			case claimed:
				return "Claimed";
			default:
				return "ERROR: Unable to determine TileState string.";
		}
	}
	
	public TileGame game;
	public Tile[] neighbors;
	
	public TileState state;
	public final int identifier;
	public final String name;
	public int timeToProduce;
	
	Map<String, Integer> exploreCost;
	Map<String, Integer> exploreGain;
	Map<String, Integer> claimCost;
	Map<String, Integer> claimGain;
	Map<String, Integer> recurringGain;
	private int recurringInterval;
	
	public Tile(TileGame g, int id, String na) {
		game = g;
		neighbors = new Tile[0];
		state = TileState.outOfPlay;
		identifier = id;
		name = na;
		
		exploreCost = new HashMap<String, Integer>();
		exploreGain = new HashMap<String, Integer>();
		claimCost = new HashMap<String, Integer>();
		claimGain = new HashMap<String, Integer>();
		recurringGain = new HashMap<String, Integer>();
		recurringInterval = 1;
		
		timeToProduce = recurringInterval;
	}
	
	public void SetRecurringInterval(int period) {
		int delta = period - recurringInterval;
		recurringInterval = period;
		timeToProduce = timeToProduce + delta;
	}
	
	public int GetRecurringInterval() {
		return recurringInterval;
	}
	
	public boolean IsOnBoard() {
		if(state == TileState.outOfPlay) {
			return false;
		}
		if(state == TileState.hidden) {
			return false;
		}
		return true;
	}
	
	public boolean IsChoice() {
		if((state == TileState.outOfPlay) || (state == TileState.hidden)
				|| (state == TileState.claimed)) {
			return false;
		}else {
			return true;
		}
	}
	
	public boolean IsVisible() {
		if(state == TileState.outOfPlay) {
			return false;
		}
		if(state == TileState.hidden) {
			return false;
		}
		return true;
	}
	
	public boolean PropagatesVisibility() {
		if(state == TileState.claimable) {
			return true;
		}
		if(state == TileState.claimed) {
			return true;
		}
		return false;
	}
	
	public boolean IsNeighbor(Tile other) {
		if(state == TileState.outOfPlay) return false;
		for(Tile tile : neighbors) {
			if(tile == other) {
				return true;
			}
		}
		return false;
	}
	
	public boolean IsClickable() {
		if(state == TileState.explorable) {
			return true;
		}
		if(state == TileState.claimable) {
			return true;
		}
		return false;
	}
	
	public void AddNeighbor(Tile other) {
		if(state == TileState.outOfPlay) return;
		if(IsNeighbor(other) == false) {
			Tile[] expansion = new Tile[neighbors.length+1];
			int i;
			for(i=0;i<neighbors.length;i++) {
				expansion[i] = neighbors[i];
			}
			expansion[i] = other;
			neighbors = expansion;
		}
	}
	
	public void RemoveNeighbor(Tile other) {
		if(state == TileState.outOfPlay) return;
		if(IsNeighbor(other)) {
			Tile[] shrink = new Tile[neighbors.length-1];
			for(int j=0, index = 0;j<neighbors.length;j++) {
				if(neighbors[j] != other) {
					shrink[index++] = neighbors[j];
				}
			}
			neighbors = shrink;
		}
	}

	public boolean Click() {
		if(Explore()) {
			return true;
		}else if(Claim()){
			return true;
		}else {
			return false;
		}
	}
	
	public boolean Explore() {
		if(state == TileState.explorable) {
			if(game.CanPayResource(exploreCost)) {
				state = TileState.claimable;
				
				Set<String> keys;
				
				keys = exploreGain.keySet();
				//Set<String> keys = recurringGain.keySet();
				for(String key : keys) {
					game.AddResource(key, exploreGain.get(key));
				}
				exploreGain.clear();
				
				keys = exploreCost.keySet();
				//Set<String> keys = recurringGain.keySet();
				for(String key : keys) {
					game.PayResource(key, exploreCost.get(key));
				}
				exploreCost.clear();
				
				UpdateNeighbors();
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
	}
	
	public boolean Claim() {
		if(state == TileState.claimable) {
			if(game.CanPayResource(claimCost)) {
				state = TileState.claimed;
				
				Set<String> keys;
				
				keys = claimGain.keySet();
				//Set<String> keys = recurringGain.keySet();
				for(String key : keys) {
					game.AddResource(key, claimGain.get(key));
				}
				claimGain.clear();
				
				keys = claimCost.keySet();
				//Set<String> keys = recurringGain.keySet();
				for(String key : keys) {
					game.PayResource(key, claimCost.get(key));
				}
				claimCost.clear();
				
				return true;
				}else {
				return false;
			}
		}else {
			return false;
		}
	}
	
	public void UpdateNeighbors() {
		for(Tile other : neighbors) {
			Generator.UpdateVisibility(this, other);
		}
	}
	
	
	
	
	public static String[] getResourceStrings(Map<String, Integer> map) {
		Set<String> keys = map.keySet();
		String[] result = new String[map.size()];
		return keys.toArray(result);
	}

	public static int[] getResourceInts(Map<String, Integer> map) {
		Set<String> keys = map.keySet();
		int[] result = new int[map.size()];
		int index = 0;
		for(String key : keys) {
			result[index++] = map.get(key);
		}
		return result;
	}
	
	public static boolean AddResource(Map<String, Integer> map, String name, int add) {
		if(add > 0) {
			if(map.containsKey(name)) {
				int old = map.get(name);
				add += old;
			}
			map.put(name, add);
			return true;
		}else {
			return false;
		}
	}


	public void AdvanceTime() {
		timeToProduce--;
		if(timeToProduce <= 0) {
			timeToProduce = recurringInterval;
			
			Set<String> keys = recurringGain.keySet();
			if(this.state == TileState.claimed) {//IF the tile is claimed, deliver resources to player.
				for(String key : keys) {
					game.AddResource(key, recurringGain.get(key));
				}
			}else {//Otherwise, deliver resources to tile's resource pool.
				for(String key : keys) {
					Tile.AddResource(claimGain, key, recurringGain.get(key));
					IncreaseCost(key, recurringGain.get(key));
				}
			}
			
		}
		
	}

	private void IncreaseCost(String rname, Integer rcount) {
		int index = 0;
		for(int i = 0; i < Generator.resourceNames.length; i++) {
			if(rname.equals(Generator.resourceNames[i])) {
				index = i;
			}
		}
		int cost = Generator.Random(Generator.resourceBaseCosts[index]);
		if(cost > 0) {
			Tile.AddResource(claimCost, Generator.resourceNames[0], cost);
		}
	}
	
}
