package com.desarrollomx.notaspersonales.clases

import java.util.*

class Nota {
  //val identificador : Int
  var cuerpoNota : String
  lateinit var categoria : String
  //val fechaCreado : Date
  //val fechaModificado : Date



  constructor(
              //identificador : Int,
              mensaje : String,
              //categoria : String,
              //fechaCreado : String, fechaModificado : String
  ){
      //this.identificador = identificador
      this.cuerpoNota = mensaje
    //  this.categoria = categoria
     // this.fechaCreado = fechaCreado
     // this.fechaModificado = fechaModificado
  }


}