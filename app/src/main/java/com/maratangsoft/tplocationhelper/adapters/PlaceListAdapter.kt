package com.maratangsoft.tplocationhelper.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.maratangsoft.tplocationhelper.R
import com.maratangsoft.tplocationhelper.activities.PlaceUrlActivity
import com.maratangsoft.tplocationhelper.databinding.RvItemListFragmentBinding
import com.maratangsoft.tplocationhelper.model.Place

class PlaceListAdapter(val context:Context, var documents:MutableList<Place>):Adapter<PlaceListAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(context).inflate(R.layout.rv_item_list_fragment, parent, false)
        return VH(itemView)
    }

    inner class VH(itemView:View): ViewHolder(itemView){
        init {
            itemView.setOnClickListener {
                //아이템뷰 클릭시 장소에 대한 세부정보 웹사이트를 보여주는 화면으로 이동
                val intent = Intent(context, PlaceUrlActivity::class.java)
                intent.putExtra("place_url", documents[adapterPosition].place_url)
                context.startActivity(intent)
            }
        }
        val binding by lazy { RvItemListFragmentBinding.bind(itemView) }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val place = documents[position]

        holder.binding.tvPlaceName.text = place.place_name
        holder.binding.tvAddress.text = if (place.road_address_name=="") place.address_name else place.road_address_name
        holder.binding.tvDistance.text = "${place.distance}m"
    }

    override fun getItemCount(): Int = documents.size
}