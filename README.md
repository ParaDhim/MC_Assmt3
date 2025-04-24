# Wi-Fi Signal Mapper 📶

<div align="center">

![Wi-Fi Signal Mapper Banner](https://github.com/user-attachments/assets/bceb2ac1-b268-4662-bcd0-7800c0a3b6b1)

<p float="left">
  <img width="350" alt="Signal Strength Matrix" src="https://github.com/user-attachments/assets/2697ee60-e2c8-48ba-beba-2d76b8510db5" />
  <img width="338" alt="Location Comparison View" src="https://github.com/user-attachments/assets/519c3b04-0659-4f98-90a0-54b58f3ce6f2" />
</p>

_A modern Android application for mapping and visualizing Wi-Fi signal strengths across different locations_

[Features](#features) • [Screenshots](#screenshots) • [Installation](#installation) • [Usage](#usage) • [Architecture](#architecture) • [License](#license)

</div>

## ✨ Features

- **📊 Real-time Signal Visualization** - Scan and visualize current Wi-Fi signals using color-coded matrices
- **🗺️ Location Management** - Save scans with custom names for different areas in your space
- **🔄 Comparative Analysis** - Toggle between individual and comparison views to analyze signal patterns
- **📱 Modern UI** - Built with Material Design 3 and Jetpack Compose for a fluid experience
- **📶 Access Point Detection** - Identify and display available networks in each location
- **🔒 Permission Handling** - Seamless user experience with proper permission handling

## 📸 Screenshots

<div align="center">
<table>
  <tr>
    <td><img src="screenshots/home_screen.png" width="200"/></td>
    <td><img src="screenshots/signal_map.png" width="200"/></td>
    <td><img src="screenshots/comparison_view.png" width="200"/></td>
  </tr>
  <tr>
    <td align="center"><b>Home Screen</b></td>
    <td align="center"><b>Signal Map</b></td>
    <td align="center"><b>Comparison View</b></td>
  </tr>
</table>
</div>

## 🔧 Installation

### Prerequisites

- Android Studio Arctic Fox or newer
- Android SDK 31 or higher
- Kotlin 1.7.20 or higher

### Setup

```bash
# Clone this repository
git clone https://github.com/yourusername/wifi-signal-mapper.git

# Navigate to the project directory
cd wifi-signal-mapper

# Open with Android Studio and sync Gradle files
```

## 📱 Usage

### Scanning Wi-Fi Signals

1. Launch the app and grant the required location permissions
2. Enter a descriptive name for your current location (e.g., "Living Room")
3. Tap the "Scan" button to begin scanning for Wi-Fi signals
4. The app will display a color-coded matrix representing signal strengths:
   - **Green**: Strong signal
   - **Yellow**: Medium signal
   - **Red**: Weak signal

### Saving Locations

- After scanning, tap "Save" to store the current scan with the specified location name
- Saved locations appear in the list below and can be selected for detailed viewing

### Comparing Locations

- Toggle the "Compare" switch to view a side-by-side comparison of all saved locations
- Compare signal distribution patterns and access point information across different areas

## 🏗️ Architecture

### Tech Stack

- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern toolkit for building native UI
- **Material Design 3** - Latest design system from Google
- **StateFlow** - For reactive state management
- **Android Wi-Fi APIs** - For accessing Wi-Fi functionality



### Signal Visualization Algorithm

The app uses a 10×10 matrix to represent signal strength measurements:

```kotlin
@Composable
fun SignalMatrixVisualization(
    signals: List<Int>,
    modifier: Modifier = Modifier
) {
    val normalizedSignals = remember(signals) {
        // Normalize signals to 0-100 range for visualization
        val minSignal = signals.minOrNull() ?: -100
        val maxSignal = signals.maxOrNull() ?: -30
        val range = max(1, abs(maxSignal - minSignal))

        signals.map { signal ->
            (((signal - minSignal).toFloat() / range) * 100).coerceIn(0f, 100f)
        }
    }
    
    // Drawing logic for the matrix visualization
}
```

## 🛠️ Performance Considerations

- **Efficient Scanning**: The app minimizes battery usage by managing scan frequencies
- **Reactive UI**: Updates are pushed to the UI only when data changes
- **Edge-to-Edge Design**: Full support for notch/cutout areas on modern devices

## 📝 Implementation Details

This project demonstrates:

1. **Permission Handling**: Proper implementation of runtime permissions for location access
2. **Jetpack Compose UI**: Modern declarative UI with Material Design 3
3. **MVVM Architecture**: Clean separation of concerns with ViewModel pattern
4. **Hardware Integration**: Direct interaction with device Wi-Fi hardware
5. **StateFlow**: Reactive programming approach for UI updates
6. **Edge-to-Edge Design**: Support for modern device form factors

## 📄 License

```
MIT License

Copyright (c) 2025 Wi-Fi Signal Mapper Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
