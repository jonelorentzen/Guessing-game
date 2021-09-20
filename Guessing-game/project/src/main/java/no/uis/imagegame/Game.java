package no.uis.imagegame ;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import no.uis.players.Player;
import no.uis.players.User;
import no.uis.players.Player.PlayerStatus;


public class Game {
    
    private Player[] players = new Player[2];
    private int numGuess = 0;
    private String pictureID = "";
    private ArrayList<Integer> usedSegments = new ArrayList<Integer>();
    private GameStatus gameStatus;
    private GameResult gameResult;

    public Game(Player player1, Player player2) {
    	this.gameStatus = GameStatus.WAITING;
    	players[0] = player1;
    	players[1] = player2;
    	for (int i = 0; i < 2; i++) {
    		players[i].setGame(this);
    	}
    	player1.setStatus(PlayerStatus.WAITING);
    	player2.setStatus(PlayerStatus.PLAYING);    	          	
    	this.gameResult = GameResult.UNFINISHED;
    }
    
    public enum GameStatus {
		WAITING, PLAYING, FINISHED
	};

	public enum GameResult {
		UNFINISHED, LOST, WON
	};
	
	public void setGameStatus(GameStatus Status) {
        this.gameStatus = Status;
    } 
	
	public GameStatus getGameStatus() {
		return gameStatus;
	}

    public String getPictureID() {
        return pictureID;
    }

    public void setPictureID(String pictureID) {
        this.pictureID = pictureID;
    } 
    
    public int getNumGuess() {
        return numGuess;
    }
    
    public ArrayList<Integer> getSegmentList() {
        return usedSegments;
    }
    
    public boolean guess(String guess) {
    	if (players[0].getPlayerStatus() != Player.PlayerStatus.PLAYING) {
    		return false;
    	} else {
	    	numGuess++;
	    	if (numGuess >= 3) {
	    		if (usedSegments.size() > 48) {
	    			this.gameStatus = GameStatus.FINISHED;
		    		this.gameResult = GameResult.LOST;
		    		players[0].setPlayerStatus(PlayerStatus.FINISHED);
		    		players[1].setPlayerStatus(PlayerStatus.FINISHED);
	    		}
	    		numGuess = 0;
	    		changePlayerStatus();
	    	}	    		
	    	if (guess.equals(pictureID)) {
	    		for (int i = 0; i < 2; i++) {
		    		players[i].setPlayerStatus(PlayerStatus.FINISHED);
	    			players[i].addScore(49 - usedSegments.size());
	    		}
	    		User.updateHighscore();
	    		this.setGameStatus(GameStatus.FINISHED);
	    		this.gameResult = GameResult.WON;
	    		SegmentStatistics.storeGameResult(this, false);
	    		return true;
	    	}
	        return false;
    	}
    }

    public boolean chooseSegment(int segmentID) {
    	if (players[1].getPlayerStatus() != Player.PlayerStatus.PLAYING) {
    		return false;
    	} else { 
	    	if (usedSegments.contains(segmentID))
	    		return false;
	    	usedSegments.add(segmentID);
	    	if (usedSegments.size() > 2) {
	    		changePlayerStatus();
	    	} else {
	    		players[1].setPlayerStatus(PlayerStatus.PLAYING);
	    	}
	        return true;
    	}
    }
    
    private void changePlayerStatus(){
    	if (players[0].getPlayerStatus() == PlayerStatus.PLAYING) {
    		players[0].setPlayerStatus(PlayerStatus.WAITING);
    		players[1].setPlayerStatus(PlayerStatus.PLAYING);	
    	}
    	else {
    		players[0].setPlayerStatus(PlayerStatus.PLAYING);
    		players[1].setPlayerStatus(PlayerStatus.WAITING);		
    	}	
    }
    
    public HashMap<String,Object> getGameUpdate(Player currentPlayer) {
		HashMap<String, Object> tmp = new HashMap<String,Object>();
		tmp.put("gameStatus", gameStatus.toString());
		tmp.put("segments", usedSegments);
		tmp.put("picture", pictureID.toString());
		tmp.put("playerRole", currentPlayer.getPlayerType().toString());
		tmp.put("playerActive", currentPlayer.getPlayerStatus().toString());
		tmp.put("guessNumber", numGuess);
		tmp.put("gameResult", gameResult);
		return tmp;
    }
   
    public void startGame(String pictureID) {
        setPictureID(pictureID);
        this.setGameStatus(GameStatus.PLAYING);
        players[0].setPlayerStatus(PlayerStatus.WAITING);
        players[1].setPlayerStatus(PlayerStatus.PLAYING);
    }
}