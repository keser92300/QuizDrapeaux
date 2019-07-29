package fr.nicolas.keser.quizdrapeaux.model

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

class PermissionHandler(val runnable: Runnable, val context: Context) : AppCompatActivity() {

    fun checkPermissionReadStorage(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(context,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_READ_STORAGE)
            } else {
                ActivityCompat.requestPermissions(context,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_READ_STORAGE)
            }
        } else {
            runnable.run()
        }
    }

    fun dialPermission(){
        displayAlertDialog(context,"Permission requise","Vous devez accepter l\'autorisation " +
                "requise pour telecharger une image depuis votre appareil.","Refuser",
            txtPositive = "Accepter", runPositive = Runnable { checkPermissionReadStorage() })
    }
}