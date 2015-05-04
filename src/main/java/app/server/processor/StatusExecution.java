package app.server.processor;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import app.server.models.Message;
import app.server.models.Start;
import app.server.protocol.Commands;
import app.server.protocol.Status;
import app.server.trigger.Server;

public class StatusExecution implements Processor {

	private Message message;
	private Server server;
	private XStream xStream;
	
	public StatusExecution(Server server, Message message) {
		xStream = new XStream(new DomDriver());
		this.server = server;
		this.message = message;
	}
	
	@Override
	public String doExecute() {
		
		List<Message> responses = new ArrayList<Message>();
		if(this.message.status == Status.OK) {
			for(Start start : this.server.acceptCommands(this.message.data)) {
				if(start.status == true) {
					Message m = CommandExecutor.launch(start.command);
					if(m.status == Status.OK ) {
							m.status = Status.STEP;
							m.command = Commands.ON_PAGE;
							m.description = "Process is working yet";
					} else if (m.status == Status.ERROR && m.data.length() == 0) {
							m.status = Status.OK;
							m.command = Commands.OFF_PAGE;
					}		
					responses.add(m);
				}	
			}
			this.server.resetCommands();
		} else {
			//TODO надо что то сделать если ERROR придет от клиента
		}
		return xStream.toXML(responses);
	}
	
}
