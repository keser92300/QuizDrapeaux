package fr.nicolas.keser.quizdrapeaux

import fr.nicolas.keser.quizdrapeaux.controler.QuizzActivity.Companion.levels
import fr.nicolas.keser.quizdrapeaux.model.*
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class unitTest {

    lateinit var user : Data.User
    lateinit var questionsPacks : List<Data.QuestionsPack>
    lateinit var question : Data.Question

    @Before
    fun setUp(){

        user = Data.User()
        questionsPacks = Data.QuestionsPack().getQuestionsPacks()
        question = Data.Question()
    }

    @Test
    fun getQuestionsArray() {
        val list : List<Data.Question> = Data.Question().getTabOfQuestions()
        assertEquals(4,list.get(3).id)
        assertEquals(MAX_QUESTIONS_IN_ONE_PACK*levels,list.size)
    }

    @Test
    fun getPacksQuestionsArray() {
        assertEquals(levels,questionsPacks.size)
        assertEquals(questionsPacks.get(0).questions.get(0),Data.Question().getTabOfQuestions().get(0))
    }

    @Test
    fun allQuizzPlayed() {
        assertFalse(user.isAllPlayed())
        for(i in questionsPacks.indices){
            user.idsQPUsed.add("i")
        }
        assertEquals(user.idsQPUsed.size, questionsPacks.size)
        assertTrue(user.isAllPlayed())
    }

    @Test
    fun isThatQuizzPlayed() {
        assertFalse(user.isThatIdQuizzPlayed(questionsPacks[1].id))
        user.idsQPUsed.add("0|" + questionsPacks[1].id)
        user.idsQPUsed.add("0|" + questionsPacks[0].id)
        assertFalse(user.isThatIdQuizzPlayed(500))
        assertTrue(user.isThatIdQuizzPlayed(questionsPacks[1].id))
    }

    @Test
    fun getPackWithId() {
        var pack = Data.QuestionsPack().getPackWithId(10000)
        assertTrue(pack==null)
        pack = Data.QuestionsPack().getPackWithId(1)
        assertTrue(pack!=null)
        assertEquals(pack?.questions!![0],questionsPacks[0].questions[0])

    }

    @Test
    fun getQuestionNotPlayed() {
        assertTrue(question.id == 0)
        question = questionsPacks[0].getQuestionNotPlayedInPack(user)
        assertTrue(question.id!=0 && question.id!=-1)
        for(question in questionsPacks[0].questions){
            user.tempQuestionsChecked.add("0" + question.id)
        }
        question = questionsPacks[0].getQuestionNotPlayedInPack(user)
        assertTrue(question.id==-1)
    }

    @Test
    fun getPositionPackInList() {
        var pos = questionsPacks[0].getPositionPackInArrayIds(user.idsQPUsed)
        assertEquals(-1,pos)
        user.idsQPUsed.add("0|" + questionsPacks[0].id)
        user.idsQPUsed.add("0|" + questionsPacks[1].id)
        pos = questionsPacks[1].getPositionPackInArrayIds(user.idsQPUsed)
        assertEquals(1,pos)
    }

}
