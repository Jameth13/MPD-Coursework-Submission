//James Paton S1111175

package com.jamespaton.MPDCoursework;


import android.app.DatePickerDialog;
import android.content.Context;


//Extends the functionality of DatePickerDialog to include another button, clear.
class DatePickerWithClearButton extends DatePickerDialog {

    DatePickerWithClearButton(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, 0, callBack, year, monthOfYear, dayOfMonth);

        setButton(BUTTON_POSITIVE, ("OK"), this);
        setButton(BUTTON_NEUTRAL, ("Clear"), this);
        setButton(BUTTON_NEGATIVE, ("Cancel"), this);
    }
}
