package app.server.protocol;

public enum Status {
	OK, //Без ошибок завершение
	ERROR, // Есть ошибки завершение
	STEP; // В просессе выполнения	
}