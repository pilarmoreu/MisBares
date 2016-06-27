package com.android.pilar.misbares.bd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BaresBDHelper extends SQLiteOpenHelper {

	// Definimos el nombre y la versión de la BD
	private static final String BD_NOMBRE = "bdbares.db";
	private static final int BD_VERSION = 1;

	// SQL que crea la base de datos. Es muy importante usar el campo _id
	private static final String BD_CREAR = "CREATE TABLE bares (_id integer primary key autoincrement, "
			+ "categoria text not null, nombre text not null, direccion text not null, relacion text, "
			+ "fecha_visita_pri long, fecha_visita_ult long,"
			+ "precio text, valoracion float, acompanante text, notas text);";

	// Contructor de la clase
	public BaresBDHelper(Context context) 	{
        super(context, BD_NOMBRE, null, BD_VERSION);
	}

	// Método invocado por Android si no existe la BD
	@Override
	public void onCreate(SQLiteDatabase database) {
		// Creamos la estructura de la BD
		database.execSQL(BD_CREAR);
	}

	// Método invocado por Android si hay un cambio de versién de la BD
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		// Eliminamos la BD y la volvemos a crear otra vez
		database.execSQL("DROP TABLE IF EXISTS bares");
		onCreate(database);
	}
}
