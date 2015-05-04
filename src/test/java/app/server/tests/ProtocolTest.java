package app.server.tests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.junit.Test;

import app.server.models.Message;
import app.server.models.Start;
import app.server.protocol.Commands;
import app.server.protocol.ProcessorFactory;
import app.server.protocol.Status;
import app.server.trigger.Server;


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
		String fromObject = xStream.toXML(new Message(Status.OK, Commands.EXECUTE,"DIR,SET"));
		//System.out.println(fromObject);
		String expected = "<app.server.models.Message>\n"
							+ "  <status>OK</status>\n"
							+ "  <command>EXECUTE</command>\n"
							+ "  <data>DIR,SET</data>\n"
						+ "</app.server.models.Message>";
		
		assertEquals(expected, fromObject);
		
		Message message =  (Message) xStream.fromXML(expected);
		assertEquals(Status.OK, message.status);
		assertEquals(Commands.EXECUTE, message.command);
		assertEquals("DIR,SET", message.data);
		
	}
	
	@Test
	public void testProcessorGetMessages() {
		Server server = new Server(configPath);
		//get message
		XStream xStream = new XStream(new DomDriver());
		List<Message> messagesOk = (List<Message>)xStream.fromXML(ProcessorFactory.getInstance().getExecutor(server, xStream.toXML(new Message(Status.OK, Commands.EXECUTE,"DIR,SET"))).doExecute());
		assertTrue(messagesOk.size() == 2);
		for(Message m : messagesOk) {
			assertTrue(m.status == Status.OK);
			assertTrue(m.command == Commands.ON_PAGE); 
		}
		List<Message> messageError = (List<Message>)xStream.fromXML(ProcessorFactory.getInstance().getExecutor(server, xStream.toXML(new Message(Status.OK, Commands.EXECUTE,"ERROR"))).doExecute());
		assertTrue(messageError.size() == 1);
		assertTrue(messageError.get(0).status == Status.ERROR);
		assertTrue(messageError.get(0).command == Commands.ON_PAGE);
	}
	
	@Test
	public void testProcessorGetStatus() {
		Server server = new Server(configPath);	
		XStream xStream = new XStream(new DomDriver());
		List<Message> messagesBusy = (List<Message>)xStream.fromXML(ProcessorFactory.getInstance().getExecutor(server, xStream.toXML(new Message(Status.OK, Commands.STATUS,"PROCESS_IS_WORKING"))).doExecute());
			assertTrue(messagesBusy.size() == 1);
			assertTrue(messagesBusy.get(0).command == Commands.ON_PAGE);
			assertTrue(messagesBusy.get(0).status == Status.STEP);
		
		List<Message> messagesFree = (List<Message>)xStream.fromXML(ProcessorFactory.getInstance().getExecutor(server, xStream.toXML(new Message(Status.OK, Commands.STATUS,"PROCESS_IS_NOT_WORKING"))).doExecute());
			assertTrue(messagesFree.size() == 1);
			assertTrue(messagesFree.get(0).command == Commands.OFF_PAGE);
			assertTrue(messagesFree.get(0).status == Status.OK);
		
		
	}
	
	
}




















