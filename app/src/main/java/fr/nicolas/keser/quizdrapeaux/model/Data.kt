package fr.nicolas.keser.quizdrapeaux.model

import android.util.Log
import fr.nicolas.keser.quizdrapeaux.controler.QuizzActivity.Companion.levels
import java.io.Serializable


class Data {

    companion object {
        val TAG = this::class.java.simpleName
    }

    data class User(var name : String = ERROR,
                    var age : Int = 0,
                    var email : String = ERROR,
                    var score : Int = 0,
                    /**Contains String ( Points + | + id ) of questionsPacks already played*/
                    val idsQPUsed :  ArrayList<String> = ArrayList(levels),
                    /**Contains String ( Point(0 or 1) + idQuestion ) of questions played in the current pack */
                    val tempQuestionsChecked :  ArrayList<String> = ArrayList(), //
                    var pathImage : String = ERROR
    ): Serializable{
        fun isAllPlayed()= if(idsQPUsed.size>0 && idsQPUsed.size == QuestionsPack().getTabOfQuestionsPack(Question().getTabOfQuestions()).size) true else false

        fun isThatIdQuizzPlayed(id : Int) : Boolean{
            var i = 0; var find = false
            if(idsQPUsed!=null && idsQPUsed.size>0) {
                while (!find && i < idsQPUsed.size) {
                    if (idsQPUsed.get(i).split("|")[1].equals("$id")) {
                        find = true
                    }
                    i++
                }
            }
            return find
        }
    }


    data class Question(val answers : List<String> = listOf(), val indexGoodAnswer : Int = 0,
                        var countryCode : String = ERROR, var id : Int = 0) : Serializable{

        /**
         * Get all of questions in one tab ( create new questions in this function )
         */

        fun getTabOfQuestions() : List<Question>{

            /*
            All questions are defined here. If they are not in English, they will
            be automatically translated in order to retrieve the country identifiers corresponding to the correct answers.
             */

            val questionsList = listOf(
                Question(listOf("Pays-Bas","Irlande","Slovenie", "Russie"),3),
                Question(listOf("Turquie","Canada","Tunisie", "Suisse"),0),
                Question(listOf("Belgique","Espagne","Argentine", "Chine"),2),
                Question(listOf("Hongrie","Pologne","Ukraine", "Australie"),1),
                Question(listOf("Croatie","Bolivie","Portugal", "Danemark"),2),

                Question(listOf("Paraguay","Honduras","Chili", "Salvador"),2),
                Question(listOf("Roumanie","Argentine","Iran", "Egypte"),3),
                Question(listOf("Senegal","Cameroun","Mali", "Soudan"),0),
                Question(listOf("Mexique","Inde","Luxembourg", "Irelande"),0),
                Question(listOf("Panama","Cuba","Commores", "Haiti"),1),

                Question(listOf("Venezuela","Bolivie","Costa-Rica", "Equateur"),3),
                Question(listOf("Norvege","Finlande","Suede", "Danemark"),1),
                Question(listOf("Ukraine","Lettonie","Moldavie", "Lituanie"),3),
                Question(listOf("Laos","Thailande","Tibet", "Viet-Nam"),0),
                Question(listOf("Emirats arabes unis","Kazakhstan","Dubai", "Madagascare"),1),

                Question(listOf("Ghana","Burundi","Malawi", "Angola"),3),
                Question(listOf("Tonga","Samoa","Panama", "Saint-Marin"),0),
                Question(listOf("Georgie","Estonie","Slovaquie", "Islande"),1),
                Question(listOf("Micronesie","Macedoine","Malte", "Belize"),2),
                Question(listOf("Cambodge", "Mongolie","Tibet","Tadjikistan"),1)
            )

            //Make id for every question
            for((index,value)in questionsList.withIndex()){
                value.id = index +1
            }
            Log.d(TAG,"" + questionsList.size)
            levels = questionsList.size/ MAX_QUESTIONS_IN_ONE_PACK

            return questionsList
        }

    }



    data class QuestionsPack(var id : Int = 0, val questions : ArrayList<Question> = arrayListOf()) : Serializable{

        /**
         * Get arrayList of packs questions
         */

        fun getQuestionsPacks() : List<QuestionsPack>{
            return getTabOfQuestionsPack(Question().getTabOfQuestions())
        }

        fun getTabOfQuestionsPack(questions: List<Question>) : List<QuestionsPack>{
            val packs = ArrayList<QuestionsPack>()
            var count = 0
            for(value in questions){
                if(count>= MAX_QUESTIONS_IN_ONE_PACK){ count = 0 }
                if(count == 0){
                    packs.add(QuestionsPack(0, ArrayList()))
                    packs.get(packs.size-1).id = packs.size
                }
                packs.get(packs.size-1).questions.add(value)
                count++
            }
            return packs
        }

        /**
         * Return object QustionPack with is Id
         */

        fun getPackWithId(id : Int) : QuestionsPack? {
            val packs = getTabOfQuestionsPack(Question().getTabOfQuestions())
            var indice = -1
            for((i,pack) in packs.withIndex()){
                if(pack.id == id){
                    indice = i
                }
            }
            Log.d(TAG,"" + indice)
            return if(indice!=-1) packs[indice] else null
        }

        /**
         * Return position of a question pack in a array of pack's ids
         * If no pack is finded in the array, the position returned is -1
         */

        fun getPositionPackInArrayIds(list : List<String>) : Int{
            var index = -1
            for(i in list.indices){
                if(list[i].split("|")[1].toInt() == this.id)
                    index = i
            }
            Log.d(TAG,"" + index)
            return index
        }

        fun getQuestionNotPlayedInPack(currentUser : User) : Data.Question{
            if(currentUser.tempQuestionsChecked.size==0){
                return this.questions[0]
            }
            for(question in this.questions){
                var find =false
                for(string in currentUser.tempQuestionsChecked){
                    if(!find && string.substring(1).equals("" +question.id)){
                        find = true
                    }
                }
                if(!find){
                    Log.d(TAG,"Not played : " + question.id)
                    return question
                }
            }
            Log.d(TAG,"All played")
            return Data.Question(id = -1)
        }
    }

}