package no.uis.imagegame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class SegmentStatistics implements Serializable {
	private static final long serialVersionUID = -4656130393125332812L;
	// Stores game results based on PictureId
	private static HashMap<String, ArrayList<SegmentStatistics>> gameResults = new HashMap<String, ArrayList<SegmentStatistics>>();
	private static HashMap<String, SegmentStatistics> aiGameResults = new HashMap<String, SegmentStatistics>();

	private String pictureID;
    private ArrayList<Integer> usedSegments;
    public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	private int score;
	
    public SegmentStatistics(Game game, Boolean AIProposer) {
		this.usedSegments = game.getSegmentList();
		this.pictureID = game.getPictureID();    	
		this.score = game.getSegmentList().size();
    }
	public static void storeGameResult(Game game, Boolean AIProposer) {
		if (AIProposer) {
			if (aiGameResults.containsKey(game.getPictureID())) {
				if (aiGameResults.get(game.getPictureID()).score < game.getSegmentList().size()) {
					// add new SegmentStatistics 
				} else {
					// Old is better, do nothing
				}
			}
		} else {
			if (gameResults.containsKey(game.getPictureID())) {
				gameResults.get(game.getPictureID()).add(new SegmentStatistics(game, AIProposer));
			} else {
				ArrayList<SegmentStatistics> tmp = new ArrayList<SegmentStatistics>();
				tmp.add(new SegmentStatistics(game,AIProposer));
				gameResults.put(game.getPictureID(), tmp);  
			}
		}
		saveSegmentStatistics();
	}
	public static void saveSegmentStatistics() {
		try {
			FileOutputStream fos = new FileOutputStream(new File("gameResults.txt"));
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			for (Entry<String, ArrayList<SegmentStatistics>> entry : gameResults.entrySet()) {
				oos.writeObject(entry.getValue());
			}
			oos.close();
			fos.close();

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	public static ArrayList<Integer> getSegmentWeight(String pictureID) { 
		ArrayList<Integer> data = new ArrayList<Integer>();
		ArrayList<SegmentStatistics> gameDataSets = gameResults.get(pictureID);
		for (int i = 0; i < 49; i++) {
			int segmentSum = 0;
			for (SegmentStatistics gameData : gameDataSets) {
				if (gameData.usedSegments.contains(i)) {
					segmentSum += 1 / (gameData.usedSegments.size());
				}
			}
			data.add(i, segmentSum);
		}
		return data;
	}
	public static void loadSegmentStatistics() {
		try {
			FileInputStream fis = new FileInputStream(new File("gameResults.txt"));
			ObjectInputStream ois = new ObjectInputStream(fis);
			boolean cont = true;
	
			while(cont) {
				// Read objects
				ArrayList<SegmentStatistics> entry = (ArrayList<SegmentStatistics>) ois.readObject();
				if(entry != null) {
					gameResults.put(entry.get(0).toString(), entry);
				} else {
			    	cont = false;
				}
			}
			ois.close();
			fis.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		} 
	}
}
