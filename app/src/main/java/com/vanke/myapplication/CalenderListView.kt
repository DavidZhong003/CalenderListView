package com.vanke.myapplication

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 *
 * @author  doive
 * on 2018/2/2 11:18
 */
class CalenderListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {

    init {
        layoutManager = LinearLayoutManager(context)

    }
}

class CalenderAdapter : RecyclerView.Adapter<VH>(){
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: VH?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}



class VH(rootView: View) :RecyclerView.ViewHolder(rootView)