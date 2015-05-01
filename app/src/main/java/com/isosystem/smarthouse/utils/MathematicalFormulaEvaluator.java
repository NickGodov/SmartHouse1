package com.isosystem.smarthouse.utils;

import android.text.TextUtils;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathematicalFormulaEvaluator {

	// Нужно ли округлять результат формулы
	Boolean mRoundFractionDigits;
	// Количество знаков после запятой
	String mFractionDigits;
	// Значение формулы
	String mFormula;
	// Значение переменной
	String mVariable;

	public MathematicalFormulaEvaluator(String formula, String var,
			String fdigits, Boolean round) {

		this.mFormula = formula;
		this.mVariable = var;
		this.mFractionDigits = fdigits;
		this.mRoundFractionDigits = round;
	}

	private String checkFormulaString(String formula) throws Exception {
		String resultFormula = formula;

		if (TextUtils.isEmpty(resultFormula.trim())) {
			resultFormula = "x";
		}

		return resultFormula;
	}

	private int checkFractionDigits(String digits) throws Exception {
		int result = 0;

		if (TextUtils.isEmpty(digits.trim())) {
			// Если пользователь оставил поле пустым, значит 0 знаков после
			// запятой
			return result;
		}

		// Если значение меньше нуля, выставляем 0
		result = Integer.parseInt(digits);
		if (result < 0) {
			result = 0;
		}

		return result;
	}

	public EvaluatorResult eval() {

		EvaluatorResult result = new EvaluatorResult();
		Evaluator evaluator = new Evaluator();

		// Проверяем поле "Количество знаков после запятой"
		int mDigits = 0;
		try {
			mDigits = checkFractionDigits(mFractionDigits);
		} catch (Exception e) {
			result.isCorrect = false;
			result.errorMessage = "Значение количества знаков после запятой некорректно, проверьте соответствующее поле";
			e.printStackTrace();
			return result;
		}
		
		// Проверяем формулу, в частности, пустое ли поле
		String replacedFormula = "";
		try {
			replacedFormula = checkFormulaString(mFormula);
		} catch (Exception e) {
			result.isCorrect = false;
			result.errorMessage = "Ошибка при загрузке формулы, проверьте корректность формулы";
			e.printStackTrace();
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
		
		result.numericRawResult = evalResult;
		result.numericRoundedResult = evalResult;

		// Если нужно округлять значение
		if (mRoundFractionDigits) {
			try {
				BigDecimal roundedResult = new BigDecimal(evalResult).setScale(
						mDigits, RoundingMode.HALF_EVEN);
				result.numericRoundedResult = roundedResult.toPlainString();
			} catch (Exception e) {
				result.isCorrect = false;
				result.errorMessage = "Ошибка при округлении результата формулы. Убедитесь в корректном вводе количества знаков после запятой и в корректности формулы";
				e.printStackTrace();
				return result;
			}
		}
		return result;
	}

}
