package com.den.shak.colorblindtest

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    // Индекс текущего изображения и набора ответов
    private var listIndex = 0

    // Списки значений кнопок и правильных ответов для тестов
    private val buttonValues = listOf(
        listOf(10, 13, 12), listOf(8, 5, 3), listOf(9, 6, 5), listOf(70, 26, 29),
        listOf(2, 5, 7), listOf(9, 3, 5), listOf(15, 11, 17), listOf(9, 6, 2),
        listOf(3, 2, 6), listOf(75, 55, 97), listOf(9, 6, 5), listOf(7, 2, 9),
        listOf(5, 2, ""), listOf(7, 2, ""), listOf(26, 6, 2),
        listOf(2, 42, 4), listOf(5, 3, 35), listOf(96, 6, 9)
    )
    private val rightAnswers = listOf(12, 8, 6, 29, 5, 3, 15, 2, 6, 97, 5, 7, "", "", 26, 42, 35, 96)
    private val protanopiaAnswers = listOf(6, 2, 5, 6)

    private var coorect = 0 // Количество правильных ответов
    private var protanopia = 0 // Количество ответов для протанопии
    private var deuteranopia = 0 // Количество ответов для дейтеранопии

    // Элементы пользовательского интерфейса
    private lateinit var images: List<Drawable>
    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var buttonRepeat: Button
    private lateinit var numberView: TextView
    private lateinit var textView: TextView
    private lateinit var cardView: MaterialCardView
    private lateinit var resultCard: MaterialCardView

    private lateinit var adContainerView: BannerAdView
    private lateinit var bannerAd: BannerAdView

    private var interstitialAd: InterstitialAd? = null
    private var interstitialAdLoader: InterstitialAdLoader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация рекламного SDK Yandex
        MobileAds.initialize(this) {}
        // Загрузка межстраничных рекламных объявлений должна происходить после инициализации SDK
        interstitialAdLoader = InterstitialAdLoader(this).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    // Реклама успешно загружена, можно показывать
                }
                override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                    // Ошибка при загрузке рекламы
                }
            })
        }

        // Загрузка межстраничной рекламы
        loadInterstitialAd()

        setContentView(R.layout.activity_main)

        // Установка отступов для главного View с учетом системных панелей
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализация элементов интерфейса
        imageView = findViewById(R.id.imageView)
        resultTextView = findViewById(R.id.resultTextView)
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)
        numberView = findViewById(R.id.numberView)
        textView = findViewById(R.id.textView)
        cardView = findViewById(R.id.cardView)
        buttonRepeat = findViewById(R.id.buttonRepeat)
        resultCard = findViewById(R.id.resultTextCard)

        // Установка обработчиков нажатий для кнопок
        button1.setOnClickListener { checkAnswer(0)  }
        button2.setOnClickListener { checkAnswer(1) }
        button3.setOnClickListener { checkAnswer(2) }
        buttonRepeat.setOnClickListener { repeat() }

        // Загрузка изображений
        loadImages()

        // Отображение текущего изображения и кнопок
        displayCurrentImageAndButtons()

        // Отслеживание изменения размера контейнера для рекламы и загрузка баннера
        adContainerView = this.findViewById<BannerAdView>(R.id.ad_container_view)
        adContainerView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Удаляем слушатель после первого вызова
                adContainerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // Загружаем баннерную рекламу с вычисленным размером
                bannerAd = loadBannerAd(adSize)
            }
        })
    }

    // Создание меню
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Обработка нажатий на элементы меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Переход к экрану с инструкцией
            R.id.instr -> {
                val intent = Intent(this, ManualActivity::class.java)
                startActivity(intent)
                return true
            }
            // Переход к экрану с настройками
            R.id.setting -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            // Открытие диалогового окна "Описание"
            R.id.about -> {
                // Получаем информацию о пакете
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                // Чтение версии приложения
                val versionName = packageInfo.versionName
                val text = getString(R.string.AboutText1) + " " + versionName + getString(R.string.AboutText2)

                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.MenuAbout)
                    .setMessage(text)
                    .setNegativeButton(R.string.AboutButton) { dialog, _ -> dialog.cancel() }
                    .show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun loadImages() {
        images = (1..18).mapNotNull { i ->
            val resId = resources.getIdentifier("image$i", "drawable", packageName)
            if (resId != 0) ContextCompat.getDrawable(this, resId) else null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayCurrentImageAndButtons() {
        imageView.setImageDrawable(images[listIndex])
        // Получаем значения для текущих кнопок
        val currentValues = buttonValues[listIndex].toMutableList()
        // Устанавливаем строковые ресурсы для элементов, где это нужно
        if (listIndex == 12 || listIndex == 13) {
            currentValues[2] = getString(R.string.Nothing)
        }
        button1.text = currentValues[0].toString()
        button2.text = currentValues[1].toString()
        button3.text = currentValues[2].toString()
        numberView.text = getString(R.string.test) + (listIndex + 1).toString() + getString(R.string.fom_18)
    }

    private fun checkAnswer(button: Int) {
        vibratePhone()

        if (buttonValues[listIndex][button] == rightAnswers[listIndex]) {
            coorect++
        } else if (listIndex >= 14) {
            // Увеличиваем счетчик протанопии/дейтеранопии и отображаем результат
            if (buttonValues[listIndex][button] == protanopiaAnswers[listIndex - 14]) {
                protanopia++
            } else {
                deuteranopia++
            }
        }

        listIndex++
        if (listIndex == images.size) {
            if (coorect == 18)
                resultTextView.text = getString(R.string.result_good)
            else {
                if (protanopia > deuteranopia) {
                    resultTextView.text = getString(R.string.result_protanopia)
                } else if (protanopia < deuteranopia) {
                    resultTextView.text = getString(R.string.result_deuteranopia)
                } else {
                    resultTextView.text = getString(R.string.reslt_not_good)
                }
            }
            end()
            showAd()

            // Запускаем следующую задачу после небольшой задержки
            Handler(Looper.getMainLooper()).postDelayed({
                showResult()
            }, 1500) // Задержка в миллисекундах (1.5 секунды)
        } else {
            displayCurrentImageAndButtons()
        }
    }

    private fun end() {
        numberView.visibility = View.INVISIBLE
        cardView.visibility = View.INVISIBLE
        textView.visibility = View.INVISIBLE
        button1.visibility = View.INVISIBLE
        button2.visibility = View.INVISIBLE
        button3.visibility = View.INVISIBLE
    }

    private fun showResult() {
        // Отображаем результат и скрываем остальные элементы интерфейса
        resultCard.visibility = View.VISIBLE
        buttonRepeat.visibility = View.VISIBLE
    }

    private fun repeat() {
        vibratePhone()
        // Сбрасываем значения и начинаем тест заново
        listIndex = 0
        coorect = 0
        protanopia = 0
        deuteranopia = 0
        displayCurrentImageAndButtons()

        resultCard.visibility = View.INVISIBLE
        buttonRepeat.visibility = View.INVISIBLE
        numberView.visibility = View.VISIBLE
        cardView.visibility = View.VISIBLE
        textView.visibility = View.VISIBLE
        button1.visibility = View.VISIBLE
        button2.visibility = View.VISIBLE
        button3.visibility = View.VISIBLE
    }

    private fun vibratePhone() {
        // Получаем объект SharedPreferences для доступа к сохранённым настройкам
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        // Читаем значение настройки темы из SharedPreferences, по умолчанию "auto"
        val vibroPreference = sharedPreferences.getBoolean("vibro_preference", true)

        if (vibroPreference) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Для API 31 и выше используем VibratorManager
                val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                // Для устройств с API ниже 31 используем Vibrator
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }
        }
    }

    // Вычисление размера баннера на основе ширины контейнера и плотности экрана
    private val adSize: BannerAdSize
        get() {
            // Получаем ширину контейнера
            var adWidthPixels = adContainerView.width
            if (adWidthPixels == 0) {
                adWidthPixels = resources.displayMetrics.widthPixels
            }
            // Преобразуем ширину в dp для баннера
            val adWidth = (adWidthPixels / resources.displayMetrics.density).roundToInt()
            return BannerAdSize.stickySize(this, adWidth)
        }

    // Метод для загрузки баннерной рекламы с обработкой событий
    private fun loadBannerAd(adSize: BannerAdSize): BannerAdView {

        return adContainerView.apply {
            setAdSize(adSize)
            // Получаем ID рекламного блока
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val installSourceInfo = packageManager.getInstallSourceInfo(packageName)
                val installerPackageName = installSourceInfo.installingPackageName

                when (installerPackageName) {
                    "com.android.vending" -> setAdUnitId(ConfigReader.getAdUnitId(this@MainActivity))
                    "ru.vk.store" -> setAdUnitId(ConfigReader.getAdRuStoreUnitId(this@MainActivity))
                    else -> setAdUnitId(ConfigReader.getAdUnitId(this@MainActivity))
                }
            } else {
                // Используем устаревший метод для API ниже 30
                @Suppress("DEPRECATION")
                val installerPackageName = packageManager.getInstallerPackageName(packageName)

                when (installerPackageName) {
                    "com.android.vending" -> setAdUnitId(ConfigReader.getAdUnitId(this@MainActivity))
                    "ru.vk.store" -> setAdUnitId(ConfigReader.getAdRuStoreUnitId(this@MainActivity))
                    else -> setAdUnitId(ConfigReader.getAdUnitId(this@MainActivity))
                }
            }

            // Установка слушателя событий баннерной рекламы
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() {
                    // Удаление баннера, если активность уничтожена
                    if (isDestroyed) {
                        bannerAd.destroy()
                        return
                    }
                }

                // Обработка ошибки загрузки рекламы
                override fun onAdFailedToLoad(error: AdRequestError) {}

                // Обработка клика на рекламу
                override fun onAdClicked() {}

                // Событие при уходе пользователя из приложения
                override fun onLeftApplication() {}

                // Событие при возврате в приложение
                override fun onReturnedToApplication() {}

                // Событие при показе рекламы
                override fun onImpression(impressionData: ImpressionData?) {
                }
            })
            // Загружаем рекламный запрос
            loadAd(AdRequest.Builder().build())
        }
    }

    private fun loadInterstitialAd() {
        // Получаем ID рекламного блока
        var adInterstitialId = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val installSourceInfo = packageManager.getInstallSourceInfo(packageName)
            val installerPackageName = installSourceInfo.installingPackageName

            adInterstitialId = when (installerPackageName) {
                "com.android.vending" -> ConfigReader.getAdInterstitiaId(this@MainActivity).toString()
                "ru.vk.store" -> ConfigReader.getAdRuStoreInterstitiaId(this@MainActivity).toString()
                else -> ConfigReader.getAdInterstitiaId(this@MainActivity).toString()
            }
        } else {
            // Используем устаревший метод для API ниже 30
            @Suppress("DEPRECATION")
            val installerPackageName = packageManager.getInstallerPackageName(packageName)

            adInterstitialId = when (installerPackageName) {
                "com.android.vending" -> ConfigReader.getAdInterstitiaId(this@MainActivity).toString()
                "ru.vk.store" -> ConfigReader.getAdRuStoreInterstitiaId(this@MainActivity).toString()
                else -> ConfigReader.getAdInterstitiaId(this@MainActivity).toString()
            }
        }
        val adRequestConfiguration = AdRequestConfiguration.Builder(adInterstitialId).build()
        interstitialAdLoader?.loadAd(adRequestConfiguration)
    }

    private fun showAd() {
        interstitialAd?.apply {
            setAdEventListener(object : InterstitialAdEventListener {
                override fun onAdShown() {
                    // Вызвается, когда реклама показана.
                    // Здесь можно обработать действия, которые должны произойти при показе рекламы.
                }

                override fun onAdFailedToShow(adError: AdError) {
                    // Вызвается, когда реклама не удалось показать.
                    // Очистите ресурсы после того, как реклама была отменена
                    interstitialAd?.setAdEventListener(null) // Убираем слушатель событий
                    interstitialAd = null // Обнуляем объект рекламы

                    // Теперь можно предзагрузить следующую межстраничную рекламу.
                    loadInterstitialAd()
                }

                override fun onAdDismissed() {
                    // Вызвается, когда реклама закрыта.
                    // Очистите ресурсы после того, как реклама была закрыта
                    interstitialAd?.setAdEventListener(null) // Убираем слушатель событий
                    interstitialAd = null // Обнуляем объект рекламы

                    // Теперь можно предзагрузить следующую межстраничную рекламу.
                    loadInterstitialAd()
                }

                override fun onAdClicked() {
                    // Вызвается, когда реклама была нажата.
                    // Можно обработать клики на рекламу.
                }

                override fun onAdImpression(impressionData: ImpressionData?) {
                    // Вызвается, когда реклама была показана (импрессия).
                    // Можно отслеживать импрессии рекламы здесь.
                }
            })
            show(this@MainActivity) // Показываем рекламу
        }
    }
}
