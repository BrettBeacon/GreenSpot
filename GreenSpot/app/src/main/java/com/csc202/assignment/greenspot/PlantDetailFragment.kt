package com.csc202.assignment.greenspot

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.csc202.assignment.greenspot.databinding.FragmentPlantDetailBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import getScaledBitmap
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import java.util.UUID

private const val DATE_FORMAT = "EEE, MMM, dd"

class PlantDetailFragment : Fragment() {

    private var _binding: FragmentPlantDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val args: PlantDetailFragmentArgs by navArgs()

    private val plantDetailViewModel: PlantDetailViewModel by viewModels {
        PlantDetailViewModelFactory(args.plantId)
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlantDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.apply {
            plantTitle.doOnTextChanged { text, _, _, _ ->
                plantDetailViewModel.updatePlant { oldPlant ->
                    oldPlant.copy(title = text.toString())
                }
            }
            plantPlace.doOnTextChanged { text, _, _, _ ->
                plantDetailViewModel.updatePlant { oldPlant ->
                    oldPlant.copy(place = text.toString())
                }
            }

            plantShared.setOnCheckedChangeListener { _, isChecked ->
                plantDetailViewModel.updatePlant { oldPlant ->
                    oldPlant.copy(isShared = isChecked)
                }
            }

            plantCamera.setOnClickListener {
                photoName = "IMG_${Date()}.JPG"
                val photoFile = File(requireContext().applicationContext.filesDir, photoName)
                val photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.csc202.assignment.greenspot.fileprovider",
                    photoFile
                )

                takePhoto.launch(photoUri)
            }

            val captureImageIntent = takePhoto.contract.createIntent(
                requireContext(),
                Uri.parse("")
            )
            plantCamera.isEnabled = canResolveIntent(captureImageIntent)

            getGps.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                    Log.d("GPS", "Have permissions. Try to get a location")
                }
                if (GoogleApiAvailability.getInstance()
                        .isGooglePlayServicesAvailable(requireContext()) == ConnectionResult.SUCCESS) {
                    // Get Location
                    fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener { location: Location? ->
                            location?.let {
                                plantDetailViewModel.updatePlant { oldPlant ->
                                    oldPlant.copy(latDouble = location.latitude, longDouble = location.longitude)
                                }
                            }
                            Log.d("GPS", "Got a location: " + location)
                        }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                plantDetailViewModel.plant.collect { plant ->
                    plant?.let { updateUi(it) }
                }
            }
        }

        setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->
            val newDate =
                bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            plantDetailViewModel.updatePlant { it.copy(date = newDate) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi(plant: Plant) {
        binding.apply {
            if (plantTitle.text.toString() != plant.title) {
                plantTitle.setText(plant.title)
            }
            if (plantPlace.text.toString() != plant.place) {
                plantPlace.setText(plant.place)
            }
            plantDate.text = plant.date.toString()
            plantDate.setOnClickListener {
                findNavController().navigate(
                    PlantDetailFragmentDirections.selectDate(plant.date)
                )
            }

            plantShared.isChecked = plant.isShared

            plantReport.setOnClickListener {
                val reportIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getPlantReport(plant))
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.plant_report_subject)
                    )
                }

                val chooserIntent = Intent.createChooser(
                    reportIntent,
                    getString(R.string.share_plant_report)
                )
                startActivity(chooserIntent)
            }

            deletePlant.setOnClickListener {
                // TODO
            }

            updatePhoto(plant.photoFileName)

        }
    }

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto ->
        if (didTakePhoto && photoName != null) {
            plantDetailViewModel.updatePlant { oldPlant ->
                oldPlant.copy(photoFileName = photoName)
            }
        }
    }

    private var photoName: String? = null

    private fun getPlantReport(plant: Plant): String {
        val sharedString = if (plant.isShared) {
            getString(R.string.plant_report_shared)
        } else {
            getString(R.string.plant_report_not_shared)
        }

        val dateString = DateFormat.format(DATE_FORMAT, plant.date).toString()

        return getString(
            R.string.plant_report,
            plant.title, plant.place, dateString, sharedString
        )
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        return resolvedActivity != null
    }

    private fun updatePhoto(photoFileName: String?) {
        if (binding.plantPhoto.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }

            if (photoFile?.exists() == true) {
                binding.plantPhoto.doOnLayout { measuredView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    )
                    binding.plantPhoto.setImageBitmap(scaledBitmap)
                    binding.plantPhoto.tag = photoFileName
                }
            } else {
                binding.plantPhoto.setImageBitmap(null)
                binding.plantPhoto.tag = null
            }
        }
    }
}