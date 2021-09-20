package no.uis.players;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import no.uis.imagegame.PasswordHasher;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User implements Serializable {
	private static final long serialVersionUID = -3461894945978508021L;

	public static HashMap<String, User> getUsers() {
		return users;
	}

	private static HashMap<String, User> users = new HashMap<String, User>();
	
	@Id
	private String username;
	private String name;
	private String passwordHash;
	private int score;
	private String password; 
	
	private static TreeSet<User> allUsersName = new TreeSet<>(new TheComparatorName()); 
	private static TreeSet<User> allUsersScore = new TreeSet<>(new TheComparatorScore());

	public User(String username, String name, String password) throws IllegalArgumentException {
		if (checkUsernameFree(username)) {
			this.username = username;
			this.name = name;
			this.score = 0;
			try {
				this.passwordHash = PasswordHasher.generateStorngPasswordHash(password);
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.password = password;
			
			users.put(username, this);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	public boolean checkUsernameFree(String username) {
		return (!users.containsKey(username));
	}
	
	public boolean authenticateUser(String password) throws NoSuchElementException, IllegalArgumentException {
		try {
			if (this.password.equals(password)) {
				return true;				
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
	}
	
	public static User getUser(String username) throws NoSuchElementException{
		if (users.containsKey(username)) {
			return users.get(username);
		} else {
			throw new NoSuchElementException();
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		updateAllUsers();
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
		updateAllUsers();
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
		saveUsers();
	}
	
	public void addScore(int score) {
		this.score += score;
		saveUsers();
	}
	
	public static void updateAllUsers() {
		for (HashMap.Entry<String, User> entry : users.entrySet()) {
			allUsersName.add(entry.getValue());
		}
	}
	
	public static void updateHighscore() {
		for (HashMap.Entry<String, User> entry : users.entrySet()) {
			allUsersScore.add(entry.getValue()); //Dosen't work properly, use data tables 
		}
	}

	@Override
	public String toString() {
		return "User [Name = " + name + ", ID = " + username + "] Score: " + this.score;
	}
	public static void saveUsers() {
		try {
			FileOutputStream fos = new FileOutputStream(new File("users.txt"));
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			for (HashMap.Entry<String, User> entry : users.entrySet()) {
				oos.writeObject(entry.getValue());
			}
			oos.close();
			fos.close();

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	public static void loadUsers() {
		try {
			FileInputStream fis = new FileInputStream(new File("users.txt"));
			ObjectInputStream ois = new ObjectInputStream(fis);
			boolean cont = true;
	
			while(cont) {
				// Read objects
				User user = (User) ois.readObject();
				if(user != null) {
					users.put(user.getUsername(), user);
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
	
	public static TreeSet<User> getAllUsersName() {
		return allUsersName;
	}
	
	public static TreeSet<User> getAllUsersScore() {
		allUsersScore.clear();
		Iterator<User> it1 = allUsersName.iterator();
		ArrayList<User> allUsers = new ArrayList<>();
		while(it1.hasNext()) {
			allUsers.add(it1.next());
		}
		for (User user : allUsers) {
			allUsersScore.add(user);
		}
		return allUsersScore;
	}
	
	public static HashMap<String, User> getAllUsers() {
		return users;
	}
}

class TheComparatorName implements Comparator<User> {
	@Override
	public int compare(User o1, User o2) {
		String name1 = o1.getName();
		String name2 = o2.getName();
		return name1.compareTo(name2);
	}
}

class TheComparatorScore implements Comparator<User> {
	@Override
	public int compare(User o1, User o2) {
		int score1 = o1.getScore();
		int score2 = o2.getScore();
		return score2 - score1;
	}
}