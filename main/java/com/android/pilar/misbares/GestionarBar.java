package com.android.pilar.misbares;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pilar.misbares.bd.BaresBDAdapter;

import java.text.DateFormat;
import java.util.Calendar;

public class GestionarBar extends Activity {

	private EditText nombreText, direccionText, precioText, notasText;
	private Spinner categoriaSpinner, relacionSpinner, acompananteSpinner;
	private RatingBar valoracionRB;

    // Usamos esta variable para saber si estamos editando (filaId=id) o es un registro nuevo (filaId=null)
	private Long filaId;

	private BaresBDAdapter bdHelper;
	static final int FECHA_DIALOG_ID = 0;
	private TextView primeraFechaEtiqueta;
	private TextView ultimaFechaEtiqueta;
	private Button primeraFechaBoton;
	private Button ultimaFechaBoton;
	private Calendar primeraFecha, ultimaFecha;
	private TextView etiquetaActiva;
	private Calendar fechaActiva;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		// Creamos un adaptador y abrimos la BD
		bdHelper = new BaresBDAdapter(this);
		bdHelper.abrir();

        // Dibujamos el UI y buscamos sus Vistas
		setContentView(R.layout.editar_bar);
		categoriaSpinner = (Spinner) findViewById(R.id.category);
		relacionSpinner = (Spinner) findViewById(R.id.relacion);
		acompananteSpinner = (Spinner) findViewById(R.id.acompanante);
		nombreText = (EditText) findViewById(R.id.libro_editar_nombre);
		direccionText = (EditText) findViewById(R.id.libro_editar_direccion);
		precioText = (EditText) findViewById(R.id.libro_editar_precio);
		notasText = (EditText) findViewById(R.id.libro_editar_notas);
		valoracionRB = (RatingBar)findViewById(R.id.libro_editar_valoracion);
		Button aceptaBoton = (Button) findViewById(R.id.libro_editar_boton_aceptar);
		primeraFechaEtiqueta = (TextView) findViewById(R.id.primeraFechaEtiqueta);
		primeraFechaBoton = (Button) findViewById(R.id.libro_editar_boton_fecha_primera);
		ultimaFechaEtiqueta = (TextView) findViewById(R.id.ultimaFechaEtiqueta);
		ultimaFechaBoton = (Button) findViewById(R.id.libro_editar_boton_fecha_ultima);

		// Fecha actual de calendario
		primeraFecha = Calendar.getInstance();
		// No establecemos la fecha final
		ultimaFecha = null;

		// Variable con el ID del registro actual
		filaId = null;
		// Obtenemos el campo ID que se debe haber pasado en la invocación de la actividad si estamos editando el registro
		Bundle extras = getIntent().getExtras();
		// Si extras contiene algo cargamos ese ID ----- EDITAMOS EL REGISTRO
        // Si extras no contiene nada no hacemos nada -- REGISTRO NUEVO
		if (extras != null) {
			filaId = extras.getLong(BaresBDAdapter.CAMPO_ID);
		}

		// Cargamos el registro en los componentes de la pantalla
		cargarRegistro();

		// Método del botón OK
		aceptaBoton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

                // Si pulsa este botón guardamos los datos y devolvemos OK a la Actividad
				long ultimaAux = -1;
				String nombre = nombreText.getText().toString();
				String direccion = direccionText.getText().toString();

				// Si no se escribe el nombre o la direccion mostramos un mensaje
				if ((direccion.isEmpty()) || (nombre.isEmpty())) {
					Toast.makeText(getBaseContext(), "Es obligatorio rellenar los campos 'nombre' y 'dirección'.", Toast.LENGTH_SHORT).show();
					return;
				}
				else if (ultimaFecha!=null) {
                    // Guardamos la fecha de la última visita si se ha seleccionado
					ultimaAux=ultimaFecha.getTimeInMillis();
					if (primeraFecha.getTimeInMillis() > ultimaAux){
						Toast.makeText(getBaseContext(), "La fecha de la primera visita no puede ser mayor que la fecha de la última.", 1).show();
						return;
					}
				}
				String categoria = (String) categoriaSpinner.getSelectedItem();
				String relacion = (String) relacionSpinner.getSelectedItem();
				String acompanante = (String) acompananteSpinner.getSelectedItem();
				String notas = notasText.getText().toString();
				String precio = precioText.getText().toString();
				float valoracion = valoracionRB.getRating();

				// Damos de alta un nuevo registro
				if (filaId == null) {
					bdHelper.crearBar(categoria, nombre, direccion, relacion, primeraFecha.getTimeInMillis(),
                            ultimaAux, precio, valoracion, acompanante, notas);
				} else {
                    // Modificamos el registro seleccionado
					bdHelper.actualizarBar(filaId, categoria, nombre, direccion, relacion, primeraFecha.getTimeInMillis(),
                            ultimaAux, precio, valoracion, acompanante, notas);
				}

				// Devolvemos el resultado de la actividad
				setResult(RESULT_OK);
				// Acabamos la actividad
				finish();
			}
		});

		// Método del botón cambio fecha primera visita
		primeraFechaBoton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				showDateDialog(primeraFechaEtiqueta, primeraFecha);
			}
		});

		// Método del botón cambio fecha última visita
		ultimaFechaBoton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (ultimaFecha == null) ultimaFecha = Calendar.getInstance();
				showDateDialog(ultimaFechaEtiqueta, ultimaFecha);
			}
		});

		// Actualizamos las etiquetas que muestran las fechas
		updateDisplay(primeraFechaEtiqueta, primeraFecha);
		updateDisplay(ultimaFechaEtiqueta, ultimaFecha);
	}

	// Cargamos el registro en la pantalla
	private void cargarRegistro() {
		if (filaId != null) {
			Cursor bar = bdHelper.getBar(filaId);
            // Indicamos que queremos controlar el Cursor
			startManagingCursor(bar);

			// Obtenemos el campo categoria
			String categoria = bar.getString(bar.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_CATEGORIA));
			// Seleccionamos la categoría en el Spinner
			for (int i=0; i<categoriaSpinner.getCount();i++){
				// Cargamos una de la opciones del listado desplegable
				String s = (String) categoriaSpinner.getItemAtPosition(i);
				// Si coindice con la que está en la BD la seleccionamos en el listado desplegable
				if (s.equalsIgnoreCase(categoria)){
					categoriaSpinner.setSelection(i);
					break;
				}
			}

			// Obtenemos el campo relacion calidad-precio
			String relacion = bar.getString(bar.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_RELACION));
			// Seleccionamos en el Spinner la relacion c-p
			for (int i=0; i<relacionSpinner.getCount();i++){
				// Cargamos una de la opciones del listado desplegable
				String s = (String) relacionSpinner.getItemAtPosition(i);
				// Si coindice con la que está en la BD la seleccionamos en el listado desplegable
				if (s.equalsIgnoreCase(relacion)){
					relacionSpinner.setSelection(i);
					break;
				}
			}

			// Obtenemos el campo del acompañante
			String acompanante = bar.getString(bar.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_ACOMPANANTE));
			// Seleccionamos el formato en el Spinner
			for (int i=0; i<acompananteSpinner.getCount();i++){
				// Cargamos una de la opciones del listado desplegable
				String s = (String) acompananteSpinner.getItemAtPosition(i);
				// Si coindice con la que está en la BD la seleccionamos en el listado desplegable
				if (s.equalsIgnoreCase(acompanante)){
					acompananteSpinner.setSelection(i);
					break;
				}
			}

			// Rellenamos las Vistas
			nombreText.setText(bar.getString(bar.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_NOMBRE)));
			direccionText.setText(bar.getString(bar.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_DIRECCION)));
			notasText.setText(bar.getString(bar.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_NOTAS)));
			precioText.setText(bar.getString(bar.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_PRECIO)));
			valoracionRB.setRating(bar.getFloat(bar.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_VALORACION)));

            // Tratamos las fechas del registro
			long fecha = bar.getLong(bar.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_FEC_ULT_VIS));
			primeraFecha.setTimeInMillis(fecha);
			fecha = bar.getLong(bar.getColumnIndexOrThrow(BaresBDAdapter.CAMPO_FEC_PRI_VIS));
			if (fecha>0) {
				ultimaFecha=Calendar.getInstance();
				ultimaFecha.setTimeInMillis(fecha);
			}
			// Dejamos de controlar el cursor de la BD
			stopManagingCursor(bar);
		}
	} // end cargarRegistro

	// Método que actualiza la etiqueta y la variable de la fecha
	private void updateDisplay(TextView dateDisplay, Calendar date) {
		if (date!=null) {
			String auxStr = DateFormat.getDateInstance().format(date.getTime());
			dateDisplay.setText(auxStr);
		} else dateDisplay.setText("Sin seleccionar");
	}

	// Método que muestra el diálogo de selección de fecha
	public void showDateDialog(TextView dateDisplay, Calendar date) {
		etiquetaActiva = dateDisplay;
		fechaActiva = date;
		showDialog(FECHA_DIALOG_ID);
	}

	// Al seleccionar una fecha, cambiamos su valor en la variable correspondiente
	private OnDateSetListener dateSetListener = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			fechaActiva.set(Calendar.YEAR, year);
			fechaActiva.set(Calendar.MONTH, monthOfYear);
			fechaActiva.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDisplay(etiquetaActiva, fechaActiva);
			unregisterDateDisplay();
		}
	};

	// Método que libera la variable temporal
	private void unregisterDateDisplay() {
		etiquetaActiva = null;
		fechaActiva = null;
	}

	// Método que crea una ventana de diálogo donde seleccionar la fecha
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case FECHA_DIALOG_ID:
			// Se usa el método dateSetListener para recibir la fecha seleccionada
			return new DatePickerDialog(this, dateSetListener, fechaActiva.get(Calendar.YEAR),
					fechaActiva.get(Calendar.MONTH), fechaActiva.get(Calendar.DAY_OF_MONTH));
		}
		return null;
	}

	// Al preparar el diálogo cargamos la fecha correspondiente
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case FECHA_DIALOG_ID:
			((DatePickerDialog) dialog).updateDate(fechaActiva.get(Calendar.YEAR),
					fechaActiva.get(Calendar.MONTH), fechaActiva.get(Calendar.DAY_OF_MONTH));
			break;
		}
	}
}
