package app.agent.core;

/**
 * Статусы сообщений
 * @author Ogarkov.Sergey
 *
 */
public enum CommandStatus {
	OK, //Без ошибок завершение
	ERROR, // Есть ошибки завершение
	PROCESSING; // В просессе выполнения	
}