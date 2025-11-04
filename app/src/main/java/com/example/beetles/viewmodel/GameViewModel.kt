package com.example.beetles.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.beetles.data.Beetle
import com.example.beetles.data.GameState
import com.example.beetles.data.SettingsData
import com.example.beetles.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.random.Random
import com.example.beetles.data.AppDatabase
import com.example.beetles.data.GameRecord
import com.example.beetles.repository.GameRecordRepository
import com.example.beetles.repository.PlayerRepository
import com.example.beetles.utils.SoundManager

class GameViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    private val repository = SettingsRepository(application)
    private val database = AppDatabase.getDatabase(application)
    private val gameRecordRepository = GameRecordRepository(database.gameRecordDao())
    private val playerRepository = PlayerRepository(database.playerDao())
    private val soundManager = SoundManager(application)

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var gravityX = 0f
    private var gravityY = 0f
    private var isBonusModeActive = false
    private val gravityStrength = 15f

    private var currentPlayerId: Int? = null

    fun setCurrentPlayer(playerId: Int) {
        currentPlayerId = playerId
    }

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    val beetles = mutableStateListOf<Beetle>()

    private var gameLoopJob: Job? = null
    private var timerJob: Job? = null
    private var spawnJob: Job? = null
    private var countdownJob: Job? = null
    private var bonusJob: Job? = null
    private var bonusDurationJob: Job? = null

    private var nextBeetleId = 0
    private var screenWidth = 0f
    private var screenHeight = 0f
    private val beetleSize = 80f
    private val topBarHeight = 80f
    private val bottomBarHeight = 80f
    private val bonusSize = 60f
    private var settingsCache: SettingsData? = null

    private var spawnDelayMs = 1000L
    private var directionChangeInterval = 2f
    private var wallDamage = 0.3f
    private var isInitialized = false

    fun initGame(width: Float, height: Float) {
        if (isInitialized) return

        isInitialized = true
        screenWidth = width
        screenHeight = height

        viewModelScope.launch {
            val settings = settingsCache ?: repository.settingsFlow.first().also {
                settingsCache = it
            }

            calculateDifficultyParameters(settings.gameSpeed)

            _gameState.value = GameState(
                maxBeetles = settings.maxBeetles,
                gameSpeed = settings.gameSpeed,
                timeLeft = settings.roundDuration,
                isGameStarted = false,
                countdown = 3
            )

            startGame()
        }
    }

    private fun calculateDifficultyParameters(gameSpeed: Float) {
        when {
            gameSpeed <= 3f -> {
                spawnDelayMs = 2500L
                directionChangeInterval = 1.2f
                wallDamage = 0.5f
            }
            gameSpeed <= 7f -> {
                spawnDelayMs = 1500L
                directionChangeInterval = 2.0f
                wallDamage = 0.3f
            }
            else -> {
                spawnDelayMs = 800L
                directionChangeInterval = 3.5f
                wallDamage = 0.15f
            }
        }
    }

    private fun startGame() {
        stopAllJobs()

        countdownJob = viewModelScope.launch {
            for (i in 3 downTo 1) {
                _gameState.value = _gameState.value.copy(countdown = i)
                delay(1000)
            }
            _gameState.value = _gameState.value.copy(isGameStarted = true, countdown = 0)
            startGameLoop()
            startBonusSpawner()
        }
    }

    fun updateScreenSize(width: Float, height: Float) {
        if (screenWidth != width || screenHeight != height) {
            screenWidth = width
            screenHeight = height
        }
    }

    private fun startGameLoop() {
        gameLoopJob = viewModelScope.launch {
            while (!_gameState.value.isGameOver && _gameState.value.isGameStarted) {
                updateGame()
                delay(16)
            }
        }

        timerJob = viewModelScope.launch {
            while (!_gameState.value.isGameOver && _gameState.value.isGameStarted) {
                delay(1000)
                val state = _gameState.value
                val newTime = state.timeLeft - 1
                _gameState.value = state.copy(timeLeft = newTime)

                if (newTime <= 0) {
                    endGame()
                }
            }
        }

        spawnJob = viewModelScope.launch {
            while (!_gameState.value.isGameOver && _gameState.value.isGameStarted) {
                val state = _gameState.value
                if (beetles.count { it.isAlive } < state.maxBeetles) {
                    spawnBeetle()
                    delay(spawnDelayMs)
                } else {
                    delay(200)
                }
            }
        }
    }

    private fun startBonusSpawner() {
        bonusJob = viewModelScope.launch {
            while (!_gameState.value.isGameOver && _gameState.value.isGameStarted) {
                delay(15000)

                val bonusX = Random.nextFloat() * (screenWidth - bonusSize)
                val bonusY = Random.nextFloat() * (screenHeight - topBarHeight - bottomBarHeight - bonusSize) + topBarHeight

                _gameState.value = _gameState.value.copy(
                    showBonus = true,
                    bonusX = bonusX,
                    bonusY = bonusY
                )

                launch {
                    delay(5000)
                    if (_gameState.value.showBonus) {
                        _gameState.value = _gameState.value.copy(showBonus = false)
                    }
                }
            }
        }
    }

    fun onBonusClicked() {
        if (!_gameState.value.showBonus) return

        _gameState.value = _gameState.value.copy(
            showBonus = false,
            isBonusActive = true,
            bonusTimeLeft = 10,
            score = _gameState.value.score + 20
        )

        isBonusModeActive = true

        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        soundManager.playBeetleScream()

        bonusDurationJob?.cancel()
        bonusDurationJob = viewModelScope.launch {
            repeat(10) {
                delay(1000)
                val newTime = _gameState.value.bonusTimeLeft - 1
                _gameState.value = _gameState.value.copy(bonusTimeLeft = newTime)
            }
            deactivateBonus()
        }
    }

    private fun deactivateBonus() {
        isBonusModeActive = false
        _gameState.value = _gameState.value.copy(
            isBonusActive = false,
            bonusTimeLeft = 0
        )

        sensorManager.unregisterListener(this)
        soundManager.stopBeetleScream()

        beetles.forEach { beetle ->
            beetle.gravitySpeedX = 0f
            beetle.gravitySpeedY = 0f
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER && isBonusModeActive) {
            gravityX = event.values[0] * gravityStrength
            gravityY = event.values[1] * gravityStrength
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateGame() {
        val state = _gameState.value

        beetles.forEach { beetle ->
            if (!beetle.isAlive) return@forEach

            if (isBonusModeActive) {
                beetle.gravitySpeedX += gravityX * 0.1f
                beetle.gravitySpeedY += -gravityY * 0.1f

                beetle.gravitySpeedX = beetle.gravitySpeedX.coerceIn(-20f, 20f)
                beetle.gravitySpeedY = beetle.gravitySpeedY.coerceIn(-20f, 20f)

                beetle.directionChangeTimer = directionChangeInterval
            } else {
                beetle.gravitySpeedX *= 0.9f
                beetle.gravitySpeedY *= 0.9f

                beetle.directionChangeTimer -= 0.016f
                if (beetle.directionChangeTimer <= 0 &&
                    beetle.x > 20 && beetle.x < screenWidth - beetleSize - 20 &&
                    beetle.y > topBarHeight + 20 && beetle.y < screenHeight - bottomBarHeight - beetleSize - 20) {

                    val angle = Random.nextFloat() * 360f
                    val speed = state.gameSpeed * 2.5f
                    beetle.speedX = kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat() * speed
                    beetle.speedY = kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() * speed
                    beetle.directionChangeTimer = Random.nextFloat() * 2f + directionChangeInterval
                    beetle.rotation = angle + 90f
                }
            }

            val totalSpeedX = beetle.speedX + beetle.gravitySpeedX
            val totalSpeedY = beetle.speedY + beetle.gravitySpeedY

            val nextX = beetle.x + totalSpeedX
            val nextY = beetle.y + totalSpeedY
            var bounced = false

            if (nextX < 0 || nextX > screenWidth - beetleSize) {
                beetle.speedX = -beetle.speedX
                beetle.gravitySpeedX = -beetle.gravitySpeedX * 0.7f
                beetle.x = beetle.x.coerceIn(0f, screenWidth - beetleSize)
                bounced = true
            } else {
                beetle.x = nextX
            }

            if (nextY < topBarHeight || nextY > screenHeight - bottomBarHeight - beetleSize) {
                beetle.speedY = -beetle.speedY
                beetle.gravitySpeedY = -beetle.gravitySpeedY * 0.7f
                beetle.y = beetle.y.coerceIn(topBarHeight, screenHeight - bottomBarHeight - beetleSize)
                bounced = true
            } else {
                beetle.y = nextY
            }

            beetle.rotation = Math.toDegrees(
                atan2(totalSpeedY.toDouble(), totalSpeedX.toDouble())
            ).toFloat() + 90f
        }

        beetles.removeAll { !it.isAlive }
    }

    private fun spawnBeetle() {
        val state = _gameState.value
        val angle = Random.nextFloat() * 360f
        val speed = state.gameSpeed * 2.5f
        val speedX = kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat() * speed
        val speedY = kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() * speed

        val beetle = Beetle(
            id = nextBeetleId++,
            x = Random.nextFloat() * (screenWidth - beetleSize),
            y = Random.nextFloat() * (screenHeight - topBarHeight - bottomBarHeight - beetleSize) + topBarHeight,
            speedX = speedX,
            speedY = speedY,
            rotation = angle + 90f,
            directionChangeTimer = Random.nextFloat() * 2f + directionChangeInterval,
            lifeTime = Float.MAX_VALUE
        )

        beetles.add(beetle)
    }

    fun onBeetleClicked(beetleId: Int) {
        val beetle = beetles.find { it.id == beetleId && it.isAlive }
        if (beetle != null) {
            beetle.isAlive = false
            val state = _gameState.value
            _gameState.value = state.copy(score = state.score + 10)
        }
    }

    fun onMissClick() {
        if (!_gameState.value.isGameStarted) return
        val state = _gameState.value
        _gameState.value = state.copy(score = state.score - 5)
    }

    private fun endGame() {
        val wasGameStarted = _gameState.value.isGameStarted
        _gameState.value = _gameState.value.copy(isGameOver = true)
        stopAllJobs()
        deactivateBonus()

        if (!wasGameStarted) return

        val playerId = currentPlayerId
        if (playerId != null) {
            viewModelScope.launch {
                try {
                    val state = _gameState.value

                    val gameRecord = GameRecord(
                        playerId = playerId,
                        score = state.score,
                        difficulty = settingsCache?.gameSpeed?.toInt() ?: 1,
                        gameSpeed = state.gameSpeed,
                        maxBeetles = state.maxBeetles,
                        roundDuration = settingsCache?.roundDuration ?: 60
                    )

                    gameRecordRepository.insertGameRecord(gameRecord)
                    playerRepository.updateGameResult(playerId, state.score)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun stopAllJobs() {
        gameLoopJob?.cancel()
        timerJob?.cancel()
        spawnJob?.cancel()
        countdownJob?.cancel()
        bonusJob?.cancel()
        bonusDurationJob?.cancel()
    }

    fun resetGame() {
        stopAllJobs()
        deactivateBonus()
        beetles.clear()
        nextBeetleId = 0
        isInitialized = false
        settingsCache = null
        currentPlayerId = null
        _gameState.value = GameState()
        screenWidth = 0f
        screenHeight = 0f
    }

    override fun onCleared() {
        super.onCleared()
        stopAllJobs()
        deactivateBonus()
        soundManager.release()
    }
}