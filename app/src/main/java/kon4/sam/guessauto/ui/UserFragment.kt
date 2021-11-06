package kon4.sam.guessauto.ui

import android.Manifest
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
import kotlinx.android.synthetic.main.fragment_user.*
import java.io.File
import java.util.*


import androidx.constraintlayout.widget.ConstraintLayout
import kon4.sam.guessauto.R
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat


@AndroidEntryPoint
class UserFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private lateinit var tempImageUrl: String

    private lateinit var binding: FragmentUserBinding
    companion object {
        const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
        const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    }

    private val requestGalleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
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
                val bitmap = BitmapFactory.decodeFile(tempImageUrl)
                viewModel.uploadImageToS3(tempImageUrl)
                //profile_photo_icon.setImageBitmap(bitmap) // решил сделать загрузку через glide
                //val pathFromUri = URIPathHelper().getPath(requireContext(), mUri!!)
               /* if (pathFromUri != null) {
                    viewModel.uploadImageToS3(pathFromUri)
                } else {
                    Toast.makeText(requireContext(), "Image path is null " + mUri!!.path!!, Toast.LENGTH_LONG).show()
                    val file = File(mUri!!.path!!)
                    viewModel.uploadImageToS3(tempImageUrl)
                }*/
            }
        }

    private val requestCameraPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                capturePhoto()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        if (App.user.user_name.isEmpty()) {
            hideAvatarItems()
        }
        setupObservers()
        setupListeners()
    }

    private fun hideAvatarItems() {
        textView3.isVisible = false
        profile_photo_icon.isVisible = false
        val params = textField.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom = toolbar2.id
        textField.requestLayout()
    }

    private fun showAvatarItems() {
        // TODO: 13.10.2021 переделать ?
        if (!textView3.isVisible) {
            textView3.isVisible = true
            profile_photo_icon.isVisible = true
            val params = textField.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = profile_photo_icon.id
            textField.requestLayout()
        }
    }

    private fun blockFields() {
        loadingUsernameProgress.visibility = View.VISIBLE
        requestResultText.visibility = View.GONE
        button.isEnabled = false
        editTextTextPersonName.isEnabled = false
    }

    private fun setupListeners() {
        editTextTextPersonName.doOnTextChanged { text, _, _, _ ->
            if (text!!.length < 4) {
                textField.error = resources.getString(R.string.username_length_short_error)
            } else if (text.length >= 4) {
                textField.error = null
            }
        }
        button.setOnClickListener {
            if (textField.error.isNullOrEmpty()) {
                if (App.user.user_name.isEmpty()) {
                    blockFields()
                    viewModel.setNewUser()
                } else if (App.user.user_name != viewModel.userName.value) {
                    blockFields()
                    viewModel.updateUserName()
                }
            }
        }
        profile_photo_icon.setOnClickListener {
            showPopup(it)
        }
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.inflate(R.menu.upload_photo_menu)
        if (App.user.url == null) {
            popup.menu.findItem(R.id.remove_photo).isVisible = false
        }
        if (!requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            popup.menu.findItem(R.id.take_photo).isVisible = false
        }
        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.choose_from_gallery -> {
                    if (hasPermission(READ_EXTERNAL_STORAGE)) {
                        openGallery()
                    } else {
                        requestGalleryPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
                    }
                }
                R.id.take_photo -> {
                    if (Build.VERSION.SDK_INT <= 28) {
                        val takePhotoPermissions = arrayOf(
                            WRITE_EXTERNAL_STORAGE,
                            READ_EXTERNAL_STORAGE
                        )
                        if (hasMultiplePermissions(takePhotoPermissions)) {
                            capturePhoto()
                        } else {
                            requestCameraPermissionsLauncher.launch(takePhotoPermissions)
                        }
                    } else {
                        capturePhoto()
                    }
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
        profile_photo_icon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.avatar_default))
        viewModel.removeImageFromS3()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = if (Build.VERSION.SDK_INT <= 28) {
            getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        } else {
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            /* requireContext()
                 .getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                 ?.absolutePath + capturedPhotoName
             MediaStore.Images.Media.EXTERNAL_CONTENT_URI*/
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
            //Uri.fromFile(capturedImage)
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
        //profile_photo_icon.setImageURI(uri) решил сделать загрузку через glide
        // TODO: 14.10.2021 удалять старое фото из s3 при загрузке новой фотки? 
        val pathFromUri = URIPathHelper().getPath(requireContext(), uri)
        if (pathFromUri != null) {
            viewModel.uploadImageToS3(pathFromUri)
        } else {
            Toast.makeText(requireContext(), "Image path is null", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupObservers() {
        viewModel.userNameSet.observe(viewLifecycleOwner, {
            loadingUsernameProgress.visibility = View.GONE
            button.isEnabled = true
            editTextTextPersonName.isEnabled = true
            it.getContentIfNotHandled()?.let { response ->
                when (response) {
                    "OK" -> {
                        requestResultText.visibility = View.VISIBLE
                        requestResultText.text = resources.getString(R.string.user_changed)
                        showAvatarItems()
                    }
                    "User exist" -> {
                        textField.error = resources.getString(R.string.user_already_exists)
                    }
                    else -> {
                        textField.error = resources.getString(R.string.unknown_error, response)
                    }
                }
            }
        })
        viewModel.uploadToS3RequestFinished.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                when (result) {
                    "OK" -> {
                        viewModel.setUserImage()
                    } else -> {
                        requestResultText.text = resources.getString(R.string.unknown_error, result)
                    }
                }
            }
        })
        viewModel.deleteFromS3RequestFinished.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                when (result) {
                    "OK" -> {
                        viewModel.removeImageForUser()
                    } else -> {
                        requestResultText.text = resources.getString(R.string.unknown_error, result)
                    }
                }
            }
        })
        viewModel.userImageSet.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                requestResultText.visibility = View.VISIBLE
                when (result) {
                    "OK" -> {
                        requestResultText.text = resources.getString(R.string.photo_changed_success)
                    } else -> {
                        requestResultText.text = resources.getString(R.string.unknown_error, result)
                    }
                }
            }
        })
    }

    private fun setupToolbar() {
        toolbar2.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_new_24)
        toolbar2.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
}