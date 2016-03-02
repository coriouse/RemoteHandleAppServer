package app.agent.runner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import app.agent.core.Command;
import app.agent.core.CommandStatus;
import app.agent.core.Message;
import app.agent.core.MessageRequest;
import app.agent.core.MessageResponse;

public class ClientRunner {
	
		private String SERVER;
		private int PORT;
		private String COMMANDS;
		private final static int TIMEOUT = 15000;
		final static String END_MESSAGE = "#";
		final private XStream xStream = new XStream(new DomDriver());
		
		
		public ClientRunner(String[] args) {
			for(String params : args) {
				String[] par = params.split("[=]"); 
				switch(par[0]) {
					 case "server":
						 this.SERVER = par[1];  	 
						 break;
					 case "port":
						 this.PORT = Integer.parseInt(par[1]);
						 break;
					 case "commands":
						 this.COMMANDS = par[1];
						 break;
				}
			}
		}
		
		private Set<String> parsecommand(String commands) {
			Set<String> cmds = new HashSet<String>();
			String[] arrCmd = commands.split(",");
			for(String s : arrCmd) {
				cmds.add(s);
			}
			return cmds;
		}
		
		@SuppressWarnings("unchecked")
		private List<Message> getMessages(BufferedReader in) throws IOException {
			int value = 0;
			StringBuilder sb = new StringBuilder();
			while((value = in.read()) != -1) {
				sb.append((char)value);
			}
			return (List<Message>)xStream.fromXML(sb.toString());
		}
		
		public void runClinet()  throws UnknownHostException, IOException {
			Socket echoSocket = new Socket(SERVER, PORT);
			PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			StringBuilder request = new StringBuilder();
			request.append(xStream.toXML(new MessageRequest(CommandStatus.OK, Command.EXECUTE,parsecommand(this.COMMANDS))));
			request.append(END_MESSAGE);	
			
			out.println(request);		
			List<Message> messages = getMessages(in);
			for(Message m : messages) {
				MessageResponse response = (MessageResponse)m;
				System.out.println(response.status);				
				System.out.println(response.description);
				System.out.println(response.data);
			}
		}
		
		public static void main(String[] args) {
				
				if(args.length == 0) {
					args = new String[3];
					args[0] = "server=127.0.0.1";
					args[1] = "port=2628";
					args[2] = "commands=DIR";
				}
			
			ClientRunner client = new ClientRunner(args);
			try {
				client.runClinet();
			} catch (Exception e) {
				StringBuffer sb = new StringBuffer();
				sb.append(e.getMessage()).append("\r\n");
				for(StackTraceElement t : e.getStackTrace()) {
					sb.append(t.toString()).append("\r\t");
				}
				System.out.println(sb.toString());
			}
		}
}
