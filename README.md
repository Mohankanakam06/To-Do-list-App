# To-Do List App

A simple and intuitive Android To-Do List application built with Kotlin. It helps users manage their daily tasks efficiently, keeping track of what needs to be done.

## Features

* **Add Tasks**: Easily create new tasks with clear descriptions.
* **View Tasks**: See an organized list of all your pending tasks.
* **Edit/Delete Tasks**: Update existing tasks if your plans change or remove completed ones.
* **Persistent Storage**: Tasks are securely saved on your device using the Room Database, so you won't lose your data when the app closes.
* **Dark Mode Support**: Beautiful and legible UI even in dark mode.
* **Search capabilities**: Find specific tasks quickly using the built-in search bar with smooth animations.

## Tech Stack

* **Language**: Kotlin
* **UI**: XML Layouts (RecyclerView, Material Components)
* **Architecture**: MVVM (Model-View-ViewModel)
* **Local Database**: Room persistence library
* **Asynchronous Processing**: Kotlin Coroutines
* **Build System**: Gradle

## Installation

### Prerequisites

* Android Studio (latest version recommended)
* JDK 17 (or compatible version)

### Steps to Run Locally

1. **Clone the Repository**
   Open your terminal or command prompt and run:
   ```bash
   git clone https://github.com/Mohankanakam06/To-Do-list-App.git
   ```

2. **Open in Android Studio**
   * Launch Android Studio.
   * Select **File > Open**, navigate to the cloned `To-Do-list-App` directory, and select it.

3. **Sync Gradle**
   Android Studio will automatically start syncing the Gradle project. Wait for the sync to complete successfully.

4. **Run the App**
   * Connect an Android device via USB (with USB Debugging enabled) or start an Android Emulator.
   * Click the **Run** button (green play icon) in the Android Studio toolbar to build and install the app on your device.

## Contributing

Contributions are welcome! If you'd like to improve the app or fix a bug, please feel free to fork the repository and submit a pull request.
