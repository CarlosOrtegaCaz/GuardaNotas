package com.desarrollomx.notaspersonales.clases

import java.text.SimpleDateFormat
import java.util.*

class Nota {
  val identificador : Int
  var cuerpoNota : String
  var categoria : String = ""
  val fechaCreado : Date
  lateinit var fechaModificado : Date



  constructor(
              identificador : Int,
              mensaje : String,
              categoria : String,
              fechaCreado : String, fechaModificado : String
  )
  {
      this.identificador = identificador
      this.cuerpoNota = mensaje

      if(!categoria.isNullOrBlank()){
          this.categoria = categoria
      }
      val pattern = "yyyy-MM-dd HH:mm:ss"
      val simpleDateFormat = SimpleDateFormat(pattern)
      var date = simpleDateFormat.parse(fechaCreado)
      this.fechaCreado = date

      if (!fechaModificado.isNullOrBlank()){
          date = simpleDateFormat.parse(fechaModificado)
          this.fechaModificado = date
      }

  }

}