package sfs2x.client.examples;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import redis.clients.jedis.Jedis;
import sfs2x.client.SmartFox;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.util.ConfigData;

public class SFS2XConnector {
	private final SmartFox sfs;
	private final String userName;
	private Jedis jedis = new Jedis("redis://localhost:6379");
	private Connection conn;

	public SFS2XConnector(String userName) throws ClassNotFoundException, SQLException {
		// Configure client connection settings
		ConfigData cfg = new ConfigData();
		cfg.setHost("localhost");
		cfg.setPort(9933);
		cfg.setZone("IndianGamer");
		cfg.setDebug(false);

		this.userName = userName;
		// Set up event handlers
		sfs = new SmartFox();
		sfs.addEventListener(SFSEvent.CONNECTION, evt -> {
			boolean success = (boolean) evt.getArguments().get("success");
			if (success) {
				System.out.println("Connection success");
				sfs.send(new LoginRequest(userName));
			} else {
				System.out.println("Connection Failed. Is the server running?");
			}
		});
		sfs.addEventListener(SFSEvent.CONNECTION_LOST, evt -> {
			System.out.println("-- Connection lost --");
		});
		sfs.addEventListener(SFSEvent.LOGIN, evt -> {
			System.out.println("Logged in as: " + sfs.getMySelf().getName());
			sfs.send(new JoinRoomRequest("The Lobby"));
		});
		sfs.addEventListener(SFSEvent.LOGIN_ERROR, evt -> {
			String message = (String) evt.getArguments().get("errorMessage");
			System.out.println("Login failed. Cause: " + message);
		});
		sfs.addEventListener(SFSEvent.ROOM_JOIN, evt -> {
			Room room = (Room) evt.getArguments().get("room");
			System.out.println("Joined Room: " + room.getName());
		});
		sfs.addEventListener(SFSEvent.ADMIN_MESSAGE, baseEvent -> {
			System.out.println("Message from Server Admin: " + baseEvent.getArguments().get("message"));
		});
		sfs.connect(cfg);
		connect();
	}

	// connect Java to Database
	private void connect() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Redis", "root", "root");
	}

	// insert user data in database table
	public void putData() throws SQLException, JsonProcessingException {
		UserModel user = new UserModel(userName, LocalDate.now(), LocalTime.now());
		PreparedStatement preparedState = conn.prepareStatement(
				"insert into userData (user_name, Date,Time) values (?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
		preparedState.setString(1, user.getUsername());
		preparedState.setDate(2, Date.valueOf(user.getDate()));
		preparedState.setTime(3, Time.valueOf(user.getTime()));
		preparedState.execute();

		ResultSet resultSet = preparedState.getGeneratedKeys();
		if (resultSet.next()) {
			int userId = resultSet.getInt(1);
			user.setUserId(userId);
			ObjectMapper mapper = getObjectMapper();
			jedis.set(String.valueOf(userId), mapper.writeValueAsString(user)); // set data on Redis and key is userId
		}
		System.out.println("Success");
	}

	public ObjectMapper getObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addDeserializer(LocalDateTime.class,
				new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
		mapper.registerModule(javaTimeModule);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		return mapper;
	}
	
	public void showAllRecords() throws JsonMappingException, JsonProcessingException {
		Set<String> keys = jedis.keys("*");
		List<UserModel> users = new ArrayList<>();
		for (String key : keys) {
			String obj = jedis.get(key);
			ObjectMapper objectMapper = getObjectMapper();
			UserModel user = objectMapper.readValue(obj, UserModel.class);
			users.add(user);
		}
		System.out.println("Total users : " + users.size());
		users.stream().forEach(System.out::println);
	}
}