package com.desarrollomx.notaspersonales

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.desarrollomx.notaspersonales.clases.Carta
import com.desarrollomx.notaspersonales.clases.Nota
import com.desarrollomx.notaspersonales.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    lateinit var contenerdor_principal : ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val botonGuardar = findViewById<Button>(R.id.btn_guardar_nota)
        botonGuardar.setOnFocusChangeListener{v, hasFocus ->
            if(hasFocus){
                accionBotonGuardarNota()
            }}
        botonGuardar.setOnClickListener { accionBotonGuardarNota() }

        conexionBD()

        cargarNotas()

        contenerdor_principal = findViewById(R.id.main_scroll)
        goToBottom()
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

    }

    override fun onStart() {
        super.onStart()
        val areaTexto = findViewById<EditText>(R.id.ta_msg)
        areaTexto.clearFocus() //No sirve
        goToBottom()
    }

    private fun cargarNotas() {
        val appDB = openOrCreateDatabase("app.db", MODE_PRIVATE, null)

        val myCursor: Cursor = appDB.rawQuery("select * from notas", null)
        while (myCursor.moveToNext()) {
            val id = myCursor.getInt(0)
            val mensaje = myCursor.getString(1)
            val categoria = if( myCursor.getString(2).isNullOrBlank()) "" else myCursor.getString(2)
            val fechaCreado = myCursor.getString(3)
            val fechaModificado = if (myCursor.getString(4).isNullOrBlank()) "" else myCursor.getString(4)

            val nota = Nota(id, mensaje, categoria, fechaCreado, fechaModificado)
            agregarCarta(nota)
        }
        myCursor.close()
        appDB.close()
    }

    private fun conexionBD(){
        val appDB = openOrCreateDatabase("app.db", MODE_PRIVATE, null)

        appDB.execSQL(
            "CREATE TABLE IF NOT EXISTS notas (ID INTEGER PRIMARY KEY AUTOINCREMENT, mensaje TEXT, categoria TEXT,  fechaCreado Date, fechaModificado Date)"
        )
        appDB.close()
    }

    private fun accionBotonGuardarNota() {
        //Regresar si no hay nada escrito
        val areaTexto = findViewById<EditText>(R.id.ta_msg)
        val mensaje = areaTexto.text.toString()
        if (mensaje.isBlank()){
            toastShort(getString(R.string.nota_vacia))
            return
        }

        //Guardar nota en base de datos
        guardarNuevaNota(mensaje)
        areaTexto.setText("")//Vaciar
        hideKeyboard()
        goToBottom()
    }

    private fun goToBottom () {
        contenerdor_principal.fullScroll(View.FOCUS_DOWN)
    }

    private fun guardarNuevaNota(mensaje: String) {

        try {
            val row1 = ContentValues()
            row1.put("mensaje", mensaje)

            //Fecha a string
            val fecha = fechaAString(Date())
            row1.put("fechaCreado", fecha)

            val appDB = openOrCreateDatabase("app.db", MODE_PRIVATE, null)

            appDB.insert("notas",null,row1)
            val whereArgs = arrayOf<String>(mensaje, fecha)

            val myCursor: Cursor = appDB.rawQuery("select * from notas where mensaje = ? and fechaCreado = ?", whereArgs)
            while (myCursor.moveToNext()) {
                val id = myCursor.getInt(0)
                val mensaje = myCursor.getString(1)
                val categoria = if( myCursor.getString(2).isNullOrBlank()) "" else myCursor.getString(2)
                val fechaCreado = myCursor.getString(3)
                val fechaModificado = if (myCursor.getString(4).isNullOrBlank()) "" else myCursor.getString(4)

                val nota = Nota(id, mensaje, categoria, fechaCreado, fechaModificado)
                agregarCarta(nota)
            }
            myCursor.close()
            appDB.close()
        } catch (e : Exception){
            e.printStackTrace()
        }

    }

    fun fechaAString (fecha : Date): String {
        var pattern = "HH:mm:ss MM-dd-yyyy"
        if (Locale.getDefault().language == "es")
        {
            pattern = "HH:mm:ss dd-MM-yyyy"
        }
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date: String = simpleDateFormat.format(fecha)
        return date
    }

    fun hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun agregarCarta(nota: Nota) {
        //Configuracion Carta
        val cardMsg = Carta(this)
        val paramsCarta = LinearLayout.LayoutParams(
            resources.getDimension(R.dimen.ancho_carta).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        paramsCarta.topMargin = 20
        paramsCarta.bottomMargin = 20
        paramsCarta.gravity = Gravity.CENTER_HORIZONTAL
        cardMsg.layoutParams = paramsCarta

        cardMsg.setContentPadding(20, 15, 20, 15)
        cardMsg.cardElevation = 9f

        //Se agrega click sostenido para editar la nota
        cardMsg.isLongClickable = true
        cardMsg.setOnLongClickListener{
            modificarNota(nota.identificador)
            true
        }
        
        //Configuracion LinearLayout
        val paramsLinearLayout = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val linearLayout = LinearLayout(this)
        linearLayout.layoutParams = paramsLinearLayout
        linearLayout.orientation = LinearLayout.VERTICAL


        //Cuerpo de la nota
        val nuevaNota = TextView(this)
        nuevaNota.text = nota.cuerpoNota
        nuevaNota.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        nuevaNota.textSize = resources.getDimension(R.dimen.texto_medio)
        nuevaNota.id = nota.identificador
        linearLayout.addView(nuevaNota)

        //Fecha
        val txtFechaCreado = TextView(this)
        val parametrosFecha = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        txtFechaCreado.layoutParams = parametrosFecha
        txtFechaCreado.text = fechaAString(nota.fechaCreado)
        txtFechaCreado.gravity = Gravity.END
        txtFechaCreado.textSize = resources.getDimension(R.dimen.texto_chico)
        //Agrega fecha
        linearLayout.addView(txtFechaCreado)

        //Agregar a contenedor principal
        val contenedor = findViewById<LinearLayout>(R.id.contenedor_principal)
        cardMsg.addView(linearLayout)
        contenedor.addView(cardMsg)

    }

    /**
     * Cambia un viewText por EditText pasando el id de nota
     * como parametro. Tambien agrega botones de aceptar o cancelar para confirmar
     * cambios.
     *  @param idNota Int
     *  @since 14/07/2021
     *  @author Juan Carlos Ortega
     */
    fun modificarNota(idNota : Int){
        val viewNota = findViewById<TextView>(idNota)
        val carta = viewNota.parent.parent as Carta
        if (carta.modifying){
            return
        }

        val editNota = EditText(this)
        editNota.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        editNota.textSize = resources.getDimension(R.dimen.texto_medio)
        editNota.setText(viewNota.text, TextView.BufferType.EDITABLE);
        editNota.id = 100000 + idNota


        val linearLayout = viewNota.parent as LinearLayout
        viewNota.visibility = View.GONE
        linearLayout.addView(editNota, 1)

        //Boton Aceptar
        val botonAceptar = Button(this)
        botonAceptar.text = getString(R.string.actualizar_nota)
        val paramsBtn = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)

        //botonAceptar.gravity = Gravity.RIGHT //paramsBtn no tiene propiedad gravity
        botonAceptar.layoutParams = paramsBtn

        botonAceptar.setOnClickListener { modificarNotaAceptar(idNota, editNota.id) }

        //Boton Cancelar
        val botonCancelar = Button(this)
        botonCancelar.text = getString(R.string.borrar_nota)
        //botonCancelar.gravity = Gravity.RIGHT
        botonCancelar.layoutParams = paramsBtn
        botonCancelar.setOnClickListener { modificarNotaCancelar(idNota, editNota.id) }

        val linearLayoutBotonesModificar = LinearLayout(this)
        linearLayoutBotonesModificar.orientation = LinearLayout.HORIZONTAL
        linearLayoutBotonesModificar.layoutParams = paramsBtn
        linearLayoutBotonesModificar.gravity = Gravity.RIGHT

        linearLayoutBotonesModificar.addView(botonAceptar)
        linearLayoutBotonesModificar.addView(botonCancelar)

        linearLayout.addView(linearLayoutBotonesModificar, linearLayout.childCount-1)

        carta.modifying = true
        ocultar_text_area()
    }

    /**
     * Accion cancelar modificar nota. Hace visible el viewText y elimina el EditText
     * @param idNota Int Identificador de la nota seleccionada
     * @param idEditNota Identificador del EditText a eliminar
     * @since 14/07/2021
     */
    fun modificarNotaCancelar(idNota: Int, idEditNota: Int){
        val viewNota = findViewById<TextView>(idNota)
        val editNota = findViewById<TextView>(idEditNota)

        if(eliminarNota(idNota)){
            viewNota.visibility = View.INVISIBLE
            val linearLayout = viewNota.parent as LinearLayout
            linearLayout.removeViewAt(linearLayout.childCount-2)
            linearLayout.removeView(editNota)

            val nota = viewNota.parent.parent as Carta
            val contenedor = findViewById<LinearLayout>(R.id.contenedor_principal)

            contenedor.removeView(nota)
        }

        mostrar_text_area()
    }

    /**
     * Elimina la nota en base de datos
     * @param idNota Int Identificador de la nota seleccionada
     * @since 10/01/2023
     */

    fun eliminarNota (idNota: Int): Boolean {
        try {
            val where = "id=?"
            val whereArgs = arrayOf<String>(idNota.toString())

            val appDB = openOrCreateDatabase("app.db", MODE_PRIVATE, null)
            appDB.delete("notas",where,whereArgs)
            appDB.close()

        } catch (e : Exception){
            return false
        }
        return true
    }

    fun ocultar_text_area() {
        val linearLayout = findViewById<LinearLayout>(R.id.conte_nuevo_text_area)
        linearLayout.visibility = View.GONE
    }

    fun mostrar_text_area() {
        val linearLayout = findViewById<LinearLayout>(R.id.conte_nuevo_text_area)
        linearLayout.visibility = View.VISIBLE
    }

    fun modificarNotaAceptar(idNota: Int, idEditNota: Int) {
        val viewNota = findViewById<TextView>(idNota)
        val editNota = findViewById<TextView>(idEditNota)

        if(guardarModificacionNota(idNota, editNota.text.toString())){
            viewNota.text = editNota.text.toString()
            viewNota.visibility = View.VISIBLE
            val linearLayout = viewNota.parent as LinearLayout
            linearLayout.removeViewAt(linearLayout.childCount-2)
            linearLayout.removeView(editNota)

            val carta = linearLayout.parent as Carta
            carta.modifying = false

        } else {
            toastShort("Ha ocurrido un error. No se ha guardado la nota correctamente")
        }

        mostrar_text_area()


    }

    /**
     * Guarda la modificacion de la nota en base de datos
     */
    fun guardarModificacionNota (idNota: Int, mensaje : String): Boolean {
        try {
            val row1 = ContentValues()
            row1.put("mensaje", mensaje)
            row1.put("fechaModificado", fechaAString(Date()))
            val appDB = openOrCreateDatabase("app.db", MODE_PRIVATE, null)

            val where = "id=?"
            val whereArgs = arrayOf<String>(idNota.toString())

            appDB.update("notas", row1, where, whereArgs)
            appDB.close()
        } catch (e : Exception){
            return false
        }
        return true
    }

    fun toastShort (mensaje : String) {
        Toast.makeText(this@MainActivity, mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    fun openNewTabWindow(urls: String, context : Context) {
        val uris = Uri.parse(urls)
        val intents = Intent(Intent.ACTION_VIEW, uris)
        val b = Bundle()
        b.putBoolean("new_window", true)
        intents.putExtras(b)
        context.startActivity(intents)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val configuracion = getString(R.string.action_settings)
        if(item.title == configuracion){
                openNewTabWindow(getString(R.string.url_anuncios), this)
        }
         return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}