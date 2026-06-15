package com.jeiel85.daddymode

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppIdentityTest {

  @Test
  fun appNameMatchesReleaseBrand() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    assertEquals("아빠모드", context.getString(R.string.app_name))
  }
}
