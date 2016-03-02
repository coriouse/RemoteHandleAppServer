package app.agent.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import app.agent.core.CommandStatus;
import app.agent.core.Message;
import app.agent.core.MessageResponse;
/**
 * Клас запуска внешних комманд
 * @author Ogarkov.Sergey
 *
 */
public class CommandExecutor {
	
	final private  static String  SERVER = "-----SERVER-----";	
	final static Logger logger = Logger.getLogger(CommandExecutor.class);
	
	public static MessageResponse launch(String command) {
		MessageResponse launchStatus = new MessageResponse();
				launchStatus.description = command;
		StringBuilder sb = new StringBuilder();		
				try {
					logger.info("Start cmd  /c "+command);
					Runtime rt = Runtime.getRuntime();
					Process proc = rt.exec("cmd.exe /c"+ command);
					//InputStream stderr = proc.getErrorStream();
					InputStream std = proc.getInputStream();
					ExecutorService pool = Executors.newFixedThreadPool(2);
						//sb.append(pool.submit(new StreamReader(stderr)).get());
						sb.append(pool.submit(new StreamReader(std)).get());
						pool.shutdown();
					int exitVal = proc.waitFor();
					if(exitVal == 1){
						launchStatus.status = CommandStatus.ERROR;
					} else {
						launchStatus.status = CommandStatus.OK;
					}
				} catch (Exception e) {
					launchStatus.status = CommandStatus.ERROR;
					launchStatus.data = getExceptionMessage(e);
					logger.error("Execution's problem", e);
				}
				logger.info("Stop cmd  /c "+command);
				logger.debug(sb.toString());
				launchStatus.data = sb.toString(); 
		return launchStatus;
	}
	
	private static String getExceptionMessage(Exception e) {
		StringBuffer message = new StringBuffer();
		message.append(e.getMessage()).append("\r\n");
		for(StackTraceElement t : e.getStackTrace()) {
			message.append(t.toString()).append("\r\t");
		}
		message.append(SERVER);
		return message.toString();
	}
	
	public static class StreamReader implements Callable<String> {
		private InputStream is;	
		private static Semaphore outputSem;
		public StreamReader(InputStream is) {
			this.is = is;
			outputSem = new Semaphore(1);
			try {
				outputSem.acquire();
			} catch (InterruptedException e) {
				logger.error("Read console", e);

			}
		}
		public String call() {
			StringBuilder sb = new StringBuilder();
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;				
				while((line = br.readLine()) != null) {
					sb.append(line).append("\r\n");
				}
			} catch (IOException ioe) {
				logger.error("Read console", ioe);
			} finally {
				outputSem.release();
			}
			return sb.toString();
		}
	}	
}