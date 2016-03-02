package app.agent.core;

/**
 * Обертка сообщений между участниками
 * @author Ogarkov.Sergey
 *
 */
public class MessageRequest extends Message {
	
	public Command command;
	public Object data;

	public MessageRequest(CommandStatus status, Command command, Object data) {
		this.status = status;
		this.command = command;
		this.data = data;
	}
	
	public MessageRequest() {}
}