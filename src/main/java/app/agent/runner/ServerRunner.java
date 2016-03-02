package app.agent.runner;

import java.io.File;
import java.nio.file.Paths;

import app.agent.server.Server;

public class ServerRunner {
	public static void main(String[] args) {
		String config = null;
		
		//for debuging
		if(args.length == 0) {
			args = new String[1];
			args[0] = "config="+Paths.get("C:/temp/ServerTriger/", "config.xml").toAbsolutePath().toString() ;
		}
		
		for(String params : args) {
			String[] par = params.split("[=]"); 
			switch(par[0]) {
				 case "config":
					 config = par[1]; 
				break;
			}
		}
		
		if(config == null || config.length() == 0) {
			System.out.println("Путь до файла конфигурации не задан. Пример: <server> config=c:\\config");	
			System.exit(1);
		} else if(config != null) {
			Server server = new Server(config);
			server.runServer();
		}	
	}	
}
