# AesPrefs
This tiny Android library provides encryption for the Android Preferences.


## How to use
### 1. Import
Add JitPack to your repositories:

```
   repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
```

and add the library to your dependencies:

```
    compile 'com.github.pepperonas:aesprefs:0.3.0'
```


## Future releases

- get app stats
- get preference stats
- print all prefs
- unit-tests


## ProGuard
```
-keep class com.pepperonas.aesprefs.** { *; }
-dontwarn com.pepperonas.aesprefs.**
```


## Contact

* Martin Pfeffer - https://celox.io - <martin.pfeffer@celox.io>


## License

    Copyright 2017 Martin Pfeffer

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


