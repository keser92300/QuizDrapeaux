package fr.nicolas.keser.quizdrapeaux.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.nicolas.keser.quizdrapeaux.R
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import fr.nicolas.keser.quizdrapeaux.controler.ParamsActivity.Companion.actionParams

/**
 * Display all settings in ParamsActivity
 */

class AdapterParams(val arrayTitles : List<String>, val arrayImages : List<Int>,val runnable: Runnable)
    : RecyclerView.Adapter<AdapterParams.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterParams.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_cell, parent, false)
        return ViewHolder(v,runnable)
    }

    override fun getItemCount(): Int {
        return arrayTitles.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.display(arrayTitles[p1],arrayImages[p1])
    }


    class ViewHolder(itemView: View,val runnable: Runnable) : RecyclerView.ViewHolder(itemView), View.OnClickListener  {

        init {
            itemView.setOnClickListener(this)
        }
        val tv = itemView.findViewById<View>(R.id.tvCell) as TextView
        val imgV = itemView.findViewById<View>(R.id.imageCell) as ImageView

        override fun onClick(v: View?) {
            actionParams = tv.text.toString()
            runnable.run()
        }

        fun display(text : String,res : Int){
            tv.text = text
            imgV.setImageResource(res)
        }
    }

}