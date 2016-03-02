package app.agent.processor;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import app.agent.core.CommandStatus;
import app.agent.core.Message;
import app.agent.core.MessageRequest;
import app.agent.core.MessageResponse;
import app.agent.core.models.Start;
import app.agent.server.Server;

/**
 * Проверка существования запущенной команды, в данном случае через cmd /c tasklist | findstr "<something>"
 * @author Ogarkov.Sergey
 *
 */
public class StatusExecution implements Processor {

	private MessageRequest message;
	private Server server;
	private XStream xStream;
	
	public StatusExecution(Server server, MessageRequest message) {
		xStream = new XStream(new DomDriver());
		this.server = server;
		this.message = message;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String doExecute() {
		
		List<Message> responses = new ArrayList<Message>();
		if(this.message.status == CommandStatus.OK) {
			for(Start start : this.server.acceptCommands((Set<String>)((MessageRequest)this.message).data)) {
				if(start.status == true) {
					MessageResponse m = CommandExecutor.launch(start.command);
					if(m.status == CommandStatus.OK ) {
							m.status = CommandStatus.PROCESSING;							
							m.description = "Process is working yet";
					} else if (m.status == CommandStatus.ERROR && ((String)m.data).length() == 0) {
							m.status = CommandStatus.OK;
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
