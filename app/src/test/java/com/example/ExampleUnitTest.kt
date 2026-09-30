package com.example

import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testLanguageSeparation() {
    // Verify toggle alternates between VI and EN
    assertEquals(AppLanguage.EN, AppLanguage.VI.other)
    assertEquals(AppLanguage.VI, AppLanguage.EN.other)

    // Verify Vietnamese translations have no bilingual slash and contain proper Vietnamese
    val viPlay = Strings.play(AppLanguage.VI)
    val viHome = Strings.backToHome(AppLanguage.VI)
    val viNext = Strings.nextLevel(AppLanguage.VI)
    assertFalse(viPlay.contains("/"))
    assertFalse(viHome.contains("/"))
    assertFalse(viNext.contains("/"))
    assertEquals("Chơi", viPlay)
    assertEquals("Về Trang Chủ", viHome)

    // Verify English translations have no bilingual slash and contain proper English
    val enPlay = Strings.play(AppLanguage.EN)
    val enHome = Strings.backToHome(AppLanguage.EN)
    val enNext = Strings.nextLevel(AppLanguage.EN)
    assertFalse(enPlay.contains("/"))
    assertFalse(enHome.contains("/"))
    assertFalse(enNext.contains("/"))
    assertEquals("Play", enPlay)
    assertEquals("Back to Home", enHome)
    assertEquals("Next Level", enNext)
  }

  @Test
  fun testAchievementTranslations() {
    assertEquals("Bước Đầu Tiên", Strings.achievementTitle("first_step", AppLanguage.VI))
    assertEquals("First Steps", Strings.achievementTitle("first_step", AppLanguage.EN))
    assertEquals("Nhà Thám Hiểm", Strings.achievementTitle("fifty_levels", AppLanguage.VI))
    assertEquals("Master Explorer", Strings.achievementTitle("fifty_levels", AppLanguage.EN))
  }
}

