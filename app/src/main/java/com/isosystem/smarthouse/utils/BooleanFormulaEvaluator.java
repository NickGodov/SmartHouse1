package com.isosystem.smarthouse.utils;

import android.text.TextUtils;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

public class BooleanFormulaEvaluator {

	// Значение формулы
	String mFormula;
	// Значение переменной
	String mVariable;

	public BooleanFormulaEvaluator(String formula, String var) {

		this.mFormula = formula;
		this.mVariable = var;
	}

	public EvaluatorResult eval() {

		EvaluatorResult result = new EvaluatorResult();
		Evaluator evaluator = new Evaluator();
	
		String replacedFormula = mFormula;
		
		// Если поле пустое возвращаем true	
		if (TextUtils.isEmpty(replacedFormula.trim())) {
			result.booleanResult = true;
			return result;
		}

		// Пытаемся вставить переменную
		try {
			evaluator.putVariable("x", mVariable);
		} catch (Exception e) {
			result.isCorrect = false;
			result.errorMessage = "Ошибка при попытке вставить значение переменной в формулу. Убедитесь в корректном значении переменной x";
			e.printStackTrace();
			return result;
		}

		// Меняем 'x' и 'Х' на #{x}
		try {
			replacedFormula = replacedFormula.replace('X', 'x');
			replacedFormula = replacedFormula.replace("x", "#{x}");
		} catch (Exception e) {
			result.isCorrect = false;
			result.errorMessage = "Ошибка при попытке парсинга переменной в формуле. Убедитель в корректности введенной формулы";
			e.printStackTrace();
			return result;
		}

		String evalResult = "";

        // Вычисляем значение. Вычисляется переменная replacedFormula
		try {
			evalResult = evaluator.evaluate(replacedFormula);
		} catch (EvaluationException e) {
			result.isCorrect = false;
			result.errorMessage = "Ошибка при вычислении формулы. Убедитесь в корректности введенной формулы";
			e.printStackTrace();
			return result;
		}
		
		if (evalResult.equals("Infinity")) {
			result.isCorrect = false;
			result.errorMessage = "При данном х происходит деление на ноль, скорректируйте формулу";
			return result;
		}

        result.booleanResult = evalResult.equals("1.0");

		return result;
	}

}
