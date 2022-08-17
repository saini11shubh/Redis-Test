package sfs2x.client.examples;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class UserModel implements Serializable {

	private static final long serialVersionUID = 123160140364074050L;

	private int userId;
	private String username;
	private LocalDate date;
	private LocalTime time;

	public UserModel() {
		super();
	}

	public UserModel(String username, LocalDate date, LocalTime time) {
		super();
		this.username = username;
		this.date = date;
		this.time = time;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "UserModel [userId=" + userId + ", username=" + username + ", date=" + date + ", time=" + time + "]";
	}
	
	
}
