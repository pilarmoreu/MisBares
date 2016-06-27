package com.android.pilar.misbares;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.pilar.misbares.bd.BaresBDAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

// Clase que redefine la forma de dibujar los elementos del listado
public class BarDatosViewBinder implements SimpleCursorAdapter.ViewBinder
{
	private Context contexto;


	// En el constructor guardamos el contexto de la aplicación principal
	public BarDatosViewBinder(Context context){
		this.contexto = context;
	}

	// Método que dibuja un elemento
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex)	{
		// Vamos obteniendo con getColumnIndex la columna que vamos a dibujar y usamos el componente view para redibujarlo

		if(cursor.getColumnIndex(BaresBDAdapter.CAMPO_RELACION)!=columnIndex) {

			if(cursor.getColumnIndex(BaresBDAdapter.CAMPO_VALORACION)==columnIndex) {
				RatingBar typeControl = (RatingBar)view;
				typeControl.setRating(cursor.getFloat(cursor.getColumnIndex(BaresBDAdapter.CAMPO_VALORACION)));
				return true;
			}

			else if (columnIndex == cursor.getColumnIndex(BaresBDAdapter.CAMPO_FEC_PRI_VIS)) {
				SimpleDateFormat curFormater = new SimpleDateFormat("dd/MM/yyyy");
				long fecha = cursor.getLong(cursor.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_FEC_PRI_VIS));
				Date fechaObj = new Date(fecha);
				((TextView) view).setText(curFormater.format(fechaObj));
				return true;
			}

			else if (columnIndex == cursor.getColumnIndex(BaresBDAdapter.CAMPO_CATEGORIA)) {
				String categoria = cursor.getString(cursor.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_CATEGORIA));
				((TextView) view).setText(categoria);
				return true;
			}

			else if (columnIndex == cursor.getColumnIndex(BaresBDAdapter.CAMPO_ACOMPANANTE)) {
				String acompanante = cursor.getString(cursor.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_ACOMPANANTE));
				((TextView) view).setText(acompanante);
				/*Resources r = contexto.getResources();
				String[] acompanantes = r.getStringArray(R.array.acompanante);
				String acompana = cursor.getString(cursor.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_ACOMPANANTE));
				ImageView typeControl = (ImageView)view;
				if (acompana.equals(acompanantes[1]))
					typeControl.setImageResource(R.drawable.familia);
				else if (acompana.equals(acompanantes[2]))
					typeControl.setImageResource(R.drawable.amigos);
				else typeControl.setImageResource(R.drawable.trabajo);*/
				return true;
			}
		}
		return false;
	}

}