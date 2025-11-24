package com.jpvj.controlcarrito

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.jpvj.controlcarrito.domain.model.MoveDirection
import com.jpvj.controlcarrito.presentation.main.MainUiState
import com.jpvj.controlcarrito.presentation.main.MainViewModel
import android.widget.ImageView

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.AndroidViewModelFactory(application) {
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(application) as T
                }
                return super.create(modelClass, extras)
            }
        }
    }

    private lateinit var tvHealth: TextView
    private lateinit var tvDistance: TextView
    private lateinit var btnHealthcheck: Button
    private lateinit var btnForward: ImageView
    private lateinit var btnBackward: ImageView
    private lateinit var btnLeft: ImageView
    private lateinit var btnRight: ImageView
    private lateinit var btnStop: Button
    private lateinit var btnLights: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        bindListeners()
        observeUiState()
    }

    private fun bindViews() {
        tvHealth = findViewById(R.id.tvHealth)
        tvDistance = findViewById(R.id.tvDistance)
        btnForward = findViewById(R.id.btnForward)
        btnBackward = findViewById(R.id.btnBackward)
        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)
        btnStop = findViewById(R.id.btnStop)
        btnLights = findViewById(R.id.btnLights)
    }

    private fun bindListeners() {
        btnForward.setOnClickListener {
            viewModel.move(MoveDirection.FORWARD)
        }

        btnBackward.setOnClickListener {
            viewModel.move(MoveDirection.BACKWARD)
        }

        btnLeft.setOnClickListener {
            viewModel.move(MoveDirection.LEFT)
        }

        btnRight.setOnClickListener {
            viewModel.move(MoveDirection.RIGHT)
        }

        btnStop.setOnClickListener {
            viewModel.move(MoveDirection.STOP)
        }

        btnLights.setOnClickListener {
            viewModel.toggleLights()
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                renderState(state)
            }
        }
    }

    private fun renderState(state: MainUiState) {
        tvHealth.text = "Estado: ${state.statusText}"
        tvDistance.text = "Distancia: ${state.distanceText}"

        // Cambiar ícono del botón de luces
        val lightsDrawable = if (state.lightsOn) {
            R.drawable.luces_on
        } else {
            R.drawable.luces_off
        }

        btnLights.setImageResource(lightsDrawable)
    }
}
