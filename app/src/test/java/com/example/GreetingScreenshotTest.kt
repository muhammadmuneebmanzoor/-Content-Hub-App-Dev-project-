package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.repository.InitialSeedData
import com.example.domain.model.ContentArticle
import com.example.presentation.ContentScreen
import com.example.presentation.ContentState
import com.example.presentation.TelemetryStats
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleArticles = InitialSeedData.getPreloadedArticles().map { entity ->
      ContentArticle(
        id = entity.id,
        title = entity.title,
        body = entity.body,
        tags = entity.tags.split(","),
        likes = entity.likes,
        dislikes = entity.dislikes,
        views = entity.views,
        userId = entity.userId,
        authorName = entity.authorName,
        readTimeMinutes = entity.readTimeMinutes,
        category = entity.category,
        isBookmarked = entity.isBookmarked,
        cachedAtTimestamp = entity.cachedAtTimestamp,
        rawJsonPayload = entity.rawJsonPayload
      )
    }

    val mockState = ContentState(
      isLoading = false,
      articles = sampleArticles,
      filteredArticles = sampleArticles,
      selectedCategory = "All",
      telemetry = TelemetryStats(
        lastSyncLatencyMs = 120,
        lastSyncTimestamp = System.currentTimeMillis(),
        payloadItemCount = sampleArticles.size
      )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        ContentScreen(
          state = mockState,
          onEvent = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

