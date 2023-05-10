package com.example.fileexplorer.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.example.fileexplorer.BaseApplication
import com.example.fileexplorer.R
import com.example.fileexplorer.databinding.FragmentOverviewBinding
import com.example.fileexplorer.model.ASCEND_ORDER
import com.example.fileexplorer.model.DESCEND_ORDER
import com.example.fileexplorer.model.DataTypes
import com.example.fileexplorer.model.FOLDER_TYPE
import com.example.fileexplorer.ui.adapter.LightFileListAdapter
import com.example.fileexplorer.ui.viewmodel.FileViewModel
import com.example.fileexplorer.ui.viewmodel.FileViewModelFactory
import java.io.File

class OverviewFragment : Fragment(), MenuProvider {

    // Create viewModel
    private val viewModel: FileViewModel by activityViewModels {
        FileViewModelFactory(
            (activity?.application as BaseApplication).database.hashedFileDao()
        )
    }

    // Data binding
    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!

    // If the user pressed the share icon
    private var wantToShare = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Keep callbacks consistent when app is rotated, etc...
        addCallbacks()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // On item listener for adapter
        val adapter = LightFileListAdapter { lightFile ->
            Log.d("Adapter", "Adapter chose ${lightFile.name}")
            if (lightFile.extension == FOLDER_TYPE) {
                if (wantToShare) {
                    // User wanna share the file
                    Toast.makeText(
                        requireContext(),
                        "Can't chose folder for sharing",
                        Toast.LENGTH_SHORT
                    ).show()
                    wantToShare = false
                } else {
                    // User just wanted to open the file
                    viewModel.updateDatabase(
                        path = lightFile.path,
                        shouldUpdateDatabase = true
                    )
                    // Add old directory to the stack
                    viewModel.pathStack.addLast(lightFile.directory)
                    addCallback(lightFile.directory)
                }
            } else {
                // If the file was chosen, clear new hash icon
                lightFile.wasEdited = false
                // Does user really want to share?
                if (wantToShare) shareFile(lightFile.path)
                else openFile(lightFile.path)
            }
        }

        binding.recyclerView.adapter = adapter
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu, menu)
        Log.d("Menu", "Menu inflated")
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        Log.d("Menu", "Menu chose ${menuItem.title}")
        // Cases for handling menu clicks
        when (menuItem.title) {
            activity?.getString(R.string.sort_by) -> {
                if (viewModel.order == ASCEND_ORDER) {
                    viewModel.order = DESCEND_ORDER
                    menuItem.setIcon(R.drawable.baseline_arrow_downward_24)
                } else {
                    viewModel.order = ASCEND_ORDER
                    menuItem.setIcon(R.drawable.baseline_arrow_upward_24)
                }
            }

            activity?.getString(R.string.name) -> viewModel.sortBy = DataTypes.NAME
            activity?.getString(R.string.size) -> viewModel.sortBy = DataTypes.SIZE
            activity?.getString(R.string.date) -> viewModel.sortBy = DataTypes.DATE
            activity?.getString(R.string.extension) -> viewModel.sortBy = DataTypes.EXTENSION
            activity?.getString(R.string.share) -> {
                wantToShare = true
                Toast.makeText(requireContext(), "Chose a file to share", Toast.LENGTH_SHORT).show()
            }
        }
        // And of course update the order from viewModel
        viewModel.updateDatabase()
        return true
    }

    // Keep callbacks consistent when app is rotated, etc...
    private fun addCallbacks() {
        /*
        I want user to double tap back in order to exit the app
        else it would've been much easier :)
         */
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            Toast.makeText(
                requireContext(),
                "To exit press back one more time",
                Toast.LENGTH_SHORT
            ).show()
            if (isEnabled) isEnabled = false
        }
        viewModel.pathStack.forEach {
            addCallback(it)
        }
    }

    // Add callbacks for each new path
    private fun addCallback(path: String) {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            viewModel.updateDatabase(
                path = path,
                shouldUpdateDatabase = true
            )
            Log.d("Menu", "New l.f., called callback")
            if (isEnabled) {
                isEnabled = false
            }
        }
    }

    // Handle open file event
    private fun openFile(path: String) {
        val file = File(path)
        val mime: String? =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase())

        val intent = Intent()
        val uri: Uri
        if (mime != null) {
            try {
                uri = FileProvider.getUriForFile(
                    requireContext(),
                    activity?.packageName + ".provider",
                    file
                )
                intent.apply {
                    action = Intent.ACTION_VIEW
                    setDataAndTypeAndNormalize(uri, mime)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to open a file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle share file event
    private fun shareFile(path: String) {
        wantToShare = false
        val file = File(path)
        val mime: String? =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase())

        val intent = Intent()
        val uri: Uri
        if (mime != null) {
            try {
                uri = FileProvider.getUriForFile(
                    requireContext(),
                    activity?.packageName + ".provider",
                    file
                )
                intent.apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    setTypeAndNormalize(mime)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to share file", Toast.LENGTH_SHORT).show()
            }
        }
    }
}