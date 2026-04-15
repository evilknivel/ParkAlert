package de.parktimer.app.automotive

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.core.graphics.drawable.IconCompat
import de.parktimer.app.R

/**
 * Car app screen that shows a 5-item grid for parking duration selection.
 *
 * Displayed as the first screen when the user opens ParkTimer via Android Auto.
 */
class SelectDurationScreen(carContext: CarContext) : Screen(carContext) {

    private val durations = listOf(15, 30, 60, 90, 120)

    override fun onGetTemplate(): Template {
        val items = durations.map { minutes ->
            val label = "$minutes Min"
            GridItem.Builder()
                .setTitle(label)
                .setImage(
                    CarIcon.Builder(
                        IconCompat.createWithResource(carContext, R.drawable.ic_parking)
                    ).build(),
                    GridItem.IMAGE_TYPE_ICON
                )
                .setOnClickListener {
                    screenManager.push(TimerScreen(carContext, minutes))
                }
                .build()
        }

        val itemList = ItemList.Builder().apply {
            items.forEach { addItem(it) }
        }.build()

        return GridTemplate.Builder()
            .setTitle(carContext.getString(R.string.app_name))
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(itemList)
            .build()
    }
}
