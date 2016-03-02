package app.agent.core;

import app.agent.processor.ExecuteCmd;
import app.agent.processor.Processor;
import app.agent.processor.StatusExecution;
import app.agent.server.Server;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
/**
 * Фабрика команд
 * @author Ogarkov.Sergey
 *
 */
public class ProcessorFactory {
	
	final XStream xStream = new XStream(new DomDriver());
	final static ProcessorFactory PROCESSOR_FACTORY = new ProcessorFactory();
	
	public static ProcessorFactory getInstance() {
		return PROCESSOR_FACTORY;
	}
	
	public Processor getExecutor(Server server, String message) {
		MessageRequest messageObj = (MessageRequest)xStream.fromXML(message);		
		switch(messageObj.command) {
		case EXECUTE:
			return new ExecuteCmd(server, messageObj);
		case STATUS:
			return new StatusExecution(server, messageObj);
		default:
			break;
		}
		return null;
	}	
}
