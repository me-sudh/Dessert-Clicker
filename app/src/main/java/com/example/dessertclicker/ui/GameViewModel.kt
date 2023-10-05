package com.example.dessertclicker.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.dessertclicker.R
import com.example.dessertclicker.data.Datasource
import com.example.dessertclicker.model.Dessert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel:ViewModel() {


    private val _uiState= MutableStateFlow(GameUiState())

    val uiState:StateFlow<GameUiState> = _uiState.asStateFlow()

    val desserts:List<Dessert> by mutableStateOf(Datasource.dessertList)


//    val currentDessertIndex by remember { mutableStateOf(0) }

    var currentDessertPrice by mutableStateOf(desserts[uiState.value.currentDessertIndex].price)

    var currentDessertImageId by mutableStateOf(desserts[uiState.value.currentDessertIndex].imageId)


//    init {
//        resetGame()
//    }


    fun updateRevenue() {
        _uiState.update { currentState->
            currentState.copy(
                revenue = currentDessertPrice,
                dessertsSold = currentState.dessertsSold.inc()
            )
        }
//        _uiState.value.revenue += currentDessertPrice
//        dessertsSold++

        // Show the next dessert
        val dessertToShow = determineDessertToShow(desserts, uiState.value.dessertsSold)
        currentDessertImageId = dessertToShow.imageId
        currentDessertPrice = dessertToShow.price
    }
    /**
     * Determine which dessert to show.
     */
    fun determineDessertToShow(
        desserts: List<Dessert>,
        dessertsSold: Int
    ): Dessert {
        var dessertToShow = desserts.first()
        for (dessert in desserts) {
            if (dessertsSold >= dessert.startProductionAmount) {
                dessertToShow = dessert
            } else {
                // The list of desserts is sorted by startProductionAmount. As you sell more desserts,
                // you'll start producing more expensive desserts as determined by startProductionAmount
                // We know to break as soon as we see a dessert who's "startProductionAmount" is greater
                // than the amount sold.
                break
            }
        }

        return dessertToShow
    }


    fun shareSoldDessertsInformation(intentContext: Context) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                intentContext.getString(R.string.share_text, uiState.value.dessertsSold,
                    uiState.value.revenue)
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)

        try {
            ContextCompat.startActivity(intentContext, shareIntent, null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                intentContext,
                intentContext.getString(R.string.sharing_not_available),
                Toast.LENGTH_LONG
            ).show()
        }
    }


}