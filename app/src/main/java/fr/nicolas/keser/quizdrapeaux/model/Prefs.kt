package fr.nicolas.keser.quizdrapeaux.model

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity

fun saveUserValues(context : Context, user : Data.User){
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = preferences.edit()
    editor.putString("PREFS_NAME",user.name)
    editor.putString("PREFS_MAIL",user.email)
    editor.putInt("PREFS_AGE",user.age)
    editor.putInt("PREFS_SCORE",user.score)
    editor.putBoolean("PREFS_INFOS_ADDED", infosUserAdded)
    editor.putBoolean("PREFS_VOL_OFF", volumeOff)
    editor.putString("PREFS_PATH_IMAGE",user.pathImage)

    editor.putInt("array_quizz_played_size", user.idsQPUsed.size)
    for (i in 0 until user.idsQPUsed.size) {
        editor.putString("array_quizz_played_$i", user.idsQPUsed.get(i))
    }
    editor.putInt("array_temp_questions_size", user.tempQuestionsChecked.size)
    for (i in 0 until user.tempQuestionsChecked.size) {
        editor.putString("array_temp_questions_$i", user.tempQuestionsChecked.get(i))
    }
    editor.putInt("array_countries_size", countrieTab.size)
    for (i in 0 until countrieTab.size) {
        editor.putString("array_countries_$i", countrieTab.get(i))
    }

    editor.commit()
}

fun loadUserValues(context: Context, user: Data.User){

    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    user.name = preferences.getString("PREFS_NAME", ERROR)
    user.email = preferences.getString("PREFS_MAIL", ERROR)
    user.age = preferences.getInt("PREFS_AGE", 0)
    user.score = preferences.getInt("PREFS_SCORE", 0)
    infosUserAdded = preferences.getBoolean("PREFS_INFOS_ADDED", false)
    user.pathImage = preferences.getString("PREFS_PATH_IMAGE", ERROR)
    volumeOff = preferences.getBoolean("PREFS_VOL_OFF",false)

    if(user.idsQPUsed.size>0){
        user.idsQPUsed.clear()
    }
    val size = preferences.getInt("array_quizz_played_size", 0)
    for (i in 0 until size)
        user.idsQPUsed.add( preferences.getString( "array_quizz_played_$i", ERROR))

    if(user.tempQuestionsChecked.size>0){
        user.tempQuestionsChecked.clear()
    }
    val sizeQuest = preferences.getInt("array_temp_questions_size", 0)
    for (i in 0 until sizeQuest)
        user.tempQuestionsChecked.add( preferences.getString( "array_temp_questions_$i", ERROR))

    if(countrieTab.size>0){
        countrieTab.clear()
    }
    val sizeCountries = preferences.getInt("array_countries_size", 0)
    for (i in 0 until sizeCountries)
        countrieTab.add( preferences.getString( "array_countries_$i", ERROR))

}


fun getPreparedIntent(context: Context, targetActivity: AppCompatActivity,vararg objectsToPut : Any) : Intent{
    val intent = Intent(context,targetActivity::class.java)

    for (any in objectsToPut){
        when(any){
            is Data.User -> intent.putExtra(KEY_EXTRAS_USER,any)
            is Data.QuestionsPack -> intent.putExtra(KEY_EXTRAS_PACK,any)
        }
    }
    return intent
}

fun getAnyFromExtras(any: Any,intent: Intent) : Any{
    var anyToreturn = Any()
    if(any is Data.User){
        anyToreturn = intent.getSerializableExtra(KEY_EXTRAS_USER)
    }else if(any is Data.QuestionsPack){
        anyToreturn = intent.getSerializableExtra(KEY_EXTRAS_PACK)
    }
    return anyToreturn
}