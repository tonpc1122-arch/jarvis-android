
package com.example.jarvis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var onResult: ((String) -> Unit)? = null
    private var onStatus: ((String) -> Unit)? = null

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startListening()
            } else {
                onStatus?.invoke("กรุณาอนุญาตการใช้ไมโครโฟน")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(this)
        }

        setContent {
            JarvisScreen(
                listen = { result, status ->
                    onResult = result
                    onStatus = status

                    if (
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        startListening()
                    } else {
                        permissionLauncher.launch(
                            Manifest.permission.RECORD_AUDIO
                        )
                    }
                },
                speak = { say(it) }
            )
        }
    }

    private fun startListening() {
        val recognizer = speechRecognizer

        if (recognizer == null) {
            onStatus?.invoke("อุปกรณ์ไม่รองรับระบบรู้จำเสียง")
            return
        }

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "th-TH"
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "th-TH"
            )
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "JARVIS กำลังฟังคำสั่ง"
            )
        }

        onStatus?.invoke("กำลังฟัง...")

        recognizer.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    onStatus?.invoke("กำลังฟัง...")
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    onStatus?.invoke("กำลังประมวลผล...")
                }

                override fun onError(error: Int) {
                    onStatus?.invoke(
                        "ฟังไม่สำเร็จ กรุณาลองอีกครั้ง"
                    )
                }

                override fun onResults(results: Bundle?) {
                    val spoken = results
                        ?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )
                        ?.firstOrNull()
                        .orEmpty()

                    if (spoken.isNotBlank()) {
                        onResult?.invoke(spoken)
                    } else {
                        onStatus?.invoke("ไม่ได้ยินคำสั่ง")
                    }
                }

                override fun onPartialResults(
                    partialResults: Bundle?
                ) {}

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {}
            }
        )

        recognizer.startListening(intent)
    }

    private fun say(text: String) {
        tts?.language = Locale("th", "TH")
        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "jarvis_reply"
        )
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("th", "TH")
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun JarvisScreen(
    listen: ((String) -> Unit, (String) -> Unit) -> Unit,
    speak: (String) -> Unit
) {
    var transcript by remember {
        mutableStateOf("แตะไมโครโฟนเพื่อพูดกับผม")
    }

    var status by remember {
        mutableStateOf("SYSTEM STANDBY")
    }

    var answer by remember {
        mutableStateOf(
            "สวัสดีครับ ผม JARVIS ผู้ช่วยส่วนตัวของคุณ"
        )
    }

    fun respond(input: String) {
        transcript = input

        val reply = when {
            input.contains("สวัสดี") ->
                "สวัสดีครับ มีอะไรให้ผมช่วยไหมครับ"

            input.contains("เวลา") ->
                "คุณสามารถตรวจสอบเวลาปัจจุบันได้จากโทรศัพท์ครับ"

            input.contains("ชื่อ") ->
                "ผมชื่อ JARVIS เป็นผู้ช่วยส่วนตัวของคุณครับ"

            input.contains("เปิด") ->
                "ตอนนี้ผมยังไม่สามารถเปิดแอปอื่นได้ครับ"

            else ->
                "ผมได้รับคำสั่งแล้วครับ ขณะนี้ระบบ AI ยังไม่ได้เชื่อมต่อ"
        }

        answer = reply
        status = "RESPONSE READY"
        speak(reply)
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF43D9FF),
            background = Color(0xFF050B14),
            surface = Color(0xFF0B1726),
            onSurface = Color(0xFFE4F7FF)
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF071A2B),
                            Color(0xFF03070D)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "J.A.R.V.I.S",
                    color = Color(0xFF66DFFF),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "PERSONAL AI ASSISTANT",
                    color = Color(0xFF6B9DB5),
                    fontSize = 11.sp,
                    letterSpacing = 3.sp
                )

                Spacer(Modifier.height(44.dp))

                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .border(
                            2.dp,
                            Color(0xFF20BDEB),
                            CircleShape
                        )
                        .padding(12.dp)
                        .border(
                            1.dp,
                            Color(0xFF17688D),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .background(
                                Color(0xFF0A263B),
                                CircleShape
                            )
                            .border(
                                1.dp,
                                Color(0xFF48D8FF),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "J",
                            color = Color(0xFF8AEAFF),
                            fontSize = 76.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }

                Spacer(Modifier.height(26.dp))

                Text(
                    status,
                    color = Color(0xFF54D9FF),
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xCC0B1B2B)
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {

                    Column(
                        Modifier.padding(18.dp)
                    ) {

                        Text(
                            "คุณพูด",
                            color = Color(0xFF63CBEA),
                            fontSize = 12.sp
                        )

                        Text(
                            transcript,
                            color = Color.White,
                            fontSize = 16.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            "JARVIS",
                            color = Color(0xFF63CBEA),
                            fontSize = 12.sp
                        )

                        Text(
                            answer,
                            color = Color(0xFFE3F7FF),
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = {
                        status = "INITIALIZING"

                        listen(
                            { spoken -> respond(spoken) },
                            { newStatus ->
                                status = newStatus.uppercase()
                            }
                        )
                    },
                    modifier = Modifier.size(88.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF087DA8)
                    )
                ) {
                    Text(
                        "MIC",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "แตะเพื่อพูด • ภาษาไทย",
                    color = Color(0xFF7196A9),
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(26.dp))

                Text(
                    "JARVIS MOBILE • BUILD 0.1",
                    color = Color(0xFF41677C),
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
