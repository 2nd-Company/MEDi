# MEDi

AI-driven support? for the visually impaired

### Overview

This is a camera app that can detect hand landmarks either from continuous camera frames seen by your device's front camera, an image, or a video from the device's gallery using a custom **task** file.

The task file is downloaded by a Gradle script when you build and run the app. You don't need to do any additional steps to download task files into the project explicitly unless you wish to use your own landmark detection task. If you do use your own task file, place it into the app's *assets* directory.

This application should be run on a physical Android device to take advantage of the camera.

## Build the demo app using Android Studio

### Prerequisites

*   The **[Android Studio](https://developer.android.com/studio/index.html)** IDE. This sample has been tested on Android Studio Dolphin.

*   A physical Android device with a minimum OS version of SDK 24 (Android 7.0 -
    Nougat) with developer mode enabled. The process of enabling developer mode
    may vary by device.

### Building

*   Open Android Studio. From the Welcome screen, select Open an existing
    Android Studio project.

*   From the Open File or Project window that appears, navigate to and select
    the mediapipe/examples/hand_landmarker/android directory. Click OK. You may
    be asked if you trust the project. Select Trust.

*   If it asks you to do a Gradle Sync, click OK.

*   With your Android device connected to your computer and developer mode
    enabled, click on the green Run arrow in Android Studio.

### Models used

Downloading, extraction, and placing the models into the *assets* folder is
managed automatically by the **download.gradle** file.

<!-- Improved compatibility of back to top link: See: https://github.com/othneildrew/Best-README-Template/pull/73 -->
<a id="readme-top"></a>
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->



![GitHub](https://img.shields.io/github/license/stratumauth/app?style=flat)
![GitHub stars](https://img.shields.io/github/stars/stratumauth/app?style=flat)
![GitHub last commit](https://img.shields.io/github/last-commit/stratumauth/app?style=flat)
[![Crowdin](https://badges.crowdin.net/authenticator-pro/localized.svg)](https://crowdin.com/project/authenticator-pro)

![MEDi](./doc/RebrandingMaterial/Wordmark.png)
<br/><br/>

A free open-source two factor authentication app for Android. It features encrypted backups, icons, categories and a high level of customisation. It also has a Wear OS companion app.

It supports TOTP (Time Based) and HOTP (Counter Based) authenticators using either SHA1, SHA256 or SHA512 hashing algorithms. Mobile-Otp (mOTP), Steam and Yandex are also supported.

## Download ⬇️
[<img alt="Get it on Google Play" height="100" src="./doc/googleplay.png">](https://play.google.com/store/apps/details?id=com.stratumauth.app)

> Stratum is currently only available on the F-Droid client through the [IzzyOnDroid repo](https://apt.izzysoft.de/fdroid/). You must first add this repository in the F-Droid client.

## Support development ❤️
[<img alt="Buy Me a Coffee" height="100" src="./doc/buymeacoffee.png">](https://www.buymeacoffee.com/jamiemh)

## Quick Links 🔗

[Request Icons](https://github.com/stratumauth/app/issues/new?assignees=&labels=enhancement&template=icon_request.md&title=)
 
[Frequently Asked Questions](https://github.com/stratumauth/app/wiki#frequently-asked-questions)

[Contribution Guide](https://github.com/stratumauth/app/blob/master/CONTRIBUTING.md)

[Backup File Format](https://github.com/stratumauth/app/blob/master/doc/BACKUP_FORMAT.md)

### Importing from other apps:

[Google Authenticator](https://github.com/stratumauth/app/wiki/Importing-from-Google-Authenticator)

[Blizzard Authenticator](https://github.com/stratumauth/app/wiki/Importing-from-Blizzard-Authenticator)

[Steam](https://github.com/stratumauth/app/wiki/Importing-from-Steam)

[Authy](https://github.com/stratumauth/app/wiki/Importing-from-Authy)

## Features 🪄

⚙️ **Compatibility:** Stratum is compatible with most providers and accounts.
 
💾 **Backup / Restore:** Backup your authenticators with strong encryption. In case you lose your or change phone, you can always gain access to your accounts. Save to cloud storage or to your device.

🌙 **Dark Mode:** Stratum has a beautiful material design inspired look in either light or dark themes.

⏺️ **Icons:** Find your authenticators easily with recognisable brand logos and icons next to each code.

📂 **Categories:** Organise your authenticators into categories.

🔒 **Offline with few permissions:** Stratum only requires a single permission and does not require Internet access to function.

🎨 **Customisation:** Set icons and rename. You can also arrange your authenticators in any order you like so you can find them easily.

⌚ **Wear OS:** Quickly view your authenticators directly from your watch. Please note that a connection to your Android device is required.

## Screenshots 📱

![Screenshot 1](./doc/screenshot1.png)
![Screenshot 2](./doc/screenshot2.png)
![Screenshot 3](./doc/screenshot3.png)
![Screenshot 4](./doc/screenshot4.png)
![Screenshot 5](./doc/screenshot5.png)
![Screenshot 6](./doc/screenshot6.png)
![Screenshot 7](./doc/screenshot7.png)
![Screenshot 8](./doc/screenshot8.png)
<br/><br/>

### Wear OS Companion

![Screenshot 1](./doc/wearos_screenshot1.png)
![Screenshot 2](./doc/wearos_screenshot2.png)
![Screenshot 3](./doc/wearos_screenshot3.png)
![Screenshot 4](./doc/wearos_screenshot4.png)
![Screenshot 5](./doc/wearos_screenshot5.png)

## Permissions 🔒

* Camera permission is required to add accounts through QR codes.

## Disclaimer

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
