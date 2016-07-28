package lt.ekgame.storasbot;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.security.auth.login.LoginException;

import org.tillerino.osuApiModel.Downloader;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lt.ekgame.storasbot.commands.engine.CommandListener;
import lt.ekgame.storasbot.plugins.AntiShitImageHosts;
import lt.ekgame.storasbot.plugins.BanchoStatusChecker;
import lt.ekgame.storasbot.plugins.BeatmapLinkExaminer;
import lt.ekgame.storasbot.plugins.osu_top.OsuTracker;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

public class StorasBot {
	
	public static JDA client;
	public static Database database;
	public static Config config;
	public static Downloader osuApi;
	public static CommandListener commandHandler;
	public static List<String> operators = new ArrayList<>();
	
	public static void main(String... args) throws SQLException, LoginException, IllegalArgumentException {
		config = ConfigFactory.parseFile(new File(args[0])); // Very important that this is first
		database = new Database(config);
		
		String token = config.getString("api.discord");
		String osuKey = config.getString("api.osu");
		operators = config.getStringList("general.operators");
		client = new JDABuilder().setBotToken(token).buildAsync();
		
		osuApi = new Downloader(osuKey);
		
		client.addEventListener(new OsuTracker());
		client.addEventListener(commandHandler = new CommandListener());
		client.addEventListener(new BeatmapLinkExaminer());
		client.addEventListener(new AntiShitImageHosts());
		client.addEventListener(new BanchoStatusChecker());
		
		client.addEventListener(new ListenerAdapter() {
			@Override
			public void onReady(ReadyEvent event) {
				client.getAccountManager().setGame(config.getString("general.game"));
			}
		});
	}
	
	public static String getPrefix(Guild guild) {
		String nickname = guild.getNicknameForUser(client.getSelfInfo());
		return "@" + (nickname != null ? nickname : client.getSelfInfo().getUsername());
	}
	
	public static void sendMessage(MessageChannel messageChannel, String message, Consumer<Message> consumer) {
		messageChannel.sendMessageAsync(message, consumer);
	}
	
	public static void sendMessage(MessageChannel messageChannel, String message) {
		messageChannel.sendMessageAsync(message, null);
	}
	
	public static void editMessage(Message message, String newText, Consumer<Message> consumer) {
		message.updateMessageAsync(newText, consumer);
	}
	
	public static void editMessage(Message message, String newText) {
		message.updateMessageAsync(newText, null);
	}
}
