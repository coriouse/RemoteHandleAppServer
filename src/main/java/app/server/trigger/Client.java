package app.server.trigger;

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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import app.server.models.Message;
import app.server.protocol.Commands;
import app.server.protocol.Status;

public class Client {
	
		private String SERVER;
		private int PORT;
		private String COMMANDS;
		private final static int TIMEOUT = 15000;
		
		
		public Client(String[] args) {
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
		
		
		public void runClinet()  throws UnknownHostException, IOException {
			Socket echoSocket = new Socket(SERVER, PORT);
			PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			XStream xStream = new XStream(new DomDriver());
			StringBuilder request = new StringBuilder();
			request.append(xStream.toXML(new Message(Status.OK, Commands.EXECUTE,this.COMMANDS)));
			request.append("\r\n");
			request.append("END_MESSAGE");	
			System.out.println(request.toString());
			String fromServer;			
			out.println(request);				
			while ((fromServer = in.readLine()) != null) {
				System.out.println("Server: " + fromServer);
			}
		}
		
		public static void main(String[] args) {
				
				if(args.length == 0) {
					args = new String[3];
					args[0] = "server=127.0.0.1";
					args[1] = "port=2628";
					args[2] = "commands=SET,DIR";
				}
		//	boolean isReachible = InetAddress.getByName("127.0.0.1").isReachable(3000);
			//if(isReachible) {				
		//		System.out.println("Start clent");
			//}
			
			Client client = new Client(args);
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
