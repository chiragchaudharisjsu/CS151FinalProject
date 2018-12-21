package com.cyraptor.uketabs;

import android.content.Context;
import android.util.AttributeSet;

public class UpdatedSpinner extends android.support.v7.widget.AppCompatSpinner {

    public UpdatedSpinner(Context context) { super(context); }

    public UpdatedSpinner(Context context, AttributeSet attrs) { super(context, attrs); }

    public UpdatedSpinner(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    @Override public void
    setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (sameSelected) {
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

    @Override public void
    setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (sameSelected) {
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }
}