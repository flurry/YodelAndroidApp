# Yahoo Mobile Developer Conference Sample App - Android

This project showcases some of the new mobile development tools released by Yahoo during the 2015 
[Mobile Developer Conference](http://yahoomobiledevcon.tumblr.com/)

## Features

- Showcases integration of Flurry native ads, along with best practices for integrating native 
ads in your Android apps

## Requirements for working with the source:

- Android Studio
- Android SDK Platform Tools r21 or later
- Android SDK Build Tools r21 or later
- Runtime of Android 4.1 (API 16) or later
- Tumblr Developer consumer key and secret

## Gemini native ads
To see best practices for integrating native ads into your app, look through the following classes:

- [GeminiAdFetcher](yodel-sample/src/com/yahoo/mobile/client/android/yodel/GeminiAdFetcher.java): This
shows best practices for fetching and storing native ads to be used in an Android app
- [BaseAdAdapter](yodel-sample/src/com/yahoo/mobile/client/android/yodel/ui/widgets/adapters/BaseAdAdapter.java):
This shows best practices for integrating native ads into a list/stream of data

For more info on getting started with Flurry for Android, see
[here](https://developer.yahoo.com/flurry/docs/analytics/gettingstarted/android/).

## Copyright

    Copyright 2015 Yahoo Inc. All rights reserved.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
         http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
