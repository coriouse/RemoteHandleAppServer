package app.server.tests;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.Test;

import app.server.models.Start;
import app.server.trigger.Server;

public class ServerTest {
	
	private final static String CONFIG = "/config.xml";
	private String configPath;
	public ServerTest() {
		configPath = new File(ServerTest.class.getResource(CONFIG).getPath()).getAbsolutePath();
	}
	
	@Test
	public void testAcceptCommands() {
		Server server = new Server(configPath);
		assertEquals(4, server.acceptCommands(null).size());
		int sum = 0;
		for(Start s : server.acceptCommands("DIR")) {
			if(s.status == true) {
				sum++;
			}
		}
		assertEquals(1, sum);
	}
}
