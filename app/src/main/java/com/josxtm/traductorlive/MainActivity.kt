package com.josxtm.traductorlive

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

class MainActivity:Activity() {
 private lateinit var status:TextView
 private lateinit var original:TextView
 private lateinit var result:TextView
 private lateinit var toggle:Button
 private lateinit var history:HistoryDb
 private var recognizer:SpeechRecognizer?=null
 private val detector=LanguageIdentification.getClient()
 private var active=false
 private var generation=0
 private var language="en-US"
 private val names=arrayOf("Inglés","Francés","Alemán","Italiano","Portugués","Español")
 private val codes=arrayOf("en-US","fr-FR","de-DE","it-IT","pt-PT","es-ES")

 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  history=HistoryDb(this)
  language=getPreferences(MODE_PRIVATE).getString("language","en-US") ?: "en-US"
  val root=LinearLayout(this).apply {
   orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(24,40,24,24);setBackgroundColor(Color.rgb(249,249,250))
  }
  root.addView(TextView(this).apply{text="Traductor LIVE";textSize=28f;setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER})
  status=TextView(this).apply{text="Micrófono desactivado";textSize=16f;gravity=Gravity.CENTER}
  root.addView(status)
  toggle=Button(this).apply{
   text="PULSA PARA\nTRADUCIR";textSize=20f;setTextColor(Color.WHITE);isAllCaps=false
   setTypeface(null,Typeface.BOLD)
   setOnClickListener{if(active)stop() else start()}
  }
  styleButton(false)
  root.addView(toggle,LinearLayout.LayoutParams(260,260).apply{topMargin=22;bottomMargin=22})
  val select=Button(this).apply { text="Idioma de voz: "+names[codes.indexOf(language).coerceAtLeast(0)] }
  select.setOnClickListener {
   AlertDialog.Builder(this).setTitle("Idioma de voz").setSingleChoiceItems(names,codes.indexOf(language).coerceAtLeast(0)){dialog,index->
    language=codes[index]
    select.text="Idioma de voz: "+names[index]
    getPreferences(MODE_PRIVATE).edit().putString("language",language).apply()
    dialog.dismiss()
   }.setNegativeButton("Cancelar",null).show()
  }
  root.addView(select)
  root.addView(TextView(this).apply{text="El reconocimiento utiliza el idioma elegido. El idioma del texto se identifica automáticamente.";textSize=12f;gravity=Gravity.CENTER})
  val scroll=ScrollView(this)
  val content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  content.addView(TextView(this).apply{text="Texto original";textSize=16f})
  original=TextView(this).apply{text="—";textSize=18f;setPadding(0,12,0,24)}
  content.addView(original)
  content.addView(TextView(this).apply{text="Traducción al castellano";textSize=16f})
  result=TextView(this).apply{text="—";textSize=21f;setPadding(0,12,0,16)}
  content.addView(result)
  scroll.addView(content)
  root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
  root.addView(Button(this).apply{text="VER HISTÓRICO";setOnClickListener{showHistory()}},LinearLayout.LayoutParams(-1,-2))
  setContentView(root)
 }
 private fun styleButton(listening:Boolean){
  toggle.background=GradientDrawable().apply{shape=GradientDrawable.OVAL;setColor(if(listening) Color.rgb(29,128,72) else Color.rgb(211,30,42))}
 }
 private fun start(){
  if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
   requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),100)
   return
  }
  if(!SpeechRecognizer.isRecognitionAvailable(this)){status.text="Reconocimiento de voz no disponible";return}
  active=true;generation++
  toggle.text="PULSA PARA\nDETENER";styleButton(true)
  listen(generation)
 }
 private fun listen(expected:Int){
  if(!active||expected!=generation)return
  recognizer?.destroy()
  recognizer=SpeechRecognizer.createSpeechRecognizer(this)
  recognizer?.setRecognitionListener(object:RecognitionListener {
   override fun onReadyForSpeech(params:Bundle?){if(active)status.text="Escuchando…"}
   override fun onBeginningOfSpeech(){}
   override fun onRmsChanged(rmsdB:Float){}
   override fun onBufferReceived(buffer:ByteArray?){}
   override fun onEndOfSpeech(){if(active)status.text="Procesando…"}
   override fun onError(error:Int){
    if(!active||generation!=expected)return
    recognizer?.destroy();recognizer=null
    if(error==SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS||error==SpeechRecognizer.ERROR_CLIENT){
     stop();status.text="Error de reconocimiento: $error"
    }else toggle.postDelayed({if(active&&generation==expected)listen(expected)},850)
   }
   override fun onResults(results:Bundle?){
    val spoken=results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
    if(active&&generation==expected){
     if(spoken.isNotBlank())translate(spoken,expected)
     toggle.postDelayed({if(active&&generation==expected)listen(expected)},400)
    }
   }
   override fun onPartialResults(partialResults:Bundle?){
    if(active&&generation==expected){
     val partial=partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
     if(!partial.isNullOrBlank())original.text=partial
    }
   }
   override fun onEvent(eventType:Int,params:Bundle?){}
  })
  val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{
   putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
   putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true)
   putExtra(RecognizerIntent.EXTRA_LANGUAGE,language)
  }
  try{recognizer?.startListening(intent)}catch(e:Exception){stop();status.text=e.message ?: "No se puede escuchar"}
 }
 private fun translate(spoken:String,expected:Int){
  original.text=spoken;result.text="Traduciendo…"
  detector.identifyLanguage(spoken).addOnSuccessListener{detected->
   if(generation!=expected)return@addOnSuccessListener
   val code=if(detected=="und")language.substringBefore("-") else detected
   val source=TranslateLanguage.fromLanguageTag(code)
   if(source==null){result.text="Idioma no compatible: $code";return@addOnSuccessListener}
   if(source==TranslateLanguage.SPANISH){
    result.text=spoken;history.insert(code,spoken,spoken);return@addOnSuccessListener
   }
   val translator=Translation.getClient(TranslatorOptions.Builder().setSourceLanguage(source).setTargetLanguage(TranslateLanguage.SPANISH).build())
   translator.downloadModelIfNeeded().addOnSuccessListener{
    translator.translate(spoken).addOnSuccessListener{translated->
     if(generation==expected){result.text=translated;history.insert(code,spoken,translated)}
     translator.close()
    }.addOnFailureListener{
     if(generation==expected)result.text="Error de traducción";translator.close()
    }
   }.addOnFailureListener{if(generation==expected)result.text="No se pudo descargar el modelo";translator.close()}
  }.addOnFailureListener{if(generation==expected)result.text="No se pudo detectar el idioma"}
 }
 private fun stop(){
  active=false;generation++
  recognizer?.cancel();recognizer?.destroy();recognizer=null
  toggle.text="PULSA PARA\nTRADUCIR";styleButton(false)
  status.text="Micrófono desactivado"
 }
 private fun showHistory(){
  if(active)stop()
  val entries=history.entries()
  val content=if(entries.isEmpty())"Todavía no hay traducciones" else entries.joinToString("\n\n"){"${it.time} · ${it.lang}\n${it.source}\n→ ${it.target}"}
  AlertDialog.Builder(this).setTitle("Histórico").setMessage(content)
   .setPositiveButton("Cerrar",null)
   .setNeutralButton("Copiar"){_,_->
    val clipboard=getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Histórico",content))
   }
   .setNegativeButton("Borrar"){_,_->
    AlertDialog.Builder(this).setTitle("¿Borrar todo el histórico?").setNegativeButton("Cancelar",null).setPositiveButton("Borrar"){_,_->history.clear()}.show()
   }.show()
 }
 override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray){
  super.onRequestPermissionsResult(requestCode,permissions,grantResults)
  if(requestCode==100&&grantResults.firstOrNull()==PackageManager.PERMISSION_GRANTED)start()
 }
 override fun onPause(){super.onPause();if(active)stop()}
 override fun onDestroy(){stop();detector.close();history.close();super.onDestroy()}
}
