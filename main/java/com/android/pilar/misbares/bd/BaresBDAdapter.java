package com.android.pilar.misbares.bd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class BaresBDAdapter {

	// Campos de la BD
	public static final String CAMPO_ID = "_id";
	public static final String CAMPO_CATEGORIA = "categoria";
	public static final String CAMPO_NOMBRE = "nombre";
	public static final String CAMPO_DIRECCION = "direccion";
	public static final String CAMPO_RELACION = "relacion";
	public static final String CAMPO_FEC_PRI_VIS = "fecha_visita_pri";
	public static final String CAMPO_FEC_ULT_VIS = "fecha_visita_ult";
	public static final String CAMPO_PRECIO = "precio";
	public static final String CAMPO_VALORACION = "valoracion";
	public static final String CAMPO_ACOMPANANTE = "acompanante";
	public static final String CAMPO_NOTAS = "notas";
	private static final String TABLA_BD = "bares";
	private Context contexto;
	private SQLiteDatabase basedatos;
	private BaresBDHelper bdHelper;

	public BaresBDAdapter(Context context) {
        this.contexto = context;
	}

	public SQLiteDatabase getBD() {
        return basedatos;
	}

	// Método que abre la BD
	public BaresBDAdapter abrir() throws SQLException {
		// Abrimos la base de datos en modo escritura
		bdHelper = new BaresBDHelper(contexto);
		basedatos = bdHelper.getWritableDatabase();
		return this;
	}

	// Método que cierra la BD
	public void cerrar() {
        bdHelper.close();
	}

	// Método que crear un bar. Devuelve el id del registro nuevo si se ha dado de alta correctamente o -1 si no.
	public long crearBar(String categoria, String nombre, String direccion, String relacion,
			long fecha_visita_pri, long fecha_visita_ult, String precio, float valoracion, String acompanante, String notas) {

        // Usamos un argumento variable para modificar el registro
		ContentValues initialValues = crearContentValues(categoria, nombre, direccion, relacion,
                fecha_visita_pri, fecha_visita_ult, precio, valoracion, acompanante, notas);
		// Usamos la función insert del SQLiteDatabase
		return basedatos.insert(TABLA_BD, null, initialValues);
	}

	// Método que actualiza un bar
	public boolean actualizarBar(long id, String categoria, String nombre, String direccion, String relacion,
                 long fecha_visita_pri, long fecha_visita_ult, String precio, float valoracion, String acompanante, String notas) {

        // Usamos un argumento variable para modificar el registro
		ContentValues updateValues = crearContentValues(categoria, nombre, direccion, relacion,
                fecha_visita_pri, fecha_visita_ult, precio, valoracion, acompanante, notas);
		// Usamos la función update del SQLiteDatabase
		return basedatos.update(TABLA_BD, updateValues, CAMPO_ID + "=" + id, null) > 0;
	}

	// Método que borra todos los bares
	public boolean borraTodosLosBares() {
		// Usamos la función delete del SQLiteDatabase
		return basedatos.delete(TABLA_BD, null , null) > 0;
	}

	// Método que borra un bar
	public boolean borraBar(long id) {
		// Usamos la función delete del SQLiteDatabase
		return basedatos.delete(TABLA_BD, CAMPO_ID + "=" + id, null) > 0;
	}

	// Devuelve un Cursor con la consulta a todos los registros de la BD
	public Cursor obtenerBares(String orden) {
		return basedatos.query(TABLA_BD, new String[] { CAMPO_ID, CAMPO_FEC_PRI_VIS, CAMPO_NOMBRE, CAMPO_DIRECCION,
				CAMPO_VALORACION, CAMPO_RELACION, CAMPO_FEC_ULT_VIS, CAMPO_PRECIO, CAMPO_ACOMPANANTE, CAMPO_NOTAS},
				null, null, null, null, orden);
	}

	// Devuelve un Cursor con la consulta de todos los registros de la BD para exportar la tabla
	public Cursor obtenerBaresBackup() {
		return basedatos.query(TABLA_BD, null, null, null, null, null, null);
	}

	// Devuelve el id de un bar
	public Cursor getBar(long id) throws SQLException {
		Cursor mCursor = basedatos.query(true, TABLA_BD, new String[] {
				CAMPO_ID, CAMPO_CATEGORIA, CAMPO_NOMBRE, CAMPO_DIRECCION, CAMPO_RELACION, CAMPO_FEC_PRI_VIS,
				CAMPO_FEC_ULT_VIS, CAMPO_PRECIO, CAMPO_VALORACION, CAMPO_ACOMPANANTE, CAMPO_NOTAS},
				CAMPO_ID + "=" +id, null, null, null, null, null);

        // Nos movemos al primer registro de la consulta
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	// Método que crea un objeto ContentValues con los parámetros indicados
	private ContentValues crearContentValues(String categoria, String nombre, String direccion, String relacion,
             long fecha_visita_pri, long fecha_visita_ult, String precio, float valoracion, String acompanante, String notas) {

        ContentValues values = new ContentValues();
		values.put(CAMPO_CATEGORIA, categoria);
		values.put(CAMPO_NOMBRE, nombre);
		values.put(CAMPO_DIRECCION, direccion);
		values.put(CAMPO_RELACION, relacion);
		values.put(CAMPO_FEC_PRI_VIS, fecha_visita_pri);
		values.put(CAMPO_FEC_ULT_VIS, fecha_visita_ult);
		values.put(CAMPO_PRECIO, precio);
		values.put(CAMPO_VALORACION, valoracion);
		values.put(CAMPO_ACOMPANANTE, acompanante);
		values.put(CAMPO_NOTAS, notas);
		return values;
	}
}
