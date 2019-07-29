package fr.nicolas.keser.quizdrapeaux.model

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import fr.nicolas.keser.quizdrapeaux.R
import java.lang.Exception
import java.net.URLEncoder

/**
 * To update questions with good country ID
 * This country id will be used to display the flag of the good answer
 *
 */

class AsyncQuizzHandler(val asyncResponse : AsyncResponse,val tvPourcent : TextView, val progressBarHor: ProgressBar, val layoutLoading: RelativeLayout,
                        val layoutButtons: LinearLayout, val queue: RequestQueue, val context: Context)
    : AsyncTask<Data.QuestionsPack, Int, Data.QuestionsPack>() {

    val TAG = this::class.java.simpleName

    var error = false
    var successOperation = false

    override fun onPreExecute() {
        super.onPreExecute()
        layoutButtons.visibility = View.GONE
        layoutLoading.visibility = View.VISIBLE
    }

    override fun doInBackground(vararg params: Data.QuestionsPack): Data.QuestionsPack {
        try {
            getQuestionPack(params[0])
        } catch (e: Exception) {
            e.printStackTrace()
            layoutButtons.visibility = View.VISIBLE
            layoutLoading.visibility = View.GONE
            error = true
        }
        val durationMax = 120000   // 2 MN
        var durationUsed = 0

        // set progressbar with loading
        var i = 1; var taskFinished = false
        while (i<=100){
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
                publishProgress(i)
            }
            i++
            durationUsed+=100
            if((error || successOperation) && !taskFinished){
                /* this will not waiting for the end of the repeat, if operation is success or
                error it will finish
                */
                i = 99
                taskFinished = true
            }
            if(i<101)
                publishProgress(i)
        }

        /*
        If the progress bar is complete, the user will wait until the duration is equal to the
        maximum duration or receive the error or successfully update.
         */
        while (!error && !successOperation && durationUsed<durationMax){
            Thread.sleep(100)
            durationUsed+=100
        }
        Log.d(TAG,"task duration : $durationUsed")

        if(successOperation) {
            return params[0]
        }else{
            return Data.QuestionsPack(id = -1)
        }
    }


    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        val m = values[0].toString() + "%"
        values[0]?.let { progressBarHor.setProgress(it) }
        tvPourcent.setText(m)
    }

    override fun onPostExecute(result: Data.QuestionsPack) {
        super.onPostExecute(result)
        layoutButtons.visibility = View.VISIBLE
        layoutLoading.visibility = View.GONE
        asyncResponse.processFinish(result)
    }

    /**
     * To recover the table of country identifiers to be able to then compare the values ​​with the good answers of the quiz
     */

    fun getQuestionPack(questionsPack: Data.QuestionsPack){

        countrieTab.clear()
        val url = "http://country.io/names.json"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET,url,null
            , Response.Listener {
                for(indice in it.keys()){
                    countrieTab.add("$indice|${it.getString(indice)}|")
                }
                if(context.getString(R.string.user_langage).equals("EN")){
                    updatePack(getAnswersNamesArray(questionsPack),questionsPack)
                }else{
                    translateCountriesName(questionsPack)
                }
                Log.d(TAG,"Response countries : $it")
            }
            , Response.ErrorListener {
                Log.e(TAG,"Error countries : $it")
                error = true
            })
        queue.add(jsonObjectRequest)

    }

    /**
     * If the default language is not English This method will make an HTTP request
     * to retrieve the translation of the country name of each good answer in order
     * to look for the correct country identifier
     */

    fun translateCountriesName(questionsPack: Data.QuestionsPack){
        var answersTxt = ""
        for(question in questionsPack.questions){
            answersTxt = answersTxt + " " + question.answers[question.indexGoodAnswer]
        }
        translate(answersTxt,context.getString(R.string.user_langage),"EN",questionsPack)
    }

    /**
     * Update appropriate country ID to the array of questionsPacks
     */

    fun updatePack(answers : List<String>, questionsPack: Data.QuestionsPack ){
        for((indice,answer) in answers.withIndex()){
            var find = false; var i = 0
            while(!find && i< countrieTab.size){
                val stringTab = countrieTab[i].split("|")
                if(answer.equals(stringTab[1])){
                    find = true
                    questionsPack.questions[indice].countryCode = stringTab[0]
                }
                i++
            }
        }
        successOperation = true
    }

    /**
     * Return a string list of good answers in english
     * Used to update pack of questions with good coountry Id
     */

    fun getAnswersNamesArray (questionsPack: Data.QuestionsPack) : List<String>{
        val list2 = arrayListOf<String>()
        for(question in questionsPack.questions){
            list2.add(question.answers[question.indexGoodAnswer])
        }
        return list2
    }

    fun translate(text: String, langage : String, langageTarget : String,
                  questionsPack: Data.QuestionsPack = Data.QuestionsPack()) {
        var translated = ""
        val query = URLEncoder.encode(text, "UTF-8")
        val langpair = URLEncoder.encode("$langage|$langageTarget", "UTF-8")
        val url = "http://mymemory.translated.net/api/get?q=$query&langpair=$langpair"
        try {
            val jsonObjectRequest = JsonObjectRequest(Request.Method.GET,url,null
                , Response.Listener {
                    val jsonObject = it.getJSONObject("responseData")
                    translated = jsonObject.getString("translatedText")
                    val stringTab = translated.split(" ")
                    updatePack(stringTab,questionsPack)
                    Log.d(TAG,"Response tranlation request : $it")
                }
                , Response.ErrorListener {
                    error = true
                    Log.e(TAG,"Error tranlation request : $it")
                })
            queue.add(jsonObjectRequest)

        } catch (e: Exception) {
            e.printStackTrace()
            error = true
        }
    }

}