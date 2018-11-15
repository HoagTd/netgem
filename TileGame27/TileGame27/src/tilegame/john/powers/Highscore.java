package tilegame.john.powers;

public class Highscore implements Comparable<Highscore>{

	public String owner;
	public int score;
	
	Highscore(String o, int s){
		owner = o;
		score = s;
	}


	@Override
	public int compareTo(Highscore other) {
		if(this.score > other.score) {
			return +1;
		}
		if(this.score < other.score) {
			return -1;
		}
		return 0;
	}
	
}
