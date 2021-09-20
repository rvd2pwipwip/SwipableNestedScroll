package com.hdesrosiers.swipablenestedscroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// https://proandroiddev.com/how-to-master-swipeable-and-nestedscroll-modifiers-in-compose-bb0635d6a760

enum class States { EXPANDED, COLLAPSED }

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MyBottomSheet(
        header = { Header(States.EXPANDED) },
        body = { Body() }
      )
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MyBottomSheet(
  header: @Composable () -> Unit,
  body: @Composable () -> Unit
) {
  val swipeableState = rememberSwipeableState(initialValue = States.EXPANDED)
  val scrollState = rememberScrollState()

  BoxWithConstraints {
    val constraintsScope = this
    val maxHeight = with(LocalDensity.current) {
      constraintsScope.maxHeight.toPx()
    }
    Box(
      modifier = Modifier
        .swipeable(
          state = swipeableState,
          orientation = Orientation.Vertical,
          anchors = mapOf(
            0f to States.EXPANDED,
            maxHeight to States.COLLAPSED
          )
        )
        .offset {
          IntOffset(
            x = 0,
            y = swipeableState.offset.value.roundToInt()
          )
        }
        .nestedScroll(
          connection = object : NestedScrollConnection {
            // Implement callbacks here
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
              val delta = available.y
              return if (delta < 0) {
                Offset(
                  x = 0f,
                  y = swipeableState.performDrag(delta)
                )
              } else {
                Offset.Zero
              }
            }

            override fun onPostScroll(
              consumed: Offset,
              available: Offset,
              source: NestedScrollSource
            ): Offset {
              val delta = available.y
              return Offset(
                x = 0f,
                y = swipeableState.performDrag(delta)
              )
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
              return if (available.y < 0 && scrollState.value == 0) {
                swipeableState.performFling(available.y)
                available
              } else {
                Velocity.Zero
              }
            }

            override suspend fun onPostFling(
              consumed: Velocity,
              available: Velocity
            ): Velocity {
              swipeableState.performFling(velocity = available.y)
              return super.onPostFling(consumed, available)
            }
          }
        )
    ) {
      Column(modifier = Modifier.fillMaxHeight()) {
        header()
        Box(
          modifier = Modifier
            .fillMaxWidth()
        ) {
          body()
        }
      }
    }
  }
}

@Composable
fun Header(state: States) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(color = Color.Green),
    contentAlignment = Alignment.CenterStart
  ) {
    Icon(
      imageVector = Icons.Default.Close,
      contentDescription = "Close",
      tint = Color.Black,
      modifier = Modifier
        .padding(10.dp)
        .size(24.dp)
        .clickable {  }
    )
  }
}

@Composable
fun Body() {
  LazyColumn(
    contentPadding = PaddingValues(10.dp)
  ) {
    items(count = 100) {
      ListItem(text = it.toString())
    }
  }
}

@Composable
fun ListItem(text: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(color = Color.LightGray),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      modifier = Modifier.padding(6.dp),
      fontWeight = FontWeight.Bold,
      fontSize = 18.sp
    )
  }
}