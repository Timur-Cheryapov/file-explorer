package com.example.fileexplorer.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fileexplorer.databinding.ListItemLightFileBinding
import com.example.fileexplorer.model.LightFile

// List adapter for files which are always displayed on the screen
class LightFileListAdapter(
    private val clickListener: (LightFile) -> Unit
) : ListAdapter<LightFile, LightFileListAdapter.LightFileViewHolder>(DiffCallBack) {

    class LightFileViewHolder(
        private var binding: ListItemLightFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(lightFile: LightFile) {
            binding.lightFile = lightFile
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LightFileViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LightFileViewHolder(
            ListItemLightFileBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LightFileViewHolder, position: Int) {
        val lightFile = getItem(position)
        holder.itemView.setOnClickListener {
            clickListener(lightFile)
        }
        holder.bind(lightFile)
    }

    companion object DiffCallBack : DiffUtil.ItemCallback<LightFile>() {
        override fun areItemsTheSame(oldItem: LightFile, newItem: LightFile): Boolean {
            /*
            Can return false in order to disable strange animations
            in the recyclerView, but will affect the performance.
             */
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: LightFile, newItem: LightFile): Boolean {
            return oldItem == newItem
        }

    }
}