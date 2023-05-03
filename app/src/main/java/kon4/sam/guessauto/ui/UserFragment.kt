package kon4.sam.guessauto.ui

import android.Manifest
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kon4.sam.guessauto.App
import kon4.sam.guessauto.BuildConfig

import kon4.sam.guessauto.databinding.FragmentUserBinding
import kon4.sam.guessauto.util.URIPathHelper
import kon4.sam.guessauto.view_model.UserViewModel
import java.io.File
import java.util.*


import androidx.constraintlayout.widget.ConstraintLayout
import kon4.sam.guessauto.R
import kon4.sam.guessauto.databinding.FragmentGameBinding
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat


@AndroidEntryPoint
class UserFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private lateinit var tempImageUrl: String

    private lateinit var _binding: FragmentUserBinding
    private val binding get() = _binding
    companion object {
        const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
        const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    }

    private val requestGalleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Timber.d("Granted $granted")
            if (granted) {
                openGallery()
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                intent?.let {
                    handleImage(intent)
                }
            }
        }

    private val captureImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (!App.user.picture_url.isNullOrEmpty()) {
                    viewModel.removeImageFromS3()
                }
                viewModel.uploadImageToS3(tempImageUrl)
            }
        }

    private val requestCameraPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            Timber.d("Granted $granted")
            if (granted) {
                capturePhoto()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        if (App.user.user_name == null) {
            hideAvatarItems()
        }
        setupObservers()
        setupListeners()
    }

    private fun hideAvatarItems() {
        binding. textView3.isVisible = false
        binding.profilePhotoIcon.isVisible = false
        val params =  binding.textField.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom =  binding.toolbar2.id
        binding.textField.requestLayout()
    }

    private fun showAvatarItems() {
        // TODO: 13.10.2021 переделать ?
        if (! binding.textView3.isVisible) {
            binding.textView3.isVisible = true
            binding.profilePhotoIcon.isVisible = true
            val params =  binding.textField.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom =  binding.profilePhotoIcon.id
            binding.textField.requestLayout()
        }
    }

    private fun blockFields() {
        binding.loadingUsernameProgress.visibility = View.VISIBLE
        binding.requestResultText.visibility = View.GONE
        binding.button.isEnabled = false
        binding.editTextTextPersonName.isEnabled = false
    }

    private fun setupListeners() {
        binding.editTextTextPersonName.doOnTextChanged { text, _, _, _ ->
            if (text!!.length < 4) {
                binding.textField.error = resources.getString(R.string.username_length_short_error)
            } else if (text.length >= 4) {
                binding.textField.error = null
            }
        }
        binding.button.setOnClickListener {
            if ( binding.textField.error.isNullOrEmpty()) {
                blockFields()
                viewModel.updateUserName()
            }
        }
        binding.profilePhotoIcon.setOnClickListener {
            showPopup(it)
        }
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.inflate(R.menu.upload_photo_menu)
        if (App.user.picture_url == null) {
            popup.menu.findItem(R.id.remove_photo).isVisible = false
        }
        if (!requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            popup.menu.findItem(R.id.take_photo).isVisible = false
        }
        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.choose_from_gallery -> {
                    if (hasPermission(READ_MEDIA_IMAGES) || ContextCompat.checkSelfPermission(
                            requireContext(), READ_MEDIA_IMAGES) ==
                        PackageManager.PERMISSION_GRANTED) {
                        openGallery()
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestGalleryPermissionLauncher.launch(READ_MEDIA_IMAGES)
                        } else {
                            requestGalleryPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
                        }

                    }
                }
                R.id.take_photo -> {
                    capturePhoto()
//                    Timber.d("take_photo")
//                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
//                        Timber.d("TIRAMISU")
//                        val takePhotoPermissions = arrayOf(
//                            READ_MEDIA_IMAGES
//                        )
//                        if (hasMultiplePermissions(takePhotoPermissions)) {
//                            Timber.d("hasMultiplePermissions")
//
//                        } else {
//                            Timber.d("dont hasMultiplePermissions")
//                            requestCameraPermissionsLauncher.launch(takePhotoPermissions)
//                        }
//                    } else {
//                        capturePhoto()
//                    }
                }
                R.id.remove_photo -> {
                    removePhoto()
                }
            }
            true
        }
        popup.show()
    }

    private fun hasMultiplePermissions(permissions: Array<String>): Boolean = permissions.all {
        return ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun removePhoto() {
        binding.profilePhotoIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.avatar_default))
        viewModel.removeImageFromS3()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = if (Build.VERSION.SDK_INT <= 28) {
            getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        } else {
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        }
        return File.createTempFile("JPEG_${timeStamp}", ".jpg", storageDir).apply {
            tempImageUrl = absolutePath
        }
    }

    private fun capturePhoto() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Timber.e(ex)
            null
        }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                it
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            captureImageLauncher.launch(intent)
        }
    }

    private fun openGallery() {
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun handleImage(data: Intent) {
        val uri = data.data!!
        if (!App.user.picture_url.isNullOrEmpty()) {
            viewModel.removeImageFromS3()
        }

        val pathFromUri = URIPathHelper().getPath(requireContext(), uri)
        if (pathFromUri != null) {
            viewModel.uploadImageToS3(pathFromUri)
        } else {
            Toast.makeText(requireContext(), "Image path is null", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupObservers() {
        viewModel.userUpdated.observe(viewLifecycleOwner) {
            binding.loadingUsernameProgress.visibility = View.GONE
            binding.button.isEnabled = true
            binding.editTextTextPersonName.isEnabled = true
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    "OK" -> {
                        binding.requestResultText.visibility = View.VISIBLE
                        binding.requestResultText.text = resources.getString(R.string.user_update_success)
                        showAvatarItems()
                    }

                    "User exist" -> {
                        binding.textField.error = resources.getString(R.string.user_already_exists)
                    }

                    else -> {
                        binding.textField.error = resources.getString(R.string.unknown_error, response)
                    }
                }
            }
        }
        viewModel.uploadToS3RequestFinished.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                when (result) {
                    "OK" -> {
                        viewModel.setUserImage()
                    }

                    else -> {
                        binding.requestResultText.text = resources.getString(R.string.unknown_error, result)
                    }
                }
            }
        }
        viewModel.deleteFromS3RequestFinished.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                when (result) {
                    "OK" -> {
                        viewModel.removeImageForUser()
                    }

                    else -> {
                        binding.requestResultText.text = resources.getString(R.string.unknown_error, result)
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar2.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_new_24)
        binding.toolbar2.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
}