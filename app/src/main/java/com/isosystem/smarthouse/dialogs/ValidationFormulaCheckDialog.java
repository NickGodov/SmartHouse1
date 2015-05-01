package com.isosystem.smarthouse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

import com.isosystem.smarthouse.notifications.Notifications;
import com.isosystem.smarthouse.utils.BooleanFormulaEvaluator;
import com.isosystem.smarthouse.utils.EvaluatorResult;
import com.isosystem.smarthouse.utils.MathematicalFormulaEvaluator;

public class ValidationFormulaCheckDialog extends DialogFragment {

	String outgoingFormula = "";
	String validationFormula = "";

	public ValidationFormulaCheckDialog(String formula1, String formula2) {
		super();

		this.outgoingFormula = formula1;
		this.validationFormula = formula2;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		final EditText input = new EditText(this.getActivity());
		input.setKeyListener(DigitsKeyListener.getInstance("0123456789."));

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(
				"Укажите формулу обработки, формулу валидации и ввидете x:")
				.setView(input)
				.setPositiveButton("Проверить",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								MathematicalFormulaEvaluator eval = new MathematicalFormulaEvaluator(
										outgoingFormula, input.getText()
												.toString(), "0", true);

								EvaluatorResult result = eval.eval();

								if (!result.isCorrect) {
									Notifications.showTooltip(getActivity(),
											result.errorMessage);
								} else {
									BooleanFormulaEvaluator boolEval = new BooleanFormulaEvaluator(
											validationFormula,
											result.numericRoundedResult);
									EvaluatorResult boolResult = boolEval
											.eval();

									if (!boolResult.isCorrect) {
										Notifications.showTooltip(
												getActivity(),
												boolResult.errorMessage);
									} else {
										if (boolResult.booleanResult) {
											Notifications.showTooltip(
													getActivity(),
													"Значение прошло валидацию");
										} else {
											Notifications
													.showTooltip(getActivity(),
															"Значение не прошло валидацию");
										}
									}
								}
							}
						})
				.setNegativeButton("Отмена",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								ValidationFormulaCheckDialog.this.dismiss();
							}
						});
		return builder.create();
	}
}