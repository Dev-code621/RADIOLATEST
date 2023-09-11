package com.radiogapp.app.adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.radiogapp.app.R
import com.radiogapp.app.models.Contacts

class ContactsAdapter() :
RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {
    var contactsList: ArrayList<Contacts>? = null
        set(value){
            field = value
            notifyDataSetChanged()
        }
    class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
       val name = itemView.findViewById<TextView>(R.id.contacts_name)
       val image = itemView.findViewById<ImageView>(R.id.contact_icon)
        val click = itemView.findViewById<ConstraintLayout>(R.id.click)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.contacts_item, parent, false)
        return ContactsViewHolder(view)
    }

    override fun getItemCount(): Int {
       return  contactsList?.get(0)?.contacts?.size ?:0
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val getname: String = contactsList?.get(0)?.contacts?.get(position)?.name ?: ""
        val geturls: String = contactsList?.get(0)?.contacts?.get(position)?.link ?: ""
        Log.i("tariq", "onComplete: $getname")
        try {
            holder.name.setText(getname)
            if (getname == "Instagram") {
                holder.image.setImageResource(R.drawable.inst)
            } else if (getname == "Facebook") {
                holder.image.setImageResource(R.drawable.fb)
            } else if (getname == "Twitter") {
                holder.image.setImageResource(R.drawable.tw)
            } else if (getname == "Youtube") {
                holder.image.setImageResource(R.drawable.yt)
            } else if (getname == "eMail") {
                holder.image.setImageResource(R.drawable.em)
            } else if (getname == "WhatsApp") {
                holder.image.setImageResource(R.drawable.what)
            } else {
                holder.image.setImageResource(R.drawable.email)
            }

        } catch (e: Exception) {
        }
        holder.click.setOnClickListener(View.OnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(geturls)
            holder.image.context.startActivity(i)
        })

    }


}