package com.isosystem.smarthouse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.isosystem.smarthouse.R;
import com.isosystem.smarthouse.connection.MessageDispatcher;
import com.isosystem.smarthouse.notifications.Notifications;
import com.isosystem.smarthouse.utils.BooleanFormulaEvaluator;
import com.isosystem.smarthouse.utils.EvaluatorResult;
import com.isosystem.smarthouse.utils.MathematicalFormulaEvaluator;


public class OutgoingRangeMessageCheckDialog extends DialogFragment {

    String mFirstOutgoingFormula = "";
    String mFirstValidationFormula = "";
    String mSecondOutgoingFormula = "";
    String mSecondValidationFormula = "";
    String prefix = "";
    Context mContext;

    public OutgoingRangeMessageCheckDialog(String formula1, String formula2,
                                           String formula3, String formula4,
                                           String pr, Context context) {
        super();

        this.mFirstOutgoingFormula = formula1;
        this.mFirstValidationFormula = formula2;
        this.mSecondOutgoingFormula = formula3;
        this.mSecondValidationFormula = formula4;
        this.prefix = pr;
        this.mContext = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View dialog_view = inflater.inflate(
                R.layout.fragment_dialog_range_message_test, null);

        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        dialog_view.setSystemUiVisibility(uiOptions);
        dialog_view.setSystemUiVisibility(8);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(
                "”кажите формулы обработки, формулы валидации и введите первое и второе значение:")
                .setView(dialog_view)
                .setPositiveButton("ѕроверить",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                String first_input = ((EditText) dialog_view
                                        .findViewById(R.id.first_value))
                                        .getText().toString();

                                String second_input = ((EditText) dialog_view
                                        .findViewById(R.id.second_value))
                                        .getText().toString();

                                // ѕроверка формулы преобразовани€ дл€ первого исход€щего значени€
                                MathematicalFormulaEvaluator firstValueEval = new MathematicalFormulaEvaluator(
                                        mFirstOutgoingFormula, first_input, "0", true);

                                EvaluatorResult first_result = firstValueEval.eval();

                                if (!first_result.isCorrect) {
                                    Notifications.showTooltip(getActivity(),
                                            first_result.errorMessage);
                                    return;
                                }

                                // ¬алидаци€ первого исход€щего значени€ (после обработки формулой)
                                BooleanFormulaEvaluator firstValueBoolEval = new BooleanFormulaEvaluator(
                                        mFirstValidationFormula,
                                        first_result.numericRoundedResult);

                                EvaluatorResult first_boolResult = firstValueBoolEval
                                        .eval();

                                if (!first_boolResult.isCorrect) {
                                    Notifications.showTooltip(
                                            getActivity(),
                                            first_boolResult.errorMessage);
                                    return;
                                }

                                if (!first_boolResult.booleanResult) {
                                    Notifications
                                            .showTooltip(getActivity(),
                                                    "ѕервое значение не прошло валидацию");
                                    return;
                                }

                                // ѕроверка формулы преобразовани€ дл€ второго исход€щего значени€
                                MathematicalFormulaEvaluator secondValueEval = new MathematicalFormulaEvaluator(
                                        mSecondOutgoingFormula, second_input, "0", true);

                                EvaluatorResult second_result = secondValueEval.eval();

                                if (!second_result.isCorrect) {
                                    Notifications.showTooltip(getActivity(),
                                            second_result.errorMessage);
                                    return;
                                }

                                // ¬алидаци€ второго исход€щего значени€ (после обработки формулой)
                                BooleanFormulaEvaluator secondValueBoolEval = new BooleanFormulaEvaluator(
                                        mSecondValidationFormula,
                                        second_result.numericRoundedResult);

                                EvaluatorResult second_boolResult = secondValueBoolEval
                                        .eval();

                                if (!second_boolResult.isCorrect) {
                                    Notifications.showTooltip(
                                            getActivity(),
                                            second_boolResult.errorMessage);
                                    return;
                                }

                                if (second_boolResult.booleanResult) {
                                    MessageDispatcher dispatcher = new MessageDispatcher(
                                            getActivity());

                                    String msg = dispatcher.SendRangeValueMessage(prefix, first_result.numericRoundedResult,
                                            second_result.numericRoundedResult, false);
                                    Notifications
                                            .showTooltip(getActivity(),
                                                    "—ообщение контроллеру: " + msg);
                                } else {
                                    Notifications
                                            .showTooltip(getActivity(),
                                                    "¬торое значение не прошло валидацию");
                                }
                            }
                        })
                .setNegativeButton("ќтмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                OutgoingRangeMessageCheckDialog.this.dismiss();
                            }
                        });
        return builder.create();
    }
}