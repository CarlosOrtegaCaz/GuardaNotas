package com.desarrollomx.notaspersonales

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.*
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

        //area_texto_nota =
       // findViewById<EditText>(R.id.ta_msg).setText("Brackets tiene varias características únicas como la Edición rápida y la Vista previa dinámica y muchas más que no vas a encontrar en otros editores. Además, Brackets está escrito en JavaScript, HTML y CSS.\n" +
        //        " Esto significa que la mayoría de quienes usan Brackets tienen las habilidades necesarias para modificar y extender el editor. De hecho, nosotros usamos Brackets todos los días para desarrollar Brackets. Para saber más sobre cómo utilizar estas características únicas, continúa leyendo.")

        var btn_guardar = findViewById<Button>(R.id.btn_guardar_nota)

        btn_guardar.setOnClickListener { accionBotonGuardarNota() }

        conexionBD()

        cargarNotas()

    }

    fun cargarNotas() {
        val appDB = openOrCreateDatabase("app.db", MODE_PRIVATE, null)

        val myCursor: Cursor = appDB.rawQuery("select * from notas", null)
        while (myCursor.moveToNext()) {
            val id = myCursor.getInt(0)
            val mensaje = myCursor.getString(1)
            println("Carta - " + mensaje)
            val categoria = myCursor.getString(2)
            val fechaCreado = myCursor.getString(3)
            val fechaModificado = myCursor.getString(4)

          //  val nota = Nota(id, mensaje, categoria, fechaCreado, fechaModificado)
            val nota = Nota(mensaje)
            println("Nota $id  $mensaje")
            agregarCarta(mensaje)
        }
        appDB.close()
    }

    fun conexionBD(){
        val appDB = openOrCreateDatabase("app.db", MODE_PRIVATE, null)

        appDB.execSQL(
            "CREATE TABLE IF NOT EXISTS notas (ID INTEGER PRIMARY KEY AUTOINCREMENT, mensaje TEXT, categoria TEXT,  fechaCreado Date, fechaModificado Date)"
        )
        appDB.close()
        println("----- Base de datos creada.")
    }

    private fun accionBotonGuardarNota() {
        //bloquear boton si no hay nada escrito
        val areaTexto = findViewById<EditText>(R.id.ta_msg)
        val mensaje = areaTexto.text.toString()
        if (mensaje.isNullOrBlank()){
            toastShort("Mensaje vacío...")
            return
        }

        //Guardar nota en base de datos
            guardarNota(mensaje)


        agregarCarta(mensaje)
        areaTexto.setText("")//Vaciar
        hideKeyboard()

    }

    private fun guardarNota(mensaje: String) {
        val row1 = ContentValues()
        row1.put("mensaje", mensaje)
        //val fecha = java.util.Calendar.getInstance()
        //println("Fecha:  " + fecha.time)

        val pattern = "yyyy-MM-dd HH:mm:ss"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date: String = simpleDateFormat.format(Date())
        println("Fecha: " + date)

        row1.put("fechaCreado", date)

        val appDB = openOrCreateDatabase("app.db", MODE_PRIVATE, null)

        appDB.insert("notas",null,row1)
        appDB.close()
    }

    fun hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun agregarCarta(mensaje: String) {
        var cardMsg = CardView(this)
        val params = LinearLayout.LayoutParams(
            900,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = 20
        params.bottomMargin = 20
        params.gravity = Gravity.CENTER_HORIZONTAL

        cardMsg.layoutParams = params
        cardMsg.setContentPadding(20, 15, 20, 35)
        cardMsg.cardElevation = 9f


        var nuevaNota = TextView(this)
        nuevaNota.setText(mensaje)
        nuevaNota.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)


        cardMsg.addView(nuevaNota)
        var contenedor = findViewById<LinearLayout>(R.id.contenedor_principal)
        contenedor.addView(cardMsg)

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