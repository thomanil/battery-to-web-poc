package com.example.battery2web

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
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

    /**
     * Function to restart the application
     */
    fun restartApp() {
        val packageManager = packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
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

                // Set up JavaScript interface to communicate with Kotlin
                addJavascriptInterface(WebAppInterface(context), "Android")

                // Set a WebChromeClient to handle JavaScript dialogs
                setWebChromeClient(android.webkit.WebChromeClient())

                if (!url.isNullOrBlank()) {
                    loadUrl(url)
                } else {
                    loadDataWithBaseURL(null, localTestharnessWebPage, "text/html", "UTF-8", null)
                }
            }
        }
    )
}


/**
 * JavaScript interface for communication between WebView and Android
 */
class WebAppInterface(private val context: Context) {

    @JavascriptInterface
    fun restartApplication() {
        // Cast context to Activity and call restartApp
        if (context is MainActivity) {
            context.restartApp()
        }
    }
}


val localTestharnessWebPage = """
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
                            button {
                                background-color: #3498db;
                                color: white;
                                border: none;
                                padding: 10px 15px;
                                border-radius: 4px;
                                font-size: 16px;
                                cursor: pointer;
                                margin-bottom: 10px;
                            }
                            button:active {
                                background-color: #2980b9;
                            }
                            .warning {
                                background-color: #e74c3c;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1>Hello from Local HTML</h1>
                            <p>This is a local HTML page loaded in the WebView.</p>
                            <button onclick="showAlert()">Click Me</button>
                            <button onclick="restartApp()" class="warning">Restart App</button>
                        </div>
                        
                        <script type="text/javascript">
                            function showAlert() {
                                alert('Button clicked! JavaScript is working.');
                            }
                            
                            function restartApp() {
                                if (confirm('Are you sure you want to restart the app?')) {
                                    // Call Android method using interface
                                    Android.restartApplication();
                                }
                            }
                        </script>
                    </body>
                    </html>
                """.trimIndent()


@Preview(showBackground = true)
@Composable
fun WebViewPreview() {
    Battery2WebTheme {
        WebViewScreen("https://example.com")
    }
}

