package com.example.fileexplorer

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fileexplorer.model.ApiStatus
import com.example.fileexplorer.model.FOLDER_TYPE
import com.example.fileexplorer.model.LightFile
import com.example.fileexplorer.ui.adapter.LightFileListAdapter

// Show data from all light files in viewModel
@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView,
                     data: List<LightFile>?) {
    val adapter = recyclerView.adapter as LightFileListAdapter
    adapter.submitList(data)
}

// Set icon of the file
@BindingAdapter("icon")
fun setIcon(imageView: ImageView, extension: String) {
    imageView.setImageResource(
        when (extension) {
            FOLDER_TYPE -> R.drawable.free_icon_folder_5368657
            "docx" -> R.drawable.free_icon_docx_file_2266784
            "webp" -> R.drawable.free_icon_files_2266862
            "jpg" -> R.drawable.free_icon_jpg_file_2266813
            "jpeg" -> R.drawable.free_icon_jpg_file_2266813
            "mp3" -> R.drawable.free_icon_mp3_file_2266818
            "mp4" -> R.drawable.free_icon_mp4_file_2266820
            "pdf" -> R.drawable.free_icon_pdf_file_2266828
            "png" -> R.drawable.free_icon_png_file_2266832
            "rar" -> R.drawable.free_icon_rar_file_2266840
            "txt" -> R.drawable.free_icon_txt_file_2266857
            "wav" -> R.drawable.free_icon_wav_file_2266860
            "zip" -> R.drawable.free_icon_zip_file_2266866
            else -> R.drawable.free_icon_paper_10209382
        }
    )
}

// Was hash (file) edited?
@BindingAdapter("wasEdited")
fun setFileWasEdited(imageView: ImageView, wasEdited: Boolean) {
    if (wasEdited) imageView.visibility = View.VISIBLE
    else imageView.visibility = View.GONE
}

// For displaying loading image. Not really working(
@BindingAdapter("ApiStatus")
fun checkApiStatus(imageView: ImageView, status: ApiStatus?) {
    when (status!!) {
        ApiStatus.LOADING -> imageView.visibility = View.VISIBLE
        ApiStatus.DONE -> imageView.visibility = View.GONE
    }
}