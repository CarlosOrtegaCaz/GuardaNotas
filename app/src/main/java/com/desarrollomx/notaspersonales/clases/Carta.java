package com.desarrollomx.notaspersonales.clases;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import org.jetbrains.annotations.NotNull;

public class Carta extends CardView {
    Boolean isModifying = false;

    public Carta(@NonNull @NotNull Context context) {
        super(context);
    }

    public void setModifying(Boolean b) {
        isModifying = b;
    }

    public Boolean getModifying(){
        return isModifying;
    }
}
