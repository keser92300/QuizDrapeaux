package fr.nicolas.keser.quizdrapeaux.controler

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_CLASS_TEXT
import android.util.Log
import android.view.View
import android.view.WindowManager
import fr.nicolas.keser.quizdrapeaux.model.*
import kotlinx.android.synthetic.main.activity_params.*
import fr.nicolas.keser.quizdrapeaux.R
import android.support.v7.widget.LinearLayoutManager

class ParamsActivity : AppCompatActivity(){

    val TAG = this::class.java.simpleName
    var currentUser = Data.User()
    val NAME_KEY = "name"
    val AGE_KEY = "age"
    var currentAttribute = ""

    /**
     * contains selected setting in ParamsActivity
     */
    companion object { var actionParams = "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_params)

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        supportActionBar?.hide()
        currentUser = getAnyFromExtras(currentUser,intent) as Data.User
        setTypeFaces(Typeface.createFromAsset(assets, MAIN_TYPEFONT),tvParamsTitre)
        initialize()
    }

    fun initialize(){
        actionParams = ""
        displayRecycler()
        displayImageFromPath(imageParams,currentUser.pathImage)
        displayUserInfos()
        imageReturnParams.setOnClickListener { displayEditText(View.GONE,"") }
        imageValidParams.setOnClickListener { update(currentAttribute)}
    }

    /**
     * Display all settings in a RecyclerView
     */

    fun displayRecycler(){
        val listText = listOf<String>(getString(R.string.cell_params_name),getString(R.string.cell_params_age),getString(R.string.cell_params_update_img),
            getString(R.string.cell_params_delete_img),getString(R.string.cell_params_quit),getString(R.string.cell_params_reset),getString(R.string.cell_params_contact),
            getString(R.string.cell_params_share),getString(R.string.cell_params_home))
        val listResImg = listOf(R.drawable.user,R.drawable.birthday,R.drawable.update_image,R.drawable.delete,R.drawable.quit,R.drawable.delete_cross,
            R.drawable.contact, R.drawable.share,R.drawable.return_icon)
        val runActions = Runnable {
            when(actionParams){
                listText[0] -> displayEditText(View.VISIBLE, NAME_KEY)
                listText[1] -> displayEditText(View.VISIBLE, AGE_KEY)
                listText[2] -> PermissionHandler(Runnable { updateUserImage() },this).checkPermissionReadStorage()//checkPermissionReadStorage()
                listText[3] -> removeUserImage()
                listText[4] -> quit()
                listText[5] -> resetApp()
                listText[6] -> contact()
                listText[7] -> share()
                listText[8] -> backToHome()
            }
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = AdapterParams(listText,listResImg,runActions)
    }


    @SuppressLint("SetTextI18n")
    fun displayUserInfos(){
        tvParamsInfos.text = "${currentUser.name} :\n${getString(R.string.age)} : ${currentUser.age} ${getString(R.string.years)}" +
                ", ${getString(R.string.score)} : ${currentUser.score} ${getString(R.string.points)}, " +
                "${getString(R.string.nbr_quizz_played)} : ${currentUser.idsQPUsed.size}."
    }

    /**
     * Display edit text to set infos
     * arg = name of selected setting (name or age etc ... )
     */

    fun displayEditText(visibility : Int, arg: String){
        edTextParams.visibility = visibility
        edTextParams.setText("")
        layoutValidParams.visibility = visibility
        currentAttribute = arg
        if(arg.equals(NAME_KEY)){edTextParams.setHint(getString(R.string.entry_userName)); edTextParams.inputType = TYPE_CLASS_TEXT}
        else if(arg.equals(AGE_KEY)){edTextParams.setHint(getString(R.string.age) + " : "); edTextParams.inputType = TYPE_CLASS_NUMBER}
    }

    /**
     * Update Strings informations selected with arg.
     */

    fun update(arg: String){
        if(arg.equals(NAME_KEY)){
            if(edTextParams.text.toString().length<2){
                makeToast(this,getString(R.string.min_char_edit_text))
                return
            }
            currentUser.name = edTextParams.text.toString()
        }else if(arg.equals(AGE_KEY)){
            if(edTextParams.text.toString().isEmpty()){
                makeToast(this,getString(R.string.no_entrie))
                return
            }
            currentUser.age = edTextParams.text.toString().toInt()
        }
        saveUserValues(this,currentUser)
        displayUserInfos()
        makeToast(this,getString(R.string.change_made))
        displayEditText(View.GONE,"")
    }

    fun updateUserImage(){
        displayEditText(View.GONE,"")
        val imageIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(imageIntent, REQUEST_GALLERY)
    }

    fun removeUserImage(){
        if(!currentUser.pathImage.equals(ERROR)){
            currentUser.pathImage = ERROR
            imageParams.setImageResource(R.drawable.user_no_image)
            saveUserValues(this,currentUser)
        }
    }

    /**
     * Make the score and all the information of the user by default and finish app
     */

    fun resetApp(){
        displayAlertDialog(this,getString(R.string.reset),getString(R.string.reset_text),
            getString(R.string.cancel), txtPositive = getString(R.string.continue_), runPositive = Runnable {
                currentUser = Data.User()
                infosUserAdded = false
                appLaunched = false
                saveUserValues(this,currentUser)
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            })
    }

    fun quit(){
        displayAlertDialog(this,getString(R.string.quit),getString(R.string.quit_question)
            ,getString(R.string.cancel), txtPositive = getString(R.string.quit), runPositive = Runnable {
                saveUserValues(this,currentUser)
                appLaunched = false
                finish()
            })

    }

    fun backToHome(){
        displayAlertDialog(this,null,getString(R.string.back_to_home),getString(R.string.cancel),
            txtPositive = getString(android.R.string.ok), runPositive = Runnable {
                recycler.visibility = View.GONE
                layoutValidParams.visibility = View.GONE
                progressParams.visibility = View.VISIBLE
                saveUserValues(this,currentUser)
                startActivity(getPreparedIntent(this,MainActivity(),currentUser))
                finish()
            })
    }

    fun contact(){
        val mails = arrayOf(ADMIN_MAIL)
        val emailIntent = Intent(android.content.Intent.ACTION_SEND)
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, mails)
        startActivity(Intent.createChooser(emailIntent, ADMIN_MAIL))
    }

    fun share(){
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Score de ${currentUser.name} sur ${getString(R.string.app_name)} : ${currentUser.score} points !\n\n"
                    +"Jettez un coup d\'oeil a mes realisations : " + ADMIN_LINK
        )
        startActivity(shareIntent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            val selectedImageUri = data?.getData()
            val picturePath = getPath(this, selectedImageUri)
            Log.d(TAG,"Picture Path : $picturePath")
            if(!picturePath.equals(ERROR) && !picturePath.equals(currentUser.pathImage)){
                currentUser.pathImage = picturePath
                displayImageFromPath(imageParams,currentUser.pathImage)
                saveUserValues(this,currentUser)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG,"request code : $requestCode")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            updateUserImage()
        } else {
            PermissionHandler(Runnable { updateUserImage() },this).dialPermission()
        }
    }

    override fun onBackPressed() {
        backToHome()
    }
}
