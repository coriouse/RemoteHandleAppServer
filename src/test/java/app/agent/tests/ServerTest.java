package app.agent.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import app.agent.core.models.Start;
import app.agent.server.Server;

public class ServerTest {
	
	private final static String CONFIG = "/config.xml";
	private String configPath;
	public ServerTest() {
		configPath = new File(ServerTest.class.getResource(CONFIG).getPath()).getAbsolutePath();
	}
	
	@Test
	public void testAcceptCommands() {
		Server server = new Server(configPath);
		assertEquals(6, server.acceptCommands(null).size());
		int sum = 0;
		Set<String> cmds = new HashSet<String>();
		cmds.add("DIR");
		for(Start s : server.acceptCommands(cmds)) {
			if(s.status == true) {
				sum++;
			}
		}
		assertEquals(1, sum);
	}
}
