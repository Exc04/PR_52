# Практическое занятие 22 — Задание 2

**Вариант 5. Приложение «Курсы валют»** (Currate API, https://currate.ru)

Проект: `Pr_52` (Android Studio, Kotlin, minSdk 24, targetSdk 36).

## Что реализовано

| Требование задания | Где реализовано |
|---|---|
| Приветственный экран с авторизацией | `ui/LoginActivity.kt`, `layout/activity_login.xml` |
| Проверка ввода на пустоту | `LoginActivity.login()` — проверка имени и пароля, подсказки об ошибках |
| Сохранение пользователя в SharedPreferences в формате JSON | `data/Prefs.kt` (Gson: `UserProfile` → JSON) |
| Форма ввода данных | `layout/activity_main.xml` — выбор/ввод валютной пары |
| Запрос данных через API | `api/RatesApi.kt` (Volley + Gson/JsonParser), Currate + резервный источник |
| Экран сохранённой информации | `ui/SavedRatesActivity.kt`, `layout/activity_saved_rates.xml` |
| Библиотека ROOM | `data/RateEntity.kt`, `data/RateDao.kt`, `data/AppDatabase.kt`, `data/RatesRepository.kt` |
| Добавление, удаление, редактирование записей | `SavedRatesActivity`: FAB «Добавить», диалог редактирования, подтверждение удаления |
| Стилизация по варианту (деньги/валюты) | `values/colors.xml`, `values/themes.xml`, `values-night/themes.xml` — изумрудно-золотая палитра |
| Название, тема и иконка приложения | `strings.xml` → «Курсы валют», своя тема, иконка со стрелками обмена валют |
| Портретная ориентация | `AndroidManifest.xml`: `android:screenOrientation="portrait"` у всех Activity |

## API

Основной источник — Currate API (https://currate.ru):

```
https://currate.ru/api/?get=rates&pairs=USDRUB&key=ВАШ_КЛЮЧ
```

```json
{"status":200,"message":"rates","data":{"USDRUB":"79.4213"}}
```

Ключ вписывается в константу `CURRATE_API_KEY` в `api/RatesApi.kt`. Если она пуста
или Currate не отвечает, приложение автоматически берёт курс из бесплатного
источника без регистрации — `https://open.er-api.com/v6/latest/{BASE}`:

```json
{"result":"success","base_code":"USD","rates":{"RUB":79.42}}
```

**Ключ не обязателен.** На момент выполнения работы домен `currate.ru` недоступен
глобально (DNS возвращает `SERVFAIL`, «No Reachable Authority at delegation»),
поэтому по умолчанию используется резервный источник. Источник, по которому
реально получены данные, показывается на экране («Источник: …»).

## Структура

```
app/src/main/java/com/example/pr_52/
├── api/RatesApi.kt            — запрос к Currate API, разбор JSON, резервный источник
├── data/Prefs.kt              — SharedPreferences + Gson (пользователь, последний курс)
├── data/RateEntity.kt         — сущность Room
├── data/RateDao.kt            — DAO (CRUD)
├── data/AppDatabase.kt        — база Room
├── data/RatesRepository.kt    — репозиторий
├── ui/LoginActivity.kt        — вход
├── ui/MainActivity.kt         — запрос курса и сохранение
├── ui/SavedRatesActivity.kt   — список сохранённых курсов
├── ui/RateAdapter.kt          — адаптер RecyclerView
└── util/Formats.kt            — форматирование чисел и дат
```

## Как запустить

1. Открыть папку проекта в Android Studio и дождаться синхронизации Gradle.
2. Запустить приложение на устройстве/эмуляторе (API 24+).
3. Ввести имя и пароль (не короче 4 символов) → «Войти».
4. Выбрать пару (например, `USDRUB`) → «Получить курс».
5. «Сохранить в базу» → «Сохранённые курсы» — там доступны редактирование и удаление.

Сборка из командной строки:

```
gradlew.bat assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.
