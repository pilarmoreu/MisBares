package com.android.pilar.misbares;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pilar.misbares.backup.ExportarBDFileTask;
import com.android.pilar.misbares.backup.ImportarBDFileTask;
import com.android.pilar.misbares.bd.BaresBDAdapter;

public class MainActivity extends ListActivity {

	private BaresBDAdapter bdHelper;
	private static final int ACTIVIDAD_NUEVA = 0;
	private static final int ACTIVIDAD_EDITAR = 1;
	private static final int MENU_ID = Menu.FIRST + 1;
	private Cursor cursor;
	private String ordenListadoCampo;
	private int ordenListado, nRegistros=-1;
	private TextView nRegistrosTV;
	private AlertDialog.Builder ventana;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        nRegistrosTV =(TextView)findViewById(R.id.nRegistros);

        // Hacemos más ancha la línea de división entre elementos en el listado
		this.getListView().setDividerHeight(3);

		// Creamos el adaptador que conecta con la BD
		bdHelper = new BaresBDAdapter(this);
		// Cargamos todos los datos
		bdHelper.abrir();
		// Recuperamos la variable que almacena el estado de la BD
		SharedPreferences prefe = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		ordenListadoCampo=prefe.getString("orden", BaresBDAdapter.CAMPO_ID);
		ordenListado=prefe.getInt("opcion", 0);
        // Cargamos los datos del listado
		cargaDatos(ordenListadoCampo, ordenListado);

        // Indicamos el menú contextual asociado al listado
		registerForContextMenu(getListView());
	}

	// Cuando se acaba la Actividad cerramos la BD para que se escriba toda la información
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bdHelper != null) {
			bdHelper.cerrar();
		}
		// Guardamos el estado de la Actividad (el orden del listado)
		SharedPreferences preferencias = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putString("orden", ordenListadoCampo);
		editor.putInt("opcion", ordenListado);
		editor.commit();
	}

	// Creamos el menú principal
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menulistado, menu);
		return true;
	}

	// Método que indica lo que ocurre cuando el usuario hace click en una opción del menú
	@Override
	public boolean onMenuItemSelected(int id, MenuItem item) {

        // Buscamos la opción del menú principal seleccionada
		switch (item.getItemId()) {
		case R.id.insertar:
			// Creamos una actividad indicando el tipo de petición "ACTIVIDAD_NUEVA" y esperamos el resultado
			Intent i = new Intent(this, GestionarBar.class);
			startActivityForResult(i, ACTIVIDAD_NUEVA);
			// Indicamos que hemos manejado la opción del menú
			return true;

		case R.id.exportar:
			if (nRegistros < 1)
				Toast.makeText(this, "Para exportar la base de datos es necesario registrar algún bar.", Toast.LENGTH_SHORT).show();
            else
            // Comprobamos si el almacenamiento externo está disponible
                if (compruebaAlmacenamientoExt()) {
					// Pedimos confirmación antes de continuar
					ventana = new AlertDialog.Builder(this);
					ventana.setIcon(android.R.drawable.ic_dialog_info);
					ventana.setTitle("ATENCIÓN");
					ventana.setMessage("¿Está seguro de querer continuar?");
					ventana.setCancelable(false);
					ventana.setPositiveButton("SI", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int boton) {
							// Si el usuario pulsa SI iniciamos un hilo para hacer la exportación
							new ExportarBDFileTask(MainActivity.this, bdHelper).execute();
						}
					});
					ventana.setNegativeButton("NO", null);
					ventana.create().show();
				}
			// Indicamos que hemos manejado la opción del menú
			return true;

		case R.id.importar:
			if (compruebaAlmacenamientoExt()) {
				// Pedimos confirmación antes de continuar
				ventana = new AlertDialog.Builder(this);
				ventana.setIcon(android.R.drawable.ic_dialog_info);
				ventana.setTitle("ATENCIÓN");
				ventana.setMessage("Esta operación borra los registros actuales ¿Desea continuar?");
				ventana.setCancelable(false);
				ventana.setPositiveButton("SI", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int boton) {
						// Si el usuario pulsa SI iniciamos un hilo para hacer la importación
						new ImportarBDFileTask(MainActivity.this, bdHelper).execute();
					}
				});
				ventana.setNegativeButton("NO", null);
				ventana.create().show();
			}
			// Indicamos que hemos manejado la opción del menú
            return true;

		case R.id.acercade:
			// Mostramos una ventana de Diálogo
			ventana = new AlertDialog.Builder(this);
			ventana.setIcon(android.R.drawable.ic_dialog_info);
			ventana.setTitle("Acerca de");
			ventana.setMessage("Esta aplicación crea y gestiona una biblioteca de tus bares favoritos. \n" +
					"Pilar Moreu Falcón \n Programación de Dispositivos Móviles 2016");
			ventana.setCancelable(false);
			ventana.setPositiveButton("Aceptar", null);
			ventana.create().show();
			// Indicamos que hemos manejado la opción del menú
			return true;

		case R.id.ordenar:
			ventana = new AlertDialog.Builder(this);
			ventana.setIcon(android.R.drawable.ic_dialog_alert);
			ventana.setTitle("Selecciona una opción");
			// Cargamos las opciones del listado de ordenación
			ventana.setItems(R.array.orden, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int opcion) {
					ordenListado=opcion;
					switch (opcion) {
					case 0:
						// Recargamos los datos
						cargaDatos(BaresBDAdapter.CAMPO_ID, opcion);
						break;
					case 1:
						// Recargamos los datos
						cargaDatos(BaresBDAdapter.CAMPO_FEC_PRI_VIS + " DESC", opcion);
						break;
					case 2:
						// Recargamos los datos
						cargaDatos(BaresBDAdapter.CAMPO_DIRECCION, opcion);
						break;
					case 3:
						// Recargamos los datos
						cargaDatos(BaresBDAdapter.CAMPO_NOMBRE, opcion);
						break;
					case 4:
						// Recargamos los datos
						cargaDatos(BaresBDAdapter.CAMPO_VALORACION + " DESC", opcion);
						break;
					}
				}
			});
			ventana.create().show();
			// Indicamos que hemos manejado la opción del menú
			return true;
		}
		return super.onMenuItemSelected(id, item);
	}

	// El usuario hace click en la opción BORRAR BAR SELECCIONADO
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		// Buscamos la opción del menú seleccionada
		switch (item.getItemId()) {
		case MENU_ID:
			// Pedimos confirmación del borrado del registro
			ventana = new AlertDialog.Builder(this);
			ventana.setIcon(android.R.drawable.ic_dialog_info);
			ventana.setTitle("ATENCIÓN");
			ventana.setMessage("¿Está seguro de querer borrar el registro seleccionado?");
			ventana.setCancelable(false);
			ventana.setPositiveButton("SI", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int boton) {
					// Si el usuario pulsa SI obtenemos el id del elemento seleccionado
					AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
					// Borramos ese registro
					bdHelper.borraBar(info.id);
					// Recargamos los datos
					cargaDatos(ordenListadoCampo, ordenListado);
				}
			});
			ventana.setNegativeButton("NO", null);
			ventana.create().show();
			// Indicamos que hemos manejado la opción del menú
			return true;
		}
		return super.onContextItemSelected(item);
	}

	// El usuario hace click en un elemento del listado y se edita el registro
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// Creamos una actividad indicando el tipo de petición "ACTIVIDAD_EDITAR" y esperamos el resultado
		Intent i = new Intent(this, GestionarBar.class);
		// Pasamos CAMPO_ID como un dato extra
		i.putExtra(BaresBDAdapter.CAMPO_ID, id);
		startActivityForResult(i, ACTIVIDAD_EDITAR);
	}

	// Método que se llama cuando una subactividad devuelve el resultado
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		// Recargamos los datos si se ha modificado algo, esto es, cuando el usuario ha hecho click en OK
		if (resultCode == Activity.RESULT_OK)
			cursor.requery();
	}

	// Método que refresca los datos del listado
	public void refrescarDatos() {
        cargaDatos(ordenListadoCampo, ordenListado);
	}

    // Método que carga los datos
	private void cargaDatos(String orden, int opcion) {
		ordenListadoCampo = orden;
		cursor = bdHelper.obtenerBares(orden);
		// Se indica que si se termina la Actividad se elimina esta Cursor de la memoria
		startManagingCursor(cursor);

		// Indicamos cómo debe pasarse el campo nombre a la Vista de la opción (fila_bares.xml)
		String[] from = new String[] { BaresBDAdapter.CAMPO_NOMBRE, BaresBDAdapter.CAMPO_DIRECCION,
				BaresBDAdapter.CAMPO_FEC_PRI_VIS, BaresBDAdapter.CAMPO_VALORACION, BaresBDAdapter.CAMPO_RELACION,
				BaresBDAdapter.CAMPO_FEC_ULT_VIS, BaresBDAdapter.CAMPO_ACOMPANANTE, BaresBDAdapter.CAMPO_PRECIO,
				BaresBDAdapter.CAMPO_NOTAS};

		int[] to = new int[] { R.id.fila_nombre, R.id.fila_direccion, R.id.fila_fecha, R.id.fila_valoracion, R.id.fila_categoria};

		// Creamos un adaptador de tipo Matriz asociado al cursor
		BarCursorAdapter notas = new BarCursorAdapter(this,	R.layout.fila_bares, cursor, from, to);

        // El usuario selecciona una de las opciones del listado
		String[] opciones = getResources().getStringArray(R.array.orden);
		// Obtenemos el número de elementos del listado
		nRegistros=notas.getCount();
		nRegistrosTV.setText("Nº de registros: " + nRegistros + "   -   Orden: " + opciones[opcion]);
		// Indicamos al listado el adaptador que le corresponde
		setListAdapter(notas);
	}

	// Creamos el menú contextual
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, MENU_ID, 0, R.string.menu_borrar);
	}

	// Método que comprueba si el almacenamiento externo está activo y si se puede escribir
	private boolean compruebaAlmacenamientoExt(){

		// Obtenemos el estado del almacenamiento externo del teléfono
		String estado = Environment.getExternalStorageState();

		// La tarjeta está activa y se puede escribir en ella
		if (Environment.MEDIA_MOUNTED.equals(estado)) {
			return true;
		} else
			// Si no se puede leer el almacenamiento externo
			if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(estado)) {
				Toast.makeText(this, "El teléfono dispone de almacenamiento externo conectado " +
						"pero no se puede escribir en él.", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "No está disponible el almacenamiento externo para hacer la exportación de datos.",
						Toast.LENGTH_SHORT).show();
			}
		return false;
	}

}

