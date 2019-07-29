package fr.nicolas.keser.quizdrapeaux.model

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import fr.nicolas.keser.quizdrapeaux.R
import java.lang.Exception

/**
 * contains String : country id + | + country name
 */
var countrieTab : ArrayList<String> = ArrayList()

var appLaunched = false
var infosUserAdded = false
var volumeOff = false
var pathImage = ERROR

/**
 *To do some treatments after async task
 */

interface AsyncResponse {
    fun processFinish(output: Data.QuestionsPack)
}

fun makeToast(context: Context, txt : String) {
    context.toast(txt)
}

fun displayImageFromPath(imageView: ImageView, pathImage : String){
    if(pathImage.equals(ERROR)){return}
    val bitmap = BitmapFactory.decodeFile(pathImage)
    imageView.setImageBitmap(bitmap)
}

fun setTypeFaces(mainFont : Typeface,vararg textViews: TextView){
    for(tv in textViews){
        tv.typeface = mainFont
    }
}

fun setViewsVisibility(visibility : Int, vararg views : View){
    for(v in views){
        v.visibility = visibility
    }
}


fun playSound(context: Context, resSound : Int){
    if(!volumeOff) {
        val mediaPlayer = MediaPlayer.create(context, resSound)
        mediaPlayer.start()
    }
}

fun isConnectionOk(context: Context,activity: AppCompatActivity, dial: Boolean):Boolean{
    val connectivityManager=activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo=connectivityManager.activeNetworkInfo
    if(networkInfo!=null && networkInfo.isConnected){
        return true
    }else{
        if(dial){
            dialNoConnection(context)
        }else{
            return false
        }
    }
    return true
}

fun dialNoConnection(context: Context) : Boolean{
    displayAlertDialog(context,null,context.getString(R.string.no_connection_msg),
        context.getString(R.string.return_),null,context.getString(R.string.parameters),
        Intent(Settings.ACTION_WIRELESS_SETTINGS))
    return false
}

/**
 * Return String path folder of the selected image in Android gallery
 */

fun getPath(context: Context, uri: Uri?): String {
    var result: String? = null
    val proj = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = context.getContentResolver().query(uri, proj, null, null, null)
    if (cursor != null) {
        if (cursor!!.moveToFirst()) {
            val column_index = cursor!!.getColumnIndexOrThrow(proj[0])
            result = cursor!!.getString(column_index)
        }
        cursor!!.close()
    }
    if (result == null) {
        result = ERROR
    }
    Log.d("Utils","Resulte path gallery : $result")
    return result
}

fun displayAlertDialog(context: Context, title : String? = null, message : String,
                       txtNegative : String,resIcon : Int? = null, txtPositive : String? = null,
                       intentPositive : Intent? = null, runPositive : Runnable? = null,
                       txtNeutral : String? = null, intentNeutral : Intent? = null,
                       runNeutral : Runnable? = null, runNegative : Runnable? = null){

    try {
        val builder = AlertDialog.Builder(context)
        if (title != null) {
            builder.setTitle(title)
        }
        if (resIcon != null) {
            builder.setIcon(resIcon)
        }
        builder.setMessage(message)
        builder.setNegativeButton(txtNegative) { dialog, which ->
            if (runNegative != null) {
                runNegative.run()
            }
        }
        if (txtPositive != null) {
            builder.setPositiveButton(txtPositive) { dialog, which ->
                if (intentPositive != null) {
                    context.startActivity(intentPositive)
                } else if (runPositive != null) {
                    runPositive.run()
                }
            }
        }
        if (txtNeutral != null) {
            builder.setNeutralButton(txtNeutral) { _, _ ->
                if (intentNeutral != null) {
                    context.startActivity(intentNeutral)
                } else if (runNeutral != null) {
                    runNeutral.run()
                }
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }catch (e : Exception){e.printStackTrace()}
}