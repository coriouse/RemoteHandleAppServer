package app.server.models;

import app.server.protocol.Commands;
import app.server.protocol.Status;

public class Message {
	
	public Status status;
	public Commands command;
	public String data;	
	public String description;

	public Message(Status status, Commands command, String data) {
		this.status = status;
		this.command = command;
		this.data = data;
	}
	
	public Message() {}
}