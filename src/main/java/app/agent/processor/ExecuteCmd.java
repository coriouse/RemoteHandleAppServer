package app.agent.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import app.agent.core.CommandStatus;
import app.agent.core.Message;
import app.agent.core.MessageRequest;
import app.agent.core.MessageResponse;
import app.agent.core.models.Start;
import app.agent.server.Server;
/**
 * Выполнение набора команд, которые запросил клиент
 * @author Ogarkov.Sergey
 *
 */
public class ExecuteCmd implements Processor {
	
	final static Logger logger = Logger.getLogger(ExecuteCmd.class);
	
	private MessageRequest message;
	private Server server;
	private XStream xStream;
	
	public ExecuteCmd(Server server, MessageRequest message) {
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
					MessageResponse m = (MessageResponse) CommandExecutor.launch(start.command);
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
