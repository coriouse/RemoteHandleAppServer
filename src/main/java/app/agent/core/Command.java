package app.agent.core;
/**
 * Команды, которые передаются между участниками соеденения
 * @author Ogarkov.Sergey
 *
 */
public enum Command {
	EXECUTE, //Запускаем выполнение	сервер
	STATUS; //Проверяем запущена задача или нет сервер	
}