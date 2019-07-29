package fr.nicolas.keser.quizdrapeaux.controler

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import fr.nicolas.keser.quizdrapeaux.model.*
import android.app.Activity
import android.graphics.Typeface
import android.util.Log
import fr.nicolas.keser.quizdrapeaux.R

class MainActivity : AppCompatActivity() {

    val TAG = this::class.java.simpleName
    var currentUser = Data.User()
    val REQUEST_FORM = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(fr.nicolas.keser.quizdrapeaux.R.layout.activity_main)

        supportActionBar?.hide()
        if(!appLaunched){
            loadUserValues(this,currentUser)
            appLaunched = true
        }else{
            currentUser = getAnyFromExtras(currentUser,intent) as Data.User
        }
        initialize()
        setTypeFaces(Typeface.createFromAsset(assets, MAIN_TYPEFONT),textView)
        button.setOnClickListener {checkBeforeQuizz()}
        button2.setOnClickListener {displayScore()}
        button3.setOnClickListener {goToParams()}
    }

    fun initialize() = if(!infosUserAdded) startActivityForResult(Intent(this,FormActivity::class.java),REQUEST_FORM)else{}

    fun goToActivity(intent: Intent){
        progressMain.visibility = View.VISIBLE
        val viewsList = listOf<View>(button,button2,button3)
        for(view in viewsList){
            view.isEnabled = false
        }
        startActivity(intent)
        finish()
    }

    fun goToQuizz(pack : Data.QuestionsPack) = goToActivity(getPreparedIntent(this,QuizzActivity(),currentUser,pack))
    fun goToParams()  = goToActivity(getPreparedIntent(this,ParamsActivity(),currentUser))


    /**
     * Check if all the quizzes are played or not. if they are not played, the quiz can start otherwise
     * an alertDialog will be displayed to reset all the quizzes
     */

    fun checkBeforeQuizz(){

        if(currentUser.isAllPlayed()){
            displayAlertDialog(this,null,getString(R.string.no_more_quiz),getString(R.string.cancel),
                txtPositive = getString(android.R.string.ok) + " !", runPositive = Runnable {
                    currentUser.idsQPUsed.clear() ; currentUser.score = 0
                    saveUserValues(this,currentUser)
                    checkBeforeQuizz()
                })
            return
        }
        if(currentUser.idsQPUsed.size<1){
            goToQuizz(Data.QuestionsPack().getQuestionsPacks()[0])
        }else{
            /*
            the size of idsQPUsed is equal to the first id not played of the array of questions packs
             */
            val pack = Data.QuestionsPack().getPackWithId(currentUser.idsQPUsed.size +1)
            Log.d(TAG,"${pack?.id}")
            if(pack!=null) {
                goToQuizz(pack)
            }else(makeToast(this,getString(R.string.error)))
        }
    }

    fun displayScore(){
        fun getScoreText() : String{
            val totalQuestionsPlayed = (currentUser.idsQPUsed.size* MAX_QUESTIONS_IN_ONE_PACK) + currentUser.tempQuestionsChecked.size
            var txt =  if(currentUser.score>0)"${getString(R.string.total_pts)} : ${currentUser.score} / $totalQuestionsPlayed\n\n" else getString(R.string.no_score_available)
            for(string in currentUser.idsQPUsed){
                txt = txt +"${getString(R.string.quiz)} ${string.split("|")[1]} : ${string.split("|")[0]} ${getString(R.string.points)} / $MAX_QUESTIONS_IN_ONE_PACK\n\n"
            }
            Log.d(TAG,"msg : $txt")
            return txt
        }
        displayAlertDialog(this,"${getString(R.string.score)} : ", getScoreText(),getString(R.string.return_))
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            currentUser.pathImage = data?.getStringExtra("img")!!
            currentUser.name = data?.getStringExtra("name")!!
            currentUser.age = data?.getStringExtra("age")!!.toInt()
            infosUserAdded = true
            saveUserValues(this,currentUser)
        }else{
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        isConnectionOk(this,this,true)
    }
}
