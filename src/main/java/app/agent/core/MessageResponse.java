package app.agent.core;

public class MessageResponse extends Message {
	
	public Command command;
	public Object data;	
	public String description;

	public MessageResponse(CommandStatus status, Command command, Object data) {
		this.status = status;
		this.command = command;
		this.data = data;
	}
	
	public MessageResponse() {}
}
