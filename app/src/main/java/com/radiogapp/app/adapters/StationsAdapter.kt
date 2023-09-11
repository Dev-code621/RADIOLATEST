package com.radiogapp.app.adapters

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.radiogapp.app.R
import com.radiogapp.app.dialogs.PermissionsDialog
import com.radiogapp.app.models.Stations
import com.radiogapp.app.ui.dashboard.DashboardFragment
import com.radiogapp.app.ui.home.HomeFragmentDirections
import com.radiogapp.app.utils.DialogSource
import java.util.Calendar

class StationsAdapter(
    var requireContext: Activity,
    var stationsList: ArrayList<Stations>?,
    var findNavController: NavController,
    var HomeFragmentDirections: HomeFragmentDirections.Companion,
   var sharedPref: SharedPreferences
) :
    RecyclerView.Adapter<StationsAdapter.StationsViewHolder>() {
    class StationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stationName: TextView = itemView.findViewById(R.id.stationName)
        val viewstatscolor: View = itemView.findViewById(R.id.viewstatscolor)
        val playBt: ImageView = itemView.findViewById(R.id.playbtn)
        val lock: LinearLayoutCompat = itemView.findViewById(R.id.lock)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.stations_item, parent, false)
        return StationsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return stationsList?.size ?: 0
    }

    override fun onBindViewHolder(holder: StationsViewHolder, position: Int) {
        holder.stationName.text = stationsList?.get(position)?.name.toString()
        holder.viewstatscolor.setBackgroundColor(Color.parseColor("#" + stationsList?.get(position)?.color))
        if (stationsList?.get(position)?.ads == true) {
           if (!isCurrentTimeOneHourGreater(sharedPref.getLong("last_shown",Calendar.getInstance().timeInMillis - (60 * 60 * 1000)))){
               holder.lock.visibility = View.INVISIBLE
            }else{
               holder.lock.visibility = View.VISIBLE
           }

        } else {
            holder.lock.visibility = View.INVISIBLE
        }
        if (position == DashboardFragment.currentPlayying){
       holder.playBt.setImageResource(R.drawable.pausebtn)
        }else{
            holder.playBt.setImageResource(R.drawable.playbtns)
        }
        holder.itemView.setOnClickListener {
             if (stationsList?.get(position)?.ads == false){
                 findNavController.navigate(HomeFragmentDirections.actionNavigationHomeToNavigationDashboard(position))
             }
            else if (!isCurrentTimeOneHourGreater(sharedPref.getLong("last_shown",Calendar.getInstance().timeInMillis - (60 * 60 * 1000)))){
                 findNavController.navigate(HomeFragmentDirections.actionNavigationHomeToNavigationDashboard(position))
             }
             else {
                PermissionsDialog(requireContext,{notifyDataSetChanged()}).show()
             }





        }


    }
    fun isCurrentTimeOneHourGreater(timeInMillis: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val oneHourInMillis = 60 * 60 * 1000 // 1 hour in milliseconds

        return currentTime >= timeInMillis + oneHourInMillis
    }

}