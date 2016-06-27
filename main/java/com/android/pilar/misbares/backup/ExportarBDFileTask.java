package com.android.pilar.misbares.backup;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.pilar.misbares.bd.BaresBDAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;

public class ExportarBDFileTask extends AsyncTask<String, Void, Boolean> {

    private final ProgressDialog dialogo;
    private Context contexto;
    private BaresBDAdapter bdHelper;
    private Cursor mCursor;

    public ExportarBDFileTask (Context contexto, BaresBDAdapter bdHelper){
        dialogo = new ProgressDialog(contexto);
        this.contexto=contexto;
        this.bdHelper= bdHelper;
    }

    // Antes de ejecutar la tarea mostramos un mensaje
    protected void onPreExecute() {
        this.dialogo.setMessage("Exportando la base de datos...");
        mCursor = bdHelper.obtenerBaresBackup();
        this.dialogo.show();
    }

    // Iniciamos en segundo plano la tarea
    protected Boolean doInBackground(final String... args) {

        File dir = android.os.Environment.getExternalStorageDirectory();
        File exportDir = new File(dir, "BackupBares");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        String dirAlmacExt=exportDir.getAbsolutePath();
        try {
            // Abrimos in fichero en el raíz de la tarjeta SD
            File fichero = new File(dirAlmacExt, "copia_seguridad.csv");
            OutputStreamWriter fout = new OutputStreamWriter(new FileOutputStream(fichero));

            int numcols = mCursor.getColumnCount();
            String auxStr="";

            // Primero buscamos los nombre de los campo
            for( int idx = 0; idx < numcols; idx++ ) {
                auxStr+=mCursor.getColumnName(idx)+";";
            }
            fout.write(auxStr + "\n");
            mCursor.moveToFirst();
            while (mCursor.getPosition() < mCursor.getCount()) {
                // Ahora escribimos los campos de la tabla bares
                auxStr="";
                for( int idx = 0; idx < numcols; idx++ ) {
                    if (mCursor.getColumnName(idx).equals(BaresBDAdapter.CAMPO_FEC_PRI_VIS) ||
                            mCursor.getColumnName(idx).equals(BaresBDAdapter.CAMPO_FEC_ULT_VIS)) {
                    	if (mCursor.getLong(idx)>0)
                    		auxStr += DateFormat.getDateInstance().format(mCursor.getLong(idx))+ ";";
                    	else auxStr += "-1;";
                    } else
                    	if (mCursor.getString(idx).isEmpty())
                    		auxStr += "#;";
                    	else auxStr += mCursor.getString(idx)+ ";";
                }
                fout.write(auxStr + "\n");
                mCursor.moveToNext();
            }
            fout.close();
            return true;
        }
        catch (Exception ex) {
            Log.e("Ficheros", "Error al escribir fichero en memoria externa");
            return false;
        }
    }

    // Acaba la ejecución del hilo
    protected void onPostExecute(final Boolean success) {
        if (this.dialogo.isShowing()) {
            this.dialogo.dismiss();
        }
        if (success) {
            Toast.makeText(contexto, "Exportación finalizada correctamente.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(contexto, "ERROR al exportar datos. Revisa los ajustes para permitir el almacenamiento.", Toast.LENGTH_SHORT).show();
        }
    }

}
