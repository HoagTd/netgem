package tilegame.john.powers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



/**
 * Servlet implementation class TileServlet
 */
@WebServlet("/TileServlet")
public class TileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Object lock = new Object();
	
	private static ArrayList<TileGame> games;
	private Communicator communicator;
	public static ArrayList<Highscore> scores;
	//private Timer timer;
	
	private int gameTurnLimit = 50;
	private int tileCount = 50;
	private int maxLinks = 5;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TileServlet() {
        super();
    }

    public void init() throws ServletException {
    	/* Some helpful advice from: http://www.eg.bucknell.edu/~mead/Java-tutorial/servlets/lifecycle/init.html
    	If an initialization error occurs that renders the servlet incapable of 
		handling client requests, throw an UnavailableException. 
		Do not call the System.exit method.
	 	Save the ServletConfig parameter so that the getServletConfig method can return the value. 
    	 */
        
    	
    	synchronized(lock) {
    		//Create list of games.
    		games = new ArrayList<TileGame>();
	    	//Create highscore table.
	    	scores = new ArrayList<Highscore>();
	        //Create communicator.
	        communicator = new Communicator();
	    	//Create and start the timer.
	        //timer = new Timer(this);
	        //timer.run();
    	}
     }
    
    public void destroy() {
    	//For advice once it is time to implement this consider:
    	//http://www.eg.bucknell.edu/~mead/Java-tutorial/servlets/lifecycle/destroy.html
    	
        // do nothing.
    	
     }
    
    
    
    public String getServletInfo() {
    	return "This servlet lets you play a tile-game. By JS, 2018.";
    }
    
    
    /*
    private class Timer implements Runnable{

    	public TileServlet boss;
    	public boolean live;
    	private long timeToSleep;
    	private long gameDeletionTime;
    	
    	Timer(TileServlet servlet){
    		boss = servlet;
    		live = true;
    		timeToSleep = 60000;
    		gameDeletionTime = 86400000*2;//Two days.
    	}
    	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(live == true) {
				
				synchronized(lock) {
					long time = Time.now();
					long delta;
					for(TileGame game : games) {
						delta = time - game.accessed;
						if(delta > gameDeletionTime) {
							game.EndGame();
						}
					}
				}
				
				try {
					Thread.sleep(timeToSleep);
				} catch (InterruptedException e) {
					live = false;
					e.printStackTrace();
				}
			}
			
		}
		
		
    }
    */
    
    
    
    /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		synchronized(lock) {
			//Start by figuring out which user this is and what they want to do.
			HttpSession session = request.getSession();
			String user = session.getId();
			long accessTime = session.getLastAccessedTime();
			
			String tileIdString = request.getParameter("ID");
			if(tileIdString == null) {
				tileIdString = "";
			}
			String commandString = request.getParameter("Command");
			if(commandString == null) {
				commandString = "";
			}
			String nickString = request.getParameter("Nick");
			if(nickString == null) {
				nickString = "";
			}
			
			TileGame game;
			
			//Does this user have a game already running?
			game = null;
			for(TileGame stored : games) {
				if(stored.owner.equals(user)) {
					game = stored;
					break;
				}
			}
			
			//If no, we create a game for the user.
			if(game == null) {
				nickString = CleanNickname(nickString);
				//Sanity check values.
				if(nickString.length() > 0) {
					game = StartGame(user, accessTime, nickString);
				}
			}else
			
			//If yes, see if this is a command.
			if(commandString.equals("NewGame") && (!(commandString.equals("")))) {
				nickString = CleanNickname(nickString);
				//Sanity check values.
				if(nickString.length() > 0) {
					game.EndGame();
					game = StartGame(user, accessTime, nickString);
				}
			}else if((tileIdString != null) && (!(tileIdString.equals("")))) {
				int headID = Integer.valueOf(tileIdString);
				game.ClickTile(accessTime, headID);
			}
			
			
			
			
			//Handle the response headers, etc.
			//Header data can not be altered once writer is accessed.
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/xml");
			response.setCharacterEncoding("UTF-8");
			
			//Get the message we are to write.
			String message = "";
			if(game != null) {
				message = communicator.StateToMessage(game);
			}else {
				message = communicator.StateToMessage();
			}
			
			
			//Write the message body.
			PrintWriter body = response.getWriter();
			
			body.println(message);
			
			//Write the message out of the buffer.
			body.flush();//No changes to message body can be made after this command.
			body.close();
		}
	}

	public String CleanNickname(String nick) {
		String nickString = new String(nick);
		
		nickString = nickString.trim();
		nickString = nickString.replaceAll("/[^A-Za-z0-9]/", "");
		nickString = nickString.trim();
		
		return nickString;
	}
	
	public TileGame StartGame(String user, long accessTime, String nickString) {
		synchronized(lock) {
			String log = "New game command detected.";
			
			//If the nickname is of valid length.
			if(nickString.length() > 0) {
				log += "Starting a game as "+nickString;
				//Create game.
		        try {
					TileGame game = new TileGame(user, accessTime, nickString, gameTurnLimit, tileCount, maxLinks);
					games.add(game);
					Communicator.message.add(log);
					return game;
				} catch (Exception e) {
					log += "ERROR: Unable to create game!";
					e.printStackTrace();
				}
			}else {
				log += "ERROR: Invalid nickname!";
			}
			
	        Communicator.message.add(log);
	        return null;
		}
	}
	
	
	public static void RegisterHighscore(Highscore score, TileGame originator) {
		synchronized(lock) {
			scores.add(score);
			Collections.sort(scores);
			games.remove(originator);			
		}
	}
	
	public static Highscore[] GetHighscores() {
		synchronized(lock) {
			Highscore[] result = new Highscore[scores.size()];
			return scores.toArray(result);
		}
	}
	
	
	
}
