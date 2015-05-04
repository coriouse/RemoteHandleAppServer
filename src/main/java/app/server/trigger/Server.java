package app.server.trigger;

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
import app.server.models.Start;
import app.server.protocol.ProcessorFactory;

public class Server {
	
	final static Logger logger = Logger.getLogger(Server.class);
	
	public Integer port;
	private final List<Start> commands;
	 
	public Server(String xml) {
		Configure configure = Server.getConfigure(xml);	
		this.port = configure.getPort();
		this.commands = configure.getAllCommands();
	}
	
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
	
	public List<Start> acceptCommands(String commands) {
		if(commands == null) {
			Collections.sort(this.commands);
			return this.commands;
		} else {			
			String[] arrCmd = commands.split(",");
			for(String c : arrCmd) {
				for(Start s : this.commands) {
					if(c.equals(s.key)) {
						s.status = true;
					} 
				}
			}			
			Collections.sort(this.commands);	
			return this.commands;
		}
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
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					if("END_MESSAGE".equals(inputLine)) {
						break;
					} else {
						sb.append(inputLine);
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
		
		
	public static void main(String[] args) {
		String config = null;
		
		if(args.length == 0) {
			args = new String[1];
			args[0] = "config=C:"+File.separator+"temp"+File.separator+"ServerTriger"+File.separator+"config.xml";
		}
		
		for(String params : args) {
			String[] par = params.split("[=]"); 
			switch(par[0]) {
				 case "config":
					 config = par[1]; 
				break;
			}
		}
		
		if(config != null) {
			Server server = new Server(config);
			server.runServer();
		}	
	}	
}
