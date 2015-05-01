package com.isosystem.smarthouse.utils;

public class EvaluatorResult {

	//Корректный ли результат
	public Boolean isCorrect;
	// Сообщение об ошибке, если результат был не корректный
	public String errorMessage;
	// Числовой результат до округления
	public String numericRawResult;
	//Числовой результат после округления
	public String numericRoundedResult;
	//Булевый результат, если формула булевая
	public Boolean booleanResult;
	
	public EvaluatorResult() {
		this.isCorrect = true;
		this.errorMessage = "";
		this.numericRawResult = "";
		this.numericRoundedResult = "";
		this.booleanResult = false;
	}
}
