package app.agent.tests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.junit.Test;

import app.agent.core.Command;
import app.agent.core.CommandStatus;
import app.agent.core.Message;
import app.agent.core.MessageRequest;
import app.agent.core.MessageResponse;
import app.agent.core.ProcessorFactory;
import app.agent.core.models.Start;
import app.agent.server.Server;














import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ProtocolTest {
	
	private final static String CONFIG = "/config.xml";
	private String configPath;
	
	public ProtocolTest() {
		configPath = new File(ServerTest.class.getResource(CONFIG).getPath()).getAbsolutePath();
	}
	

	@Test
	public void testMessage() {
		XStream xStream = new XStream(new DomDriver());	
		String fromObject = xStream.toXML(new MessageRequest(CommandStatus.OK, Command.EXECUTE,"DIR,SET"));
		//System.out.println(fromObject);
		/*String expected = "<app.server.models.Message>\n"
							+ "  <status>OK</status>\n"
							+ "  <command>EXECUTE</command>\n"
							+ "  <data>DIR,SET</data>\n"
						+ "</app.server.models.Message>";
					*/	
		
		
		String expected = "<app.agent.protocol.MessageRequest>\n"
							+"  <status>OK</status>\n"
							+"  <command>EXECUTE</command>\n"
							+"  <data class=\"string\">DIR,SET</data>\n"
							+"</app.agent.protocol.MessageRequest>";
		
		
		assertEquals(expected, fromObject);
		
		MessageRequest message =  (MessageRequest) xStream.fromXML(expected);
		assertEquals(CommandStatus.OK, message.status);
		assertEquals(Command.EXECUTE, message.command);
		assertEquals("DIR,SET", message.data);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testProcessorGetMessages() {
		Server server = new Server(configPath);
		//get message
		XStream xStream = new XStream(new DomDriver());
		List<Message> messagesOk = (List<Message>)xStream.fromXML(ProcessorFactory.getInstance().getExecutor(server, xStream.toXML(new MessageRequest(CommandStatus.OK, Command.EXECUTE,parsecommand("DIR,SET")))).doExecute());
		assertTrue(messagesOk.size() == 2);
		for(Message m : messagesOk) {
			MessageResponse response = (MessageResponse)m;
			assertTrue(response.status == CommandStatus.OK);
		}
		List<Message> messageError = (List<Message>)xStream.fromXML(ProcessorFactory.getInstance().getExecutor(server, xStream.toXML(new MessageRequest(CommandStatus.OK, Command.EXECUTE,parsecommand("ERROR")))).doExecute());
		assertTrue(messageError.size() == 1);
		assertTrue(((MessageResponse)messageError.get(0)).status == CommandStatus.ERROR);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testProcessorGetStatus() {
		Server server = new Server(configPath);	
		XStream xStream = new XStream(new DomDriver());
		List<Message> messagesBusy = (List<Message>)xStream.fromXML(ProcessorFactory.getInstance().getExecutor(server, xStream.toXML(new MessageRequest(CommandStatus.OK, Command.STATUS,parsecommand("PROCESS_IS_WORKING")))).doExecute());
			assertTrue(messagesBusy.size() == 1);			
			assertTrue(((MessageResponse)messagesBusy.get(0)).status == CommandStatus.PROCESSING);
		
		List<Message> messagesFree = (List<Message>)xStream.fromXML(ProcessorFactory.getInstance().getExecutor(server, xStream.toXML(new MessageRequest(CommandStatus.OK, Command.STATUS,parsecommand("PROCESS_IS_NOT_WORKING")))).doExecute());
			assertTrue(messagesFree.size() == 1);
			assertTrue(((MessageResponse)messagesFree.get(0)).status == CommandStatus.OK);
		
		
	}
	
	private Set<String> parsecommand(String commands) {
		Set<String> cmds = new HashSet<String>();
		String[] arrCmd = commands.split(",");
		for(String s : arrCmd) {
			cmds.add(s);
		}
		return cmds;
	}
	
	
}