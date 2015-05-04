package app.server.protocol;

import app.server.models.Message;
import app.server.processor.ExecuteCmd;
import app.server.processor.Processor;
import app.server.processor.StatusExecution;
import app.server.trigger.Server;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ProcessorFactory {
	
	final XStream xStream = new XStream(new DomDriver());
	final static ProcessorFactory PROCESSOR_FACTORY = new ProcessorFactory();
	
	public static ProcessorFactory getInstance() {
		return PROCESSOR_FACTORY;
	}
	
	public Processor getExecutor(Server server, String message) {
		Message messageObj = (Message)xStream.fromXML(message);		
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
