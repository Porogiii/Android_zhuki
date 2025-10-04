package com.example.zhuki.ui.screens

import android.content.Context
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.zhuki.R
import java.io.InputStream
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.CenterAlignedTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val rulesText = loadHtmlFromRawResource(context, R.raw.rules)

    val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-size: 16px;
                    line-height: 1.6;
                    margin: 0;
                    padding: 24px 16px 16px 16px;
                    font-family: 'Roboto', sans-serif;
                    color: #FFFFFF !important;
                    background-color: transparent;
                }

                h1 {
                    font-size: 24px;
                    margin-top: 0;
                    margin-bottom: 20px;
                    text-align: center;
                    color: #FFFFFF !important; /* Белый заголовок */
                    font-weight: 600;
                }

                h2 {
                    font-size: 18px;
                    margin-top: 24px;
                    margin-bottom: 12px;
                    color: #FFB74D !important; /* Оранжевый для заголовков */
                    font-weight: 500;
                }

                p {
                    margin-bottom: 16px;
                    color: #FFFFFF !important; /* Белый текст */
                }

                ul, ol {
                    margin-left: 20px;
                    margin-bottom: 20px;
                    color: #FFFFFF !important;
                }

                li {
                    margin-bottom: 8px;
                    color: #FFFFFF !important;
                }

                strong {
                    color: #4FC3F7 !important;
                    font-weight: 600;
                }

                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
            </style>
        </head>
        <body>
            $rulesText
        </body>
        </html>
    """.trimIndent()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Правила игры",
                        color = Color.White
                    )
                }
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = false
                    setBackgroundColor(0x00000000)
                    isVerticalScrollBarEnabled = true

                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.setSupportZoom(false)

                    loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
            }
        )
    }
}

private fun loadHtmlFromRawResource(context: Context, resourceId: Int): String {
    return try {
        val inputStream: InputStream = context.resources.openRawResource(resourceId)
        inputStream.bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        """
        <h1>Правила игры "Жуки"</h1>
        <p>Не удалось загрузить правила игры. Пожалуйста, проверьте подключение.</p>
        """.trimIndent()
    }
}