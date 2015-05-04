package app.server.models;

public class  Start implements Comparable<Start> {
	
	public Integer order;
	public String key;
	public String command;
	public boolean status = false;
	
	public Start(Integer order, String key, String command) {
		this.order = order;
		this.key = key;
		this.command = command;
	}

	@Override
	public int compareTo(Start start) {
		if(this.order == start.order)
			return 0;
		else 
			return this.order > start.order ? 1 : -1;
	}

	@Override
	public String toString() {
		return "Start [order=" + order + ", key=" + key + ", command="
				+ command + ", status=" + status + "]";
	}
	
	
	
	
}