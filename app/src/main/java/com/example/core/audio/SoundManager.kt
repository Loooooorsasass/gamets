package com.example.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * SoundManager quản lý âm thanh cho trò chơi bằng PCM AudioTrack an toàn, hiệu năng cao:
 * - Sử dụng các AudioTrack tĩnh (MODE_STATIC) được tái sử dụng, không tạo/hủy AudioTrack liên tục.
 * - Kiểm tra nghiêm ngặt AudioTrack.STATE_INITIALIZED trước khi phát để tương thích tuyệt đối
 *   với mọi thiết bị (kể cả môi trường máy ảo không có thiết bị đầu ra âm thanh).
 * - Nhạc nền (BGM) lặp vô tận bằng 1 AudioTrack luồng (MODE_STREAM) duy nhất.
 */
class SoundManager(@Suppress("UNUSED_PARAMETER") private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) {
                resumeBgm()
            } else {
                pauseBgm()
            }
        }

    // Các AudioTrack MODE_STATIC tái sử dụng cho từng hiệu ứng SFX
    @Volatile private var trackClick: AudioTrack? = null
    @Volatile private var trackStep: AudioTrack? = null
    @Volatile private var trackBump: AudioTrack? = null
    @Volatile private var trackWin: AudioTrack? = null
    @Volatile private var trackLose: AudioTrack? = null
    @Volatile private var trackStarPing: AudioTrack? = null

    // Nhạc nền BGM
    @Volatile private var pcmBgm: ShortArray? = null
    @Volatile private var trackBgm: AudioTrack? = null
    private var bgmJob: Job? = null
    @Volatile private var isBgmDesiredPlaying: Boolean = false

    init {
        scope.launch {
            initAudioSystem()
            if (isMusicEnabled) {
                startBgm()
            }
        }
    }

    /**
     * Khởi tạo sẵn dữ liệu PCM và các AudioTrack tĩnh tái sử dụng trong RAM.
     */
    private fun initAudioSystem() {
        try {
            // 1. Button click: Âm click sắc gọn cao độ 960Hz
            val pcmClick = generatePcmTone(freq = 960.0, durationMs = 20, volume = 0.4f, decay = true)
            trackClick = createStaticAudioTrack(pcmClick, 0.7f)

            // 2. Move sound: Âm bước chân gõ nhẹ 520Hz
            val pcmStep = generatePcmTone(freq = 520.0, durationMs = 30, volume = 0.35f, decay = true)
            trackStep = createStaticAudioTrack(pcmStep, 0.6f)

            // 3. Wall bump: Âm trầm đục dội lại 120Hz
            val pcmBump = generatePcmTone(freq = 120.0, durationMs = 70, volume = 0.55f, decay = true)
            trackBump = createStaticAudioTrack(pcmBump, 0.8f)

            // 4. Win sound: Chuỗi âm vang chuông hân hoan
            val pcmWin = generateMelodyPcm(
                notes = listOf(
                    523.25 to 80,   // C5
                    659.25 to 80,   // E5
                    783.99 to 90,   // G5
                    1046.50 to 260  // C6
                ),
                volume = 0.6f
            )
            trackWin = createStaticAudioTrack(pcmWin, 0.95f)

            // 5. Lose sound
            val pcmLose = generateMelodyPcm(
                notes = listOf(
                    392.00 to 110,  // G4
                    329.63 to 110,  // E4
                    261.63 to 110,  // C4
                    196.00 to 220   // G3
                ),
                volume = 0.45f
            )
            trackLose = createStaticAudioTrack(pcmLose, 0.8f)

            // 6. Star chime
            val pcmStarPing = generateMelodyPcm(
                notes = listOf(
                    1318.51 to 70,  // E6
                    1760.00 to 180  // A6
                ),
                volume = 0.5f
            )
            trackStarPing = createStaticAudioTrack(pcmStarPing, 0.85f)

            // 7. Dữ liệu PCM nhạc nền vui tươi rộn rã xu hướng NHẠC TẾT (Tet / Spring Festive Melody)
            pcmBgm = generateFestiveTetBgmPcm(volume = 0.22f)
        } catch (e: Exception) {
            Log.w("SoundManager", "Audio system initialization fallback: ${e.message}")
        }
    }

    private fun createStaticAudioTrack(samples: ShortArray, volume: Float): AudioTrack? {
        return try {
            val byteSize = samples.size * 2
            val minBufSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(byteSize)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            if (track.state == AudioTrack.STATE_INITIALIZED) {
                track.write(samples, 0, samples.size)
                track.setVolume(volume.coerceIn(0f, 1f))
                track
            } else {
                try { track.release() } catch (_: Exception) {}
                null
            }
        } catch (e: Exception) {
            Log.w("SoundManager", "Static track creation skipped: ${e.message}")
            null
        }
    }

    private fun playStaticTrack(track: AudioTrack?) {
        if (!isSoundEnabled || track == null) return
        try {
            if (track.state == AudioTrack.STATE_INITIALIZED) {
                track.stop()
                track.reloadStaticData()
                track.play()
            }
        } catch (_: Exception) {
            // Thiết bị không hỗ trợ phát hoặc bận
        }
    }

    // ==========================================
    // PHÁT HIỆU ỨNG ÂM THANH (SFX)
    // ==========================================

    fun playStep() {
        playStaticTrack(trackStep)
    }

    fun playBump() {
        playStaticTrack(trackBump)
    }

    fun playWin() {
        playStaticTrack(trackWin)
    }

    fun playLose() {
        playStaticTrack(trackLose)
    }

    fun playClick() {
        playStaticTrack(trackClick)
    }

    fun playStarChime() {
        playStaticTrack(trackStarPing)
    }

    // ==========================================
    // QUẢN LÝ NHẠC NỀN (BGM)
    // ==========================================

    fun startBgm() {
        isBgmDesiredPlaying = true
        if (!isMusicEnabled) return
        if (bgmJob?.isActive == true) return

        bgmJob = scope.launch {
            try {
                // Đợi nếu bộ đệm BGM đang được khởi tạo
                var retries = 0
                while (pcmBgm == null && retries < 20 && isActive) {
                    delay(50L)
                    retries++
                }
                val bgmSamples = pcmBgm ?: return@launch

                val minBufSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                var track: AudioTrack? = null
                try {
                    track = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(minBufSize)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                } catch (e: Exception) {
                    Log.w("SoundManager", "BGM AudioTrack creation skipped: ${e.message}")
                    return@launch
                }

                if (track.state != AudioTrack.STATE_INITIALIZED) {
                    try { track.release() } catch (_: Exception) {}
                    return@launch
                }

                trackBgm = track
                track.setVolume(0.35f)
                track.play()

                val chunkSize = 2048
                var offset = 0
                while (isActive && isMusicEnabled && isBgmDesiredPlaying) {
                    val count = minOf(chunkSize, bgmSamples.size - offset)
                    val written = track.write(bgmSamples, offset, count)
                    if (written < 0) break
                    offset += count
                    if (offset >= bgmSamples.size) {
                        offset = 0
                    }
                }
            } catch (_: Exception) {
            } finally {
                val currentTrack = trackBgm
                trackBgm = null
                try { currentTrack?.stop() } catch (_: Exception) {}
                try { currentTrack?.release() } catch (_: Exception) {}
            }
        }
    }

    fun stopBgm() {
        pauseBgm()
    }

    fun pauseBgm() {
        isBgmDesiredPlaying = false
        bgmJob?.cancel()
        bgmJob = null
        val currentTrack = trackBgm
        trackBgm = null
        try { currentTrack?.stop() } catch (_: Exception) {}
        try { currentTrack?.release() } catch (_: Exception) {}
    }

    fun resumeBgm() {
        if (isMusicEnabled) {
            startBgm()
        }
    }

    fun release() {
        pauseBgm()
        try {
            scope.cancel()
        } catch (_: Exception) {}

        listOf(trackClick, trackStep, trackBump, trackWin, trackLose, trackStarPing).forEach { track ->
            try {
                if (track?.state == AudioTrack.STATE_INITIALIZED) {
                    track.stop()
                }
                track?.release()
            } catch (_: Exception) {}
        }
        trackClick = null
        trackStep = null
        trackBump = null
        trackWin = null
        trackLose = null
        trackStarPing = null
    }

    // ==========================================
    // UTILITIES TỰ SINH DỮ LIỆU PCM 16-BIT
    // ==========================================

    companion object {
        private const val SAMPLE_RATE = 22050

        private fun generatePcmTone(freq: Double, durationMs: Int, volume: Float, decay: Boolean): ShortArray {
            val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val time = i.toDouble() / SAMPLE_RATE
                val angle = 2.0 * PI * freq * time
                val env = if (decay) (1.0 - (i.toDouble() / numSamples)).coerceIn(0.0, 1.0) else 1.0
                val sample = (sin(angle) * Short.MAX_VALUE * volume * env).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            return buffer
        }

        private fun generateMelodyPcm(notes: List<Pair<Double, Int>>, volume: Float): ShortArray {
            val buffers = notes.map { (freq, durationMs) ->
                generatePcmTone(freq, durationMs, volume, decay = true)
            }
            val totalSamples = buffers.sumOf { it.size }
            val result = ShortArray(totalSamples)
            var offset = 0
            for (b in buffers) {
                System.arraycopy(b, 0, result, offset, b.size)
                offset += b.size
            }
            return result
        }

        /**
         * Tự sinh dữ liệu âm thanh PCM 16-bit cho NHẠC NỀN XU HƯỚNG TẾT VUI TƯƠI (Vietnamese Tet Festive Theme):
         * - Giai điệu ngũ cung tươi vui rộn ràng đón Tết, hoa mai hoa đào nở rộ (C - D - E - G - A).
         * - Âm sắc chuông gõ / đàn T'rưng dân tộc cách điệu hiện đại (Chime bell synth) hòa âm rộn rã.
         * - Nhịp bass rộn ràng, nhịp điệu tưng bừng với tiếng gõ phách vui nhộn.
         */
        private fun generateFestiveTetBgmPcm(volume: Float = 0.22f): ShortArray {
            val stepMs = 210 // Tốc độ rộn ràng ~142 BPM (nhanh vui tươi)
            val totalSteps = 64 // 16 ô nhịp x 4 phách = 64 bước = ~13.4 giây lặp vô tận hoàn hảo
            val totalDurationMs = totalSteps * stepMs
            val totalSamples = (SAMPLE_RATE * totalDurationMs / 1000.0).toInt()
            val result = ShortArray(totalSamples)

            // Tần số nốt nhạc Ngũ Cung rực rỡ sắc xuân
            val nC3 = 130.81; val nE3 = 164.81; val nF3 = 174.61; val nG3 = 196.00; val nA3 = 220.00
            val nC4 = 261.63; val nD4 = 293.66; val nE4 = 329.63; val nF4 = 349.23; val nG4 = 392.00; val nA4 = 440.00
            val nC5 = 523.25; val nD5 = 587.33; val nE5 = 659.25; val nG5 = 783.99; val nA5 = 880.00
            val nC6 = 1046.50; val nD6 = 1174.66; val nE6 = 1318.51; val nG6 = 1567.98

            // 1. Giai điệu chính Nhạc Tết (Lead Melody: Rộn ràng ngày xuân, Tết Tết Tết đến rồi, mừng xuân an khang)
            data class NoteEvent(val step: Int, val durationSteps: Double, val freq: Double, val gain: Float)
            val leadMelody = listOf(
                // Đoạn 1: Mở màn rộn rã "Tết Tết Tết đến rồi"
                NoteEvent(0, 0.9, nG4, 0.95f),
                NoteEvent(1, 0.9, nG4, 0.95f),
                NoteEvent(2, 0.9, nE4, 0.85f),
                NoteEvent(3, 0.9, nG4, 0.95f),
                NoteEvent(4, 0.9, nA4, 1.0f),
                NoteEvent(5, 0.9, nC5, 1.05f),
                NoteEvent(6, 0.9, nA4, 0.95f),
                NoteEvent(7, 0.9, nG4, 0.9f),

                // Đoạn 2: Trăm hoa đua nở, rộn rã phố phường
                NoteEvent(8, 0.9, nC5, 1.0f),
                NoteEvent(9, 0.9, nC5, 1.0f),
                NoteEvent(10, 0.9, nA4, 0.9f),
                NoteEvent(11, 0.9, nC5, 1.0f),
                NoteEvent(12, 0.9, nD5, 1.05f),
                NoteEvent(13, 0.9, nE5, 1.1f),
                NoteEvent(14, 0.9, nD5, 0.95f),
                NoteEvent(15, 0.9, nC5, 1.0f),

                // Đoạn 3: Cung đàn mùa xuân tưng bừng
                NoteEvent(16, 0.9, nE5, 1.1f),
                NoteEvent(17, 0.9, nG5, 1.15f),
                NoteEvent(18, 0.9, nE5, 1.0f),
                NoteEvent(19, 0.9, nD5, 0.95f),
                NoteEvent(20, 0.9, nC5, 1.0f),
                NoteEvent(21, 0.9, nD5, 1.0f),
                NoteEvent(22, 0.9, nE5, 1.05f),
                NoteEvent(23, 0.9, nG5, 1.1f),

                // Đoạn 4: Chúc Tết vạn nhà bình an
                NoteEvent(24, 0.9, nA5, 1.15f),
                NoteEvent(25, 0.9, nG5, 1.05f),
                NoteEvent(26, 0.9, nE5, 1.0f),
                NoteEvent(27, 0.9, nD5, 0.95f),
                NoteEvent(28, 1.8, nC5, 1.05f),
                NoteEvent(30, 1.8, nC5, 1.0f),

                // Đoạn 5: Cao trào Điệp khúc mùa xuân (Quãng cao rực rỡ như tiếng pháo hoa & chuông vàng)
                NoteEvent(32, 0.9, nG5, 1.15f),
                NoteEvent(33, 0.9, nG5, 1.15f),
                NoteEvent(34, 0.9, nE5, 1.05f),
                NoteEvent(35, 0.9, nG5, 1.15f),
                NoteEvent(36, 0.9, nA5, 1.2f),
                NoteEvent(37, 0.9, nC6, 1.25f),
                NoteEvent(38, 0.9, nA5, 1.15f),
                NoteEvent(39, 0.9, nG5, 1.1f),

                // Đoạn 6: Nụ cười đón Tết hân hoan
                NoteEvent(40, 0.9, nE5, 1.05f),
                NoteEvent(41, 0.9, nG5, 1.15f),
                NoteEvent(42, 0.9, nA5, 1.2f),
                NoteEvent(43, 0.9, nC6, 1.25f),
                NoteEvent(44, 0.9, nD6, 1.2f),
                NoteEvent(45, 0.9, nE6, 1.3f),
                NoteEvent(46, 0.9, nD6, 1.15f),
                NoteEvent(47, 0.9, nC6, 1.2f),

                // Đoạn 7: Mùa xuân sang mang lộc tài
                NoteEvent(48, 0.9, nG5, 1.1f),
                NoteEvent(49, 0.9, nA5, 1.15f),
                NoteEvent(50, 0.9, nC6, 1.2f),
                NoteEvent(51, 0.9, nD6, 1.2f),
                NoteEvent(52, 1.4, nE6, 1.25f),
                NoteEvent(54, 0.6, nD6, 1.05f),
                NoteEvent(55, 0.9, nC6, 1.15f),
                NoteEvent(56, 0.9, nA5, 1.05f),

                // Đoạn 8: Kết câu rực rỡ chuẩn bị lặp vòng hoa mỹ
                NoteEvent(57, 0.9, nG5, 1.05f),
                NoteEvent(58, 0.9, nE5, 1.0f),
                NoteEvent(59, 0.9, nD5, 0.95f),
                NoteEvent(60, 1.8, nC5, 1.15f),
                NoteEvent(62, 1.8, nC5, 1.1f)
            )

            // 2. Hợp âm đệm tưng bừng (Upbeat Chords on off-beats)
            val measureChords = listOf(
                listOf(nC4, nE4, nG4), // Bar 1 (C Maj)
                listOf(nF3, nA3, nC4), // Bar 2 (F Maj)
                listOf(nA3, nC4, nE4), // Bar 3 (A Min)
                listOf(nG3, nC4, nE4), // Bar 4 (C / G)
                listOf(nF3, nA3, nC4), // Bar 5 (F Maj)
                listOf(nG3, nD4, nG4), // Bar 6 (G Maj)
                listOf(nA3, nC4, nE4), // Bar 7 (A Min)
                listOf(nG3, nC4, nE4), // Bar 8 (C Maj)
                listOf(nC4, nE4, nG4), // Bar 9 (C Maj)
                listOf(nF3, nA3, nC4), // Bar 10 (F Maj)
                listOf(nA3, nC4, nE4), // Bar 11 (A Min)
                listOf(nG3, nD4, nG4), // Bar 12 (G Maj)
                listOf(nF3, nA3, nC4), // Bar 13 (F Maj)
                listOf(nC4, nE4, nG4), // Bar 14 (C Maj)
                listOf(nG3, nD4, nG4), // Bar 15 (G Maj)
                listOf(nC4, nE4, nG4)  // Bar 16 (C Maj)
            )

            // 3. Bassline vui nhộn (Bouncy Walking Bass)
            val bassPattern = listOf(
                nC3, nG3, nC3, nG3,
                nF3, nC3, nF3, nC3,
                nA3, nE3, nA3, nE3,
                nC3, nG3, nC3, nG3,
                nF3, nC3, nF3, nC3,
                nG3, nD3_or(nG3), nG3, nD3_or(nG3),
                nA3, nE3, nA3, nE3,
                nC3, nG3, nC3, nG3,
                nC3, nG3, nC3, nG3,
                nF3, nC3, nF3, nC3,
                nA3, nE3, nA3, nE3,
                nG3, nD3_or(nG3), nG3, nD3_or(nG3),
                nF3, nC3, nF3, nC3,
                nC3, nG3, nC3, nG3,
                nG3, nD3_or(nG3), nG3, nD3_or(nG3),
                nC3, nG3, nC3, nG3
            )

            for (i in 0 until totalSamples) {
                val timeSec = i.toDouble() / SAMPLE_RATE
                val currentStep = (timeSec * 1000.0 / stepMs)
                var mixed = 0.0

                // A. Tổng hợp giai điệu chính (Lead Synth chuông gõ / phách Tết)
                for (note in leadMelody) {
                    val noteStartSec = note.step * stepMs / 1000.0
                    val noteDurSec = note.durationSteps * stepMs / 1000.0
                    if (timeSec >= noteStartSec && timeSec < noteStartSec + noteDurSec) {
                        val tNote = timeSec - noteStartSec
                        val envAttack = (tNote / 0.008).coerceIn(0.0, 1.0)
                        val envDecay = kotlin.math.exp(-tNote * 3.8)
                        val env = envAttack * envDecay * note.gain

                        // Âm sắc tươi vui: Sóng cơ bản + Họa âm bậc 2 + Họa âm bậc 3 lấp lánh
                        val leadTone = sin(2.0 * PI * note.freq * timeSec) +
                                0.45 * sin(4.0 * PI * note.freq * timeSec) +
                                0.22 * sin(6.0 * PI * note.freq * timeSec) +
                                0.10 * sin(8.0 * PI * note.freq * timeSec)
                        mixed += leadTone * 0.42 * env
                    }
                }

                // B. Tổng hợp hợp âm đệm (Upbeat Chords nảy trên nhịp chẵn)
                val measureIdx = (currentStep / 4.0).toInt().coerceIn(0, measureChords.size - 1)
                val stepInMeasure = (currentStep % 4.0)
                // Đệm nảy ở các phách 1.5, 2.0, 3.5 để tạo nhịp điệu rộn rã
                val isChordBeat = (stepInMeasure in 1.0..1.8) || (stepInMeasure in 3.0..3.8)
                if (isChordBeat) {
                    val chord = measureChords[measureIdx]
                    val beatOffsetSec = ((currentStep % 1.0) * stepMs / 1000.0)
                    val chordEnv = kotlin.math.exp(-beatOffsetSec * 7.5)
                    var chordSum = 0.0
                    for (f in chord) {
                        chordSum += sin(2.0 * PI * f * timeSec) + 0.25 * sin(4.0 * PI * f * timeSec)
                    }
                    mixed += (chordSum / chord.size) * 0.22 * chordEnv
                }

                // C. Tổng hợp Bass rộn rã
                val bassStepIdx = currentStep.toInt().coerceIn(0, bassPattern.size - 1)
                val bassFreq = bassPattern[bassStepIdx]
                val bassOffsetSec = ((currentStep % 1.0) * stepMs / 1000.0)
                val bassEnv = kotlin.math.exp(-bassOffsetSec * 5.2)
                val bassTone = sin(2.0 * PI * bassFreq * timeSec) + 0.35 * sin(4.0 * PI * bassFreq * timeSec)
                mixed += bassTone * 0.26 * bassEnv

                // D. Tiếng gõ phách Tết vui nhộn (Wood block / festive percussion pulse)
                val isBeatHit = (currentStep % 1.0) < 0.2
                if (isBeatHit) {
                    val hitTime = (currentStep % 1.0) * stepMs / 1000.0
                    val hitFreq = if (currentStep.toInt() % 2 == 0) 1400.0 else 1900.0
                    val hitEnv = kotlin.math.exp(-hitTime * 45.0)
                    mixed += sin(2.0 * PI * hitFreq * timeSec) * 0.08 * hitEnv
                }

                val sampleVal = (mixed * Short.MAX_VALUE * volume).toInt()
                result[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            return result
        }

        private fun nD3_or(defaultFreq: Double): Double = 146.83
    }
}
