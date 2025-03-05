package com.example.battery2web

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.battery2web.ui.theme.Battery2WebTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Battery2WebTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WebViewScreen(
                        url = "", //"https://example.com"
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun WebViewScreen(url: String?, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true

                // Configure WebView settings
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    javaScriptCanOpenWindowsAutomatically = true
                    allowContentAccess = true
                    allowFileAccess = true
                }

                // Set a WebChromeClient to handle JavaScript dialogs
                setWebChromeClient(android.webkit.WebChromeClient())

                if (!url.isNullOrBlank()) {
                    loadUrl(url)
                } else {
                    val htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Local HTML Page</title>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                margin: 20px;
                                background-color: #f0f0f0;
                            }
                            h1 {
                                color: #2c3e50;
                            }
                            .container {
                                background-color: white;
                                padding: 20px;
                                border-radius: 8px;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1>Hello from Local HTML</h1>
                            <p>This is a local HTML page loaded in the WebView.</p>
                            <button onclick="alert('Button clicked!')">Click Me</button>
                        </div>
                    </body>
                    </html>
                """.trimIndent()

                    loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun WebViewPreview() {
    Battery2WebTheme {
        WebViewScreen("https://example.com")
    }
}