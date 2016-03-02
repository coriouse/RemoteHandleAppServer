package app.agent.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import app.agent.core.ProcessorFactory;
import app.agent.core.models.Start;
/**
 * Сервер обработки команд
 * @author Ogarkov.Sergey
 *
 */
public class Server {
	
	final static Logger logger = Logger.getLogger(Server.class);
	
	public Integer port;
	private final List<Start> commands;
	final static char END_MESSAGE = '#';
	
	public Server(String xml) {
		Configure configure = Server.getConfigure(xml);	
		this.port = configure.getPort();
		this.commands = configure.getAllCommands();
	}
	
	/**
	 * Класс обработки файла конфигурации
	 * @author Ogarkov.Sergey
	 *
	 */
	private static class Configure {
		
		private DocumentBuilderFactory factory;
		private DocumentBuilder builder;
		private final List<Start> commands = new ArrayList<Start>();
		private Integer port;
		
		public Configure(String xml) {
			try {
				factory = DocumentBuilderFactory.newInstance();
				builder = factory.newDocumentBuilder();	
				parse(xml);
				logger.info("Configuration is finished");
			} catch (ParserConfigurationException e) {
				logger.error("Xml parser", e);
			}
		}
		
		private void parse(String xml) {
			try {
				Document document = builder.parse(new FileInputStream(new File(xml))/*  ClassLoader.getSystemResourceAsStream("file.xml")*/);
				NodeList nodeListPort = document.getElementsByTagName("port");
				port = Integer.parseInt(nodeListPort.item(0).getTextContent());
				NodeList nodeListCommands = document.getElementsByTagName("command");
				addCommands(nodeListCommands);
			} catch (SAXException | IOException e) {
				logger.error("Parse", e);
			}
		}
		
		private void addCommands(NodeList nodeListCommands) {
			for(int j = 0;j<nodeListCommands.getLength();j++) {
				int order =  Integer.parseInt(nodeListCommands.item(j).getAttributes().getNamedItem("order").getTextContent());
				String key = nodeListCommands.item(j).getAttributes().getNamedItem("key").getTextContent();
				String command = nodeListCommands.item(j).getChildNodes().item(1).getTextContent(); 
				commands.add(new Start(order, key, command));
			}
		}
		
		public List<Start> getAllCommands() {
			return commands;
		}
				
		public Integer getPort() {
			return port;
		}
	}
	
	public static Configure getConfigure(String xml) {
		return new Configure(xml);
	} 
	
	public List<Start> acceptCommands(Set<String> command) {
		if(command == null) {
			Collections.sort(this.commands);
			return this.commands;
		} else {
			for(Start s : this.commands) {
				if(command.contains(s.key)) {
					s.status = true;
				} 
			}
		}
			Collections.sort(this.commands);	
			return this.commands;
	}
	
	public void resetCommands() {
		synchronized (commands) {
			for(Start s : this.commands) {
				s.status = false;
			}	
		}
	}
	
	public void runServer() {
		logger.info("Start trigger server: port="+this.port);
		ExecutorService pool = Executors.newFixedThreadPool(50);
		try (ServerSocket server = new ServerSocket(this.port)) {
			while (true) {
				try {
					Socket connection = server.accept();
					Callable<Void> task = new ExecuteTask(connection, this);
					pool.submit(task);
				} catch (IOException ex) {}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Поток обслуживания клиента
	 * @author Ogarkov.Sergey
	 *
	 */
	private static class ExecuteTask implements Callable<Void> {
		private Socket connection;
		private Server server;
		private XStream xStream;
		
		public ExecuteTask(Socket connection, Server server) {
			this.xStream = new XStream(new DomDriver());
			this.connection = connection;
			this.server = server;
		}
		
		@Override
		public Void call() {
			try {
				logger.info("Start thread from "+connection.getInetAddress().getHostAddress());
				PrintWriter outs =  new PrintWriter(connection.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder sb = new StringBuilder();	
				 int value=0;
				 while((value = in.read()) != -1) {
					 if(END_MESSAGE == (char)value) {
							break;
					} else {
						sb.append((char)value);
					}
				}
				String answer = ProcessorFactory.getInstance().getExecutor(server, sb.toString()).doExecute();
					outs.println(answer);				
				logger.info("Message sent successfully");
				logger.info("Stop thread from "+connection.getInetAddress().getHostAddress());
				outs.close();
			} catch (IOException ex) {
				logger.error("Server error ", ex);
			} finally {
				try {
					connection.close();
				} catch (IOException e) {
					logger.error("Server error ", e);
				}
			}
			return null;
		}
	}
}
