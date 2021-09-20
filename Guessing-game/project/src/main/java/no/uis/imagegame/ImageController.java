package no.uis.imagegame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.engine.jdbc.connections.internal.UserSuppliedConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import no.uis.players.AIPlayer;
import no.uis.players.Player;
import no.uis.players.User;
import no.uis.imagegame.Game;
import java.util.Random;


@Controller
public class ImageController {
	Player waitingPlayer;
	HashMap<String, Player> activePlayers = new HashMap<String, Player>();
	ArrayList<Game> activeGames = new ArrayList<>();
	
	TreeSet<User> allUsersName = User.getAllUsersName();
	TreeSet<User> allUsersScore = User.getAllUsersScore();
	
	//Load list of images in my scattered_images folder
	@Value("classpath:/static/images/scattered_images/*")
	private Resource[] resources;
	
	//Initialise label reader, map labels/images
	ImageLabelReader labelReader = new ImageLabelReader("src/main/resources/static/label/label_mapping.csv",
			"src/main/resources/static/label/image_mapping.csv");
	
	@GetMapping("/")
	public String loginScreen(HttpServletResponse response) {
		Cookie cookie = new Cookie("username", "");
		response.addCookie(cookie);
		return "login";
	}
	
	@ResponseBody
	@SuppressWarnings("rawtypes")
	@PostMapping("/gameLogin") // simple login - no authentication, can add authentication later. 
	public HashMap<String, Object> gameLogin(@RequestParam(value="username", required = false) String username, 
			@RequestParam(value="password", required = false) String password, HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String,Object>();
		if (username.isEmpty()) {
			map.put("error", "Empty username");
		} else {
			try {
				User currentUser = User.getUser(username);
				if (currentUser.authenticateUser(password)) {
					Player newPlayer = new Player(currentUser, Player.PlayerType.GUESSER);
					Cookie cookie = new Cookie("username", username);
					response.addCookie(cookie);
					map.put("username", username);
			
					if (waitingPlayer != null) {
						Player player1 = waitingPlayer;
						waitingPlayer = null;
						newPlayer.setPlayerType(Player.PlayerType.PROPOSER);
						activeGames.add(new Game(player1, newPlayer));
						activePlayers.put(player1.getUsername(), player1);
						activePlayers.put(newPlayer.getUsername(), newPlayer);	
						map.put("queuing", "falsey");
					} else {
						waitingPlayer = newPlayer;
						map.put("queuing", "true");
					}
				}
			} catch (Exception e) {
				
			}
		}
		return map;
	}
	
	public String randomPictureID() {
		Random random = new Random();
		ArrayList<String> allImageLabels = getAllLabels(labelReader);
		int randomNumber = random.nextInt(allImageLabels.size());
		String randomPictureID = allImageLabels.get(randomNumber);
        return randomPictureID;
    }
	
	@SuppressWarnings("rawtypes")
	@ResponseBody
	@GetMapping("/startAI") // //Start AI player
	public HashMap<String, Object> startAI(@CookieValue(value = "username", defaultValue = "na") String username, 
			HttpServletResponse response) {				
		HashMap<String, Object> map = new HashMap<String,Object>();			
		try {
			if (waitingPlayer.getUsername().equals(username)) {
				Player newPlayer = new AIPlayer();
				Player player1 = waitingPlayer;
				waitingPlayer = null;
				newPlayer.setPlayerType(Player.PlayerType.PROPOSER);
				activeGames.add(new Game(player1, newPlayer));
				activePlayers.put(player1.getUsername(), player1);
				newPlayer.getGame().startGame(randomPictureID()); //insert random pictureId here
				map.put("queuing", "false");
			}
		} catch (Exception e) {
			map.put("error", e.getMessage());
		}
		return map; // view
	}
	
	@GetMapping("/game")
	public String showImage(Model model,
			@RequestParam(value = "selectedlabel", required = false, defaultValue = "cinema") String name, 
			@CookieValue(value = "username", defaultValue = "na", required = false) String username, 
			HttpServletResponse response) {
		if (username != "na" && activePlayers.containsKey(username)) {
			if (activePlayers.get(username).getGame().getGameStatus() == Game.GameStatus.PLAYING) {
				name = activePlayers.get(username).getGame().getPictureID();
			}
			String[] files = labelReader.getImageFiles(name);
			String image_folder_name = getImageFolder(files);
			ArrayList<String> imageLabels = getAllLabels(labelReader);
			model.addAttribute("listlabels", imageLabels);
			model.addAttribute("selected", name);
			ArrayList<String> images = new ArrayList<String>();
			for (int i = 0; i < 49; ++i) {
				images.add("images/scattered_images/" + image_folder_name + "/" + i + ".png");
			}
			model.addAttribute("listimages", images);
		}
		return "gameScreen"; // view
	}

	private String getImageFolder(String[] files) {
		String image_folder_name = "";
		for (String file : files) {
			String folder_name = file + "_scattered";
			for (Resource r : resources) {

				if (folder_name.equals(r.getFilename())) {
					image_folder_name = folder_name;
					break;
				}
			}
		}
		return image_folder_name;
	}

	@SuppressWarnings("rawtypes")
	@GetMapping(path = "/gameUpdate", produces = "application/json;charset=UTF-8")
	@ResponseBody
	public Map getGameUpdate(@CookieValue(value = "username", defaultValue = "na") String username) {
		HashMap<String, Object> map = new HashMap<String,Object>();
		if (username != "na") {
			if (waitingPlayer != null && waitingPlayer.getUsername().equals(username)) {
				map.put("queuing", "true");
			} else {
				if (activePlayers.containsKey(username)) {
					map = activePlayers.get(username).getGame().getGameUpdate(activePlayers.get(username));
					map.put("queuing", "false");
				}
			}		
		}
		return map;		
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/sendUpdate") // choices from players. 
	@ResponseBody
	public HashMap<String, Object> sendUpdate(@RequestParam(value="", required = false) String segment, 
			@RequestParam(value="", required = false) String guess, @RequestParam(value="", required = false) String pictureId,
			@CookieValue(value = "username", defaultValue = "na") String username, 
			HttpServletResponse response) {
		HashMap<String, Object> map = new HashMap<String,Object>();
		if (username != "na") {
			if (activePlayers.containsKey(username)) {				
				Player player = activePlayers.get(username);
				if (player.getUsername().equals(username)) {
					map = player.getGame().getGameUpdate(player);
					map.put("queuing", "false");
					if (player.getPlayerType() == Player.PlayerType.GUESSER) {
						map.put("makeGuess", player.getGame().guess(guess));
					} else if (player.getPlayerType() == Player.PlayerType.PROPOSER) {
						if (player.getGame().getGameStatus() == Game.GameStatus.WAITING) {
							player.getGame().startGame(pictureId);
						} else if (player.getGame().getGameStatus() == Game.GameStatus.PLAYING) {
							map.put("chooseSegment", player.getGame().chooseSegment(Integer.parseInt(segment)));
						} 
					}
				}	
			}
		}
		return map;
	}
	
	private ArrayList<String> getAllLabels(ImageLabelReader ilr) {
		ArrayList<String> labels = new ArrayList<String>();
		for (Resource r : resources) {
			String fileName = r.getFilename();
			String fileNameCorrected = fileName.substring(0, fileName.lastIndexOf('_'));
			String label = ilr.getLabel(fileNameCorrected);
			labels.add(label);
		}
		return labels;
	}

	@GetMapping("/leaderboard") // Gets the leaderboard with parameter k as top k players
	public String getLeaderboard(Model model, @RequestParam(value = "k", required = false, defaultValue = "3") int topNumber, HttpServletResponse response) {
			
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Integer> scores = new ArrayList<Integer>();
		
		for (HashMap.Entry<String, User> entry : User.getUsers().entrySet()) {
			names.add(entry.getValue().getName());
			scores.add(entry.getValue().getScore());
		}
		model.addAttribute("names", names);
		model.addAttribute("scores", scores);
				
		return "leaderboard";
	}
}
