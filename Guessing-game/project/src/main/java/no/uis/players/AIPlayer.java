package no.uis.players;

import java.util.Random;

import no.uis.imagegame.Game.GameStatus;

public class AIPlayer extends Player {
    Random random = new Random();

	public AIPlayer() {
		this.setUsername("AI Joe");
		this.setPlayerType(PlayerType.PROPOSER);
	}

	public void setPlayerStatus(PlayerStatus status) {
		if (status == PlayerStatus.PLAYING) {
			if (this.getGame().getGameStatus() == GameStatus.PLAYING) {
				int randomSegment;
				do {
					randomSegment = random.nextInt(49);
				} while (!this.getGame().chooseSegment(randomSegment));		
			}
		}
		this.setStatus(status);
	}
}