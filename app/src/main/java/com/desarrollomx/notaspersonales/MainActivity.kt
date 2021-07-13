package com.desarrollomx.notaspersonales

import android.R.id
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.*
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.desarrollomx.notaspersonales.clases.Nota
import com.desarrollomx.notaspersonales.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        //Prueba con texto con 500 caracteres
        //area_texto_nota =

        // findViewById<EditText>(R.id.ta_msg).setText("Brackets tiene varias características únicas como la Edición rápida y la Vista previa dinámica y muchas más que no vas a encontrar en otros editores. Además, Brackets está escrito en JavaScript, HTML y CSS.\n" +
        //        " Esto significa que la mayoría de quienes usan Brackets tienen las habilidades necesarias para modificar y extender el editor. De hecho, nosotros usamos Brackets todos los días para desarrollar Brackets. Para saber más sobre cómo utilizar estas características únicas, continúa leyendo.")

        val botonGuardar = findViewById<Button>(R.id.btn_guardar_nota)

        botonGuardar.setOnClickListener { accionBotonGuardarNota() }

        conexionBD()

        cargarNotas()
        goToBottom()
        hideKeyboard()

    }

    override fun onStart() {
        super.onStart()
        val areaTexto = findViewById<EditText>(R.id.ta_msg)
        areaTexto.clearFocus() //No sirve

    }

    private fun cargarNotas() {
        val appDB = openOrCreateDatabase("app.db", MODE_PRIVATE, null)

        val myCursor: Cursor = appDB.rawQuery("select * from notas", null)
        while (myCursor.moveToNext()) {
            val id = myCursor.getInt(0)
            val mensaje = myCursor.getString(1)
            //println("Carta - " + mensaje)
            val categoria = if( myCursor.getString(2).isNullOrBlank()) "" else myCursor.getString(2)
            val fechaCreado = myCursor.getString(3)
            val fechaModificado = if (myCursor.getString(4).isNullOrBlank()) "" else myCursor.getString(4)

            val nota = Nota(id, mensaje, categoria, fechaCreado, fechaModificado)
            //val nota = Nota(mensaje)
            //println("Nota $id  $mensaje")
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
            toastShort("Mensaje vacío...")
            return
        }

        //Guardar nota en base de datos
            //guardarNota(mensaje)


        //agregarCarta(mensaje)
        areaTexto.setText("")//Vaciar
        hideKeyboard()
        goToBottom()



    }

    private fun goToBottom () {
        val contenedorCartas = findViewById<LinearLayout>(R.id.contenedor_principal)
        val contenedorScroll = findViewById<ScrollView>(R.id.main_scroll)
        contenedorScroll.smoothScrollTo(0,contenedorCartas.height)
    }

    private fun guardarNota(mensaje: String) {
        val row1 = ContentValues()
        row1.put("mensaje", mensaje)
        //val fecha = java.util.Calendar.getInstance()
        //println("Fecha:  " + fecha.time)

        //Fecha a string


        row1.put("fechaCreado", fechaAString(Date()))

        val appDB = openOrCreateDatabase("app.db", MODE_PRIVATE, null)

        appDB.insert("notas",null,row1)
        appDB.close()
    }

    fun fechaAString (fecha : Date): String {
        val pattern = "yyyy-MM-dd HH:mm:ss"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date: String = simpleDateFormat.format(fecha)
        //println("Fecha: " + date)
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
        val cardMsg = CardView(this)
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
        txtFechaCreado.textSize = getResources().getDimension(R.dimen.texto_chico)
        //Agrega fecha
        linearLayout.addView(txtFechaCreado)

        //Agregar a contenedor principal
        val contenedor = findViewById<LinearLayout>(R.id.contenedor_principal)
        cardMsg.addView(linearLayout)
        contenedor.addView(cardMsg)

    }

    fun modificarNota(idNota : Int){
        val viewNota = findViewById<TextView>(idNota)
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
        botonAceptar.text = ";)"
            val paramsBtn = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)

        //botonAceptar.gravity = Gravity.RIGHT
        botonAceptar.layoutParams = paramsBtn

        botonAceptar.setOnClickListener { modificarNotaAceptar(idNota, editNota.id) }

        //Boton Cancelar
        val botonCancelar = Button(this)
        botonCancelar.text = "x"
        //botonCancelar.gravity = Gravity.RIGHT
        botonCancelar.layoutParams = paramsBtn

        val linearLayoutBotonesModificar = LinearLayout(this)
        linearLayoutBotonesModificar.orientation = LinearLayout.HORIZONTAL
        linearLayoutBotonesModificar.layoutParams = paramsBtn
        linearLayoutBotonesModificar.gravity = Gravity.RIGHT

        linearLayoutBotonesModificar.addView(botonAceptar)
        linearLayoutBotonesModificar.addView(botonCancelar)

        linearLayout.addView(linearLayoutBotonesModificar, linearLayout.childCount-1)

        //Ocultar text area
        ocultar_text_area()

    }

    fun ocultar_text_area() {
        val linearLayout = findViewById<LinearLayout>(R.id.conte_nuevo_text_area)
        linearLayout.visibility = View.GONE

        //val fragment = findViewById<Fragment>(R.id.nav_host_fragment_content_main)
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

        }

        mostrar_text_area()


    }

    //Guarda la modificacion en base de datos
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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