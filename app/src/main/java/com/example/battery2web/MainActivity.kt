package com.example.battery2web

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
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
import org.json.JSONObject

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

    @JavascriptInterface
    fun getBatteryInfo(): String {
        // Get battery information from the system
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }

        // Get the battery percentage
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct: Float = if (level != -1 && scale != -1) level * 100 / scale.toFloat() else -1f

        // Get charging status
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // Get charging method
        val chargePlug: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val usbCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
        val acCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_AC

        // Create JSON object with battery information
        val batteryInfo = JSONObject().apply {
            put("percentage", batteryPct)
            put("isCharging", isCharging)
            put("usbCharge", usbCharge)
            put("acCharge", acCharge)
        }

        return batteryInfo.toString()
    }
}


val localTestharnessWebPage = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Battery Info</title>
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
                                display: block;
                                width: 100%;
                                margin-top: 10px;
                            }
                            button:active {
                                background-color: #2980b9;
                            }
                            .warning {
                                background-color: #e74c3c;
                            }
                            .battery-info {
                                margin: 20px 0;
                                padding: 15px;
                                background-color: #f9f9f9;
                                border-radius: 5px;
                                border-left: 5px solid #3498db;
                            }
                            .battery-level {
                                font-size: 24px;
                                font-weight: bold;
                                margin-bottom: 10px;
                            }
                            .battery-icon {
                                position: relative;
                                width: 60px;
                                height: 30px;
                                border: 2px solid #333;
                                border-radius: 3px;
                                margin: 15px 0;
                                padding: 0;
                            }
                            .battery-icon:after {
                                content: '';
                                position: absolute;
                                width: 6px;
                                height: 15px;
                                background: #333;
                                top: 7px;
                                right: -8px;
                                border-radius: 0 3px 3px 0;
                            }
                            .battery-fill {
                                height: 100%;
                                background-color: #2ecc71;
                                transition: width 0.5s ease;
                            }
                            .battery-charging {
                                position: absolute;
                                top: 5px;
                                left: 20px;
                                font-size: 18px;
                                color: #333;
                            }
                            .low-battery .battery-fill {
                                background-color: #e74c3c;
                            }
                            .medium-battery .battery-fill {
                                background-color: #f39c12;
                            }
                            .refresh {
                                background-color: #2ecc71;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1>Battery Information</h1>

                            <div class="battery-info">
                                <div class="battery-level">
                                    <span id="batteryPercentage">Loading...</span>
                                </div>
                                <div class="battery-status" id="batteryStatus">
                                    Checking battery status...
                                </div>
                                <div id="batteryIconContainer">
                                    <div class="battery-icon">
                                        <div class="battery-fill" id="batteryFill"></div>
                                        <span class="battery-charging" id="chargingIcon" style="display: none;">⚡</span>
                                    </div>
                                </div>
                            </div>

                            <button onclick="updateBatteryInfo()" class="refresh">Refresh Battery Info</button>
                            <button onclick="restartApp()" class="warning">Restart App</button>
                        </div>

                        <script type="text/javascript">
                            function showAlert() {
                                alert('Button clicked! JavaScript is working.');
                            }

                            function restartApp() {
                                if (confirm('Are you sure you want to restart the app?')) {
                                    try {
                                        Android.restartApplication();
                                    } catch (e) {
                                        alert('Error: ' + e.message);
                                    }
                                }
                            }

                            function updateBatteryInfo() {
                                try {
                                    // Get battery information from Android
                                    const batteryInfoJson = Android.getBatteryInfo();
                                    const batteryInfo = JSON.parse(batteryInfoJson);

                                    // Update battery percentage display
                                    const percentage = batteryInfo.percentage.toFixed(1);
                                    document.getElementById('batteryPercentage').textContent = percentage + '% Battery';

                                    // Update battery fill
                                    const batteryFill = document.getElementById('batteryFill');
                                    batteryFill.style.width = percentage + '%';

                                    // Update battery icon class based on level
                                    const batteryIcon = document.getElementById('batteryIconContainer');
                                    if (percentage < 20) {
                                        batteryIcon.className = 'low-battery';
                                    } else if (percentage < 50) {
                                        batteryIcon.className = 'medium-battery';
                                    } else {
                                        batteryIcon.className = '';
                                    }

                                    // Update charging status
                                    const statusElement = document.getElementById('batteryStatus');
                                    const chargingIcon = document.getElementById('chargingIcon');

                                    if (batteryInfo.isCharging) {
                                        let chargeMethod = '';
                                        if (batteryInfo.usbCharge) {
                                            chargeMethod = 'via USB';
                                        } else if (batteryInfo.acCharge) {
                                            chargeMethod = 'via AC adapter';
                                        }
                                        statusElement.textContent = 'Charging ' + chargeMethod;
                                        chargingIcon.style.display = 'inline';
                                    } else {
                                        statusElement.textContent = 'Not charging';
                                        chargingIcon.style.display = 'none';
                                    }

                                } catch (e) {
                                    document.getElementById('batteryPercentage').textContent = 'Error getting battery info';
                                    console.error('Error updating battery info:', e);
                                }
                            }

                            // Call when page loads
                            window.onload = function() {
                                // Will be called from WebViewClient's onPageFinished
                                console.log('Page loaded, waiting for battery update...');
                            };
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

