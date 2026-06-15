package com.jeiel85.daddymode

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.jeiel85.daddymode.ui.theme.DaddyModeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [35])
class HomeScreenScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun releaseBrandScreenshot() {
    composeTestRule.setContent { DaddyModeTheme { Text("퇴근길 아빠모드") } }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/home-brand.png")
  }
}
