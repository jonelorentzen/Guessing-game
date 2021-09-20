package no.uis.players;

import no.uis.imagegame.Game;

public class Player {
	private Long id;
	private String username;
	private PlayerType type;
	private PlayerStatus status;
	private Game currentGame;
	private User user;
	public enum PlayerType {
		GUESSER, PROPOSER
	};

	public enum PlayerStatus {
		WAITING, PLAYING, FINISHED
	};

	protected Player() {
	}

	public Player(String username, PlayerType pType) {
		this.setUsername(username);
		this.setPlayerType(pType);
		this.setPlayerStatus(PlayerStatus.WAITING);
	}

	public Player(User user, PlayerType pType) {
		this.setUsername(user.getUsername());
		this.setPlayerType(pType);
		this.setPlayerStatus(PlayerStatus.WAITING);
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public PlayerType getPlayerType() {
		return type;
	}

	public void setPlayerType(PlayerType playerType) {
		this.type = playerType;
	}

	public int getScore() {
		int score;
		try {
			score = user.getScore();
		} catch (Exception e) {
			score = 0;
		}
		return score;
	}

	public void addScore(int score) {
		try {
			user.addScore(score);
		} catch (Exception e) {
			// if player is not tied to a user, none is given any points
		}
	}

	public PlayerStatus getPlayerStatus() {
		return getStatus();
	}

	public void setPlayerStatus(PlayerStatus status) {
		this.setStatus(status);
	}
	
	public Game getGame() {
		return currentGame;		
		
	}
	public void setGame (Game currentGame) {
		this.currentGame = currentGame;
	}

	public PlayerStatus getStatus() {
		return status;
	}

	public void setStatus(PlayerStatus status) {
		this.status = status;
	}
	
}