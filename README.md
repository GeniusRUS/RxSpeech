# RxSpeech
[![](https://jitpack.io/v/GeniusRUS/RxSpeech.svg)](https://jitpack.io/#GeniusRUS/RxSpeech)

Простая библиотека, оборачивающая запрос голосового ввода в RX, в результате получающая `List<String>`

* Получение текста из голоса
```kotlin
RxSpeech.with(context)
  .setPrompt("Custom message")  // заголовок для окна голосового ввода (необязательно)
  .setLocale(Locale("ru"))      // локаль для окна голосового ввода (необязательно, по-умолчанию системная)
  .setMaxResults(5)             // максимальное количество результатов распознавания (необязательно, по-умолчанию 3)
  .requestText()                // метод запроса
  .subscribeOn(Schedulers.io())
  .observeOn(AndroidSchedulers.mainThread())
  .subscribe { println(it) }
```

## Подключение зависимости

1. Добавить jitpack-репозиторий в ваш __build.gradle__ уровня проекта
```groovy
allprojects {
  repositories {
    maven { url "https://jitpack.io" }
  }
}
```
2. прописать зависимость в __build.gradle__ уровня модуля

```gradle
dependencies {
  implementation 'com.github.geniusrus:rxspeech:$latest_version'
}
```

## Пример

Расположен в модуле `app`

## Разработчик 

* Виктор Лиханов

[Yandex почта](volixanov@unitbean.com)

## Использованные библиотеки

[RxJava](https://github.com/ReactiveX/RxJava)

[RxAndroid](https://github.com/ReactiveX/RxAndroid)

## Лицензия
```
The MIT License (MIT)

Copyright (c) 2018 Viktor Likhanov

Permission is hereby granted, free of charge, to any person obtaining a 
copy of this software and associated documentation files (the "Software"), 
to deal in the Software without restriction, including without limitation 
the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the Software is 
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included 
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

```
