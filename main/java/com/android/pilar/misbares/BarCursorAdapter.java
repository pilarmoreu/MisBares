package com.android.pilar.misbares;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

// Clase que redefine el Cursor de la BD
class BarCursorAdapter extends SimpleCursorAdapter
{
	// Redefinimos el método que dibuja los elementos del listado
	public BarCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		setViewBinder(new BarDatosViewBinder(context));
	}
}
