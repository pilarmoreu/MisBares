package com.android.pilar.misbares.backup;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.SQLException;
import android.os.AsyncTask;
import android.widget.Toast;

import com.android.pilar.misbares.MainActivity;
import com.android.pilar.misbares.bd.BaresBDAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImportarBDFileTask extends AsyncTask<String, Void, Boolean> {
	private final ProgressDialog dialogo;
	private Context contexto;
	private BaresBDAdapter bdHelper;
	private MainActivity actividad;

	public ImportarBDFileTask (Context contexto, BaresBDAdapter bdHelper){
		dialogo = new ProgressDialog(contexto);
		this.contexto=contexto;
		this.bdHelper= bdHelper;
		this.actividad=(MainActivity) contexto;
	}

	// Antes de ejecutar la tarea mostramos un mensaje
	protected void onPreExecute() {
		this.dialogo.setMessage("Importando la base de datos...");
		this.dialogo.show();
	}

	// Iniciamos en segundo plano la tarea de importar
	protected Boolean doInBackground(final String... args) {

		File dir = android.os.Environment.getExternalStorageDirectory();
		File importFile = new File(dir, "BackupBares/copia_seguridad.csv");
		// Si no existe el archivo de importanción mostramo un mensaje de error
		if (!importFile.exists()) {
			if (this.dialogo.isShowing()) {
				this.dialogo.dismiss();
			}
			Toast.makeText(contexto, "ERROR al importar datos.", Toast.LENGTH_SHORT).show();
			return false;
		}

		try{
			bdHelper.getBD().beginTransaction();
			bdHelper.borraTodosLosBares();
			BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(importFile)));
			String auxStr = "";
			// Leemos la primera línea para quitar la cabecera
			fin.readLine();
			while ((auxStr = fin.readLine()) != null){
				String[] DatosRegistro = auxStr.split(";");
				// Si el último campo no tiene contenido es necesario inicializarlo
				if (DatosRegistro[9]==null) DatosRegistro[9]="";
				String fechaFinal=DatosRegistro[6];
				long fechaFinalLong=-1;
				long fechaInicialLong=-1;
				SimpleDateFormat dfm = new SimpleDateFormat("dd/MM/yyyy");

				try {
					if (!fechaFinal.equals("-1")) {
						Date b = dfm.parse(fechaFinal);
						fechaFinalLong=b.getTime();
					}
					Date a = dfm.parse(DatosRegistro[5]);
					fechaInicialLong=a.getTime();
				}
				catch (ParseException e) {
					e.printStackTrace();
				}

				for (int i=0; i<DatosRegistro.length; i++)
					if (DatosRegistro[i].equals("#")) DatosRegistro[i]="";
				bdHelper.crearBar(DatosRegistro[1], DatosRegistro[2], DatosRegistro[3], DatosRegistro[4],
						fechaInicialLong, fechaFinalLong, DatosRegistro[7],
						Float.valueOf(DatosRegistro[8]), DatosRegistro[9], DatosRegistro[10]);
			}
			fin.close();

			bdHelper.getBD().setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			if (this.dialogo.isShowing()) {
				this.dialogo.dismiss();
			}
			Toast.makeText(contexto, "ERROR de base de datos al importar datos.", Toast.LENGTH_SHORT).show();
			return false;
		} catch (IOException e) {
			if (this.dialogo.isShowing()) {
				this.dialogo.dismiss();
			}
			Toast.makeText(contexto, "ERROR de lectura a acceso ficheros.", Toast.LENGTH_SHORT).show();
			return false;
		} finally {
			bdHelper.getBD().endTransaction();
		}
	}

	// Acaba la ejecución del hilo
	protected void onPostExecute(final Boolean success) {
		if (this.dialogo.isShowing()) {
			this.dialogo.dismiss();
		}
		if (success) {
			Toast.makeText(contexto, "Importación finalizada correctamente.", Toast.LENGTH_SHORT).show();
		}
		// Recargamos los registros
		actividad.refrescarDatos();
	}

}