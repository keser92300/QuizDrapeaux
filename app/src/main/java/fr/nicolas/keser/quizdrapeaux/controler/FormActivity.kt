package fr.nicolas.keser.quizdrapeaux.controler

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_form.*
import android.util.Log
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import fr.nicolas.keser.quizdrapeaux.R
import fr.nicolas.keser.quizdrapeaux.model.*


class FormActivity : AppCompatActivity(), View.OnClickListener {

    val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(fr.nicolas.keser.quizdrapeaux.R.layout.activity_form)
        supportActionBar?.hide()
        pathImage = ERROR
        initOnclickListener(listOf(buttonForm,imageReturnForm,imageValidForm))
    }

    fun initOnclickListener(listViews : List<View>){for (v in listViews) v.setOnClickListener(this)}

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.buttonForm -> PermissionHandler(Runnable { updateImage() },this).checkPermissionReadStorage()
            R.id.imageReturnForm -> quit()
            R.id.imageValidForm -> validInfos()
        }
    }

    fun validInfos(){

        fun isEmptyEdit(editText: EditText):Boolean{if(editText.text.isEmpty()){ editText.setError(getString(R.string.text_requiered));return true}else{ return false}}
        fun isWrongSizeEdit(editText: EditText):Boolean{if(editText.text.length<2){ editText.setError(getString(R.string.min_char_edit_text));return true}else{ return false}}

        if(isEmptyEdit(edTextNameForm)){return}
        if(isEmptyEdit(edTextAgeForm)){return}
        if(isWrongSizeEdit(edTextNameForm)){return}

        Log.d(TAG,"${edTextNameForm.text.toString()} / ${edTextAgeForm.text.toString()}")
        val intent = Intent()
        intent.putExtra("name",edTextNameForm.text.toString())
        intent.putExtra("age",edTextAgeForm.text.toString())
        intent.putExtra("img",pathImage)
        setResult(Activity.RESULT_OK,intent)
        finish()
    }

    fun quit(){
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    fun updateImage() = if(pathImage == ERROR) selectImageFromGallery() else removeImageAdded()

    fun selectImageFromGallery(){
        val imageIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(imageIntent, REQUEST_GALLERY)
    }

    fun removeImageAdded(){
        imageForm.setImageResource(R.drawable.user_no_image)
        buttonForm.text = getString(R.string.add_image)
        pathImage = ERROR
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG,"Permission request code : $requestCode")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            updateImage()
        } else {
            PermissionHandler(Runnable { updateImage() },this).dialPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            val selectedImageUri = data?.getData()
            val picturePath = getPath(this, selectedImageUri)
            Log.d(TAG,"picture path : $picturePath")
            if(!picturePath.equals(ERROR) && !picturePath.equals(pathImage)){
                pathImage = picturePath
                displayImageFromPath(imageForm,pathImage)
                buttonForm.text = getString(R.string.cell_params_delete_img)
            }
        }
    }
}
