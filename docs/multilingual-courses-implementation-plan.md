# LinguaLeap: подробный план поддержки нескольких языков и языковых пар

Документ предназначен для последующей реализации без повторного проектирования. Он описывает целевую модель, API, миграцию, изменения frontend, тесты и критерии приёмки.

## 1. Цель

Превратить LinguaLeap из словаря только для испанского языка в однопользовательское приложение с независимыми курсами:

- испанский с переводами на русский и английский;
- английский с переводом на русский;
- французский с переводом на русский и, при необходимости, английский;
- произвольные последующие комбинации изучаемого языка и языков перевода.

Пользователь добавляет курс через верхнюю панель, переключается между курсами и видит только слова, списки, статистику и повторения активного курса.

## 2. Рамки первой реализации

### Входит

1. Создание и переключение курсов.
2. Миграция текущих данных в курс «Испанский — RU/EN».
3. Динамический набор переводов вместо обязательных `russianVariant` и `englishVariant`.
4. Полная изоляция слов, списков, отложенных слов, статистики и повторений.
5. Динамическая озвучка по локали изучаемого языка.
6. Языковые модули словарей, грамматических подписей и упражнений.
7. Английский и французский как новые курсы для основного словаря и карточек.
8. Сохранение текущего алгоритма SM-2 без изменения формул.

### Не входит

1. Общая очередь повторений по всем языкам.
2. Независимый SM-2 для направлений «туда» и «обратно».
3. Общие списки между курсами.
4. Совместное использование одного слова несколькими курсами.
5. Удаление и архивирование курсов.
6. Английские и французские числительные в первом этапе.
7. Многопользовательские права и аккаунты.
8. Изменение frontend-маршрутов: в MVP они остаются текущими.

## 3. Основные понятия и инварианты

### 3.1. Термины

- **Language** — справочник языка: `es`, `en`, `fr`, `ru`.
- **LanguageCourse** — независимый словарь: изучаемый язык плюс упорядоченный список языков перевода.
- **Primary translation language** — первый язык перевода курса; именно он обязателен при добавлении значения.
- **UI locale** — язык интерфейса из Vue I18n. Он не связан с языком курса.
- **Active course** — выбранный на клиенте курс. Сервер не хранит активный курс.

### 3.2. Обязательные правила

1. Каждое `ForeignWord` принадлежит ровно одному курсу.
2. Одинаковое написание допускается в разных курсах.
3. Внутри одного курса написание слова остаётся уникальным.
4. Каждый список принадлежит одному курсу и содержит только его значения.
5. Автоматические списки создаются отдельно для каждого курса.
6. Каждое `WordToAddLater` принадлежит одному курсу.
7. Все выборки слов, статистики и повторений содержат условие по `courseId`.
8. Операции по `meaningId`, `listId` и `collocationId` проверяют принадлежность курсу из URL.
9. В курсе есть минимум один язык перевода.
10. Изучаемый язык не может одновременно быть языком перевода.
11. Языки перевода не повторяются; их порядок сохраняется.
12. Прогресс SM-2 относится к `WordMeaning` и общий для всех переводов внутри курса.
13. Все HTTP-методы активного словаря получают `courseId` явно; не использовать серверную сессию или глобальную переменную.

## 4. Целевая модель данных

### 4.1. `Language`

Новая JPA-сущность, таблица `languages`.

Поля:

- `String code` — первичный ключ, ISO 639-1 в нижнем регистре;
- `String defaultName` — имя по умолчанию;
- `String defaultSpeechLocale` — например `es-ES`, `en-US`, `fr-FR`, `ru-RU`.

Начальные записи: `es`, `en`, `fr`, `ru`.

Названия в UI локализуются через Vue I18n; `defaultName` нужен как fallback.

### 4.2. `LanguageCourse`

Новая JPA-сущность, таблица `courses`.

Поля:

- `Long id`;
- `Language targetLanguage`, связь `@ManyToOne(optional = false)`;
- `LocalDate createdAt`;
- `List<CourseTranslationLanguage> translationLanguages`, cascade all, orphan removal.

Не хранить вычисляемое название курса. Название формируется по `targetLanguage` в UI.

### 4.3. `CourseTranslationLanguage`

Новая JPA-сущность, таблица `course_translation_languages`.

Поля:

- суррогатный `Long id`;
- `LanguageCourse course`;
- `Language language`;
- `int displayOrder`.

Ограничения:

- unique `(course_id, language_code)`;
- unique `(course_id, display_order)`;
- `displayOrder = 0` обозначает основной язык перевода.

Для небольшого проекта суррогатный ID проще в JPA, чем составной ключ.

### 4.4. `ForeignWord`

Сохранить таблицу `words` и существующие ID.

Добавить:

- `LanguageCourse course`, `@ManyToOne(optional = false)`, колонка `course_id`.

Изменить уникальность:

- удалить старое global unique по `word`;
- добавить unique `(course_id, word)`.

Не переводить хранимое написание в нижний регистр. Перед поиском и сохранением делать `trim()`. Сохранить текущее case-sensitive поведение основного словаря.

### 4.5. `WordMeaning`

Удалить из Java-модели:

- `russianVariant`;
- `englishVariant`.

Добавить:

```java
@OneToMany(mappedBy = "meaning", cascade = CascadeType.ALL, orphanRemoval = true)
private List<WordMeaningTranslation> translations;
```

Остальные поля, включая POS, gender, definition, image, lists, contexts и learning status, сохранить.

### 4.6. `WordMeaningTranslation`

Новая сущность, таблица `meaning_translations`.

Поля:

- `Long id`;
- `WordMeaning meaning`;
- `Language language`;
- `String text`.

Ограничение: unique `(meaning_id, language_code)`.

Пустые переводы не сохранять. Основной перевод обязателен и проверяется сервисом.

### 4.7. `WordMeaningCollocation`

Удалить из Java-модели:

- `translationRussian`;
- `translationEnglish`.

Добавить список `WordMeaningCollocationTranslation`.

Новая таблица `collocation_translations`:

- `id`;
- `collocation_id`;
- `language_code`;
- `text`;
- unique `(collocation_id, language_code)`.

Переводы коллокаций остаются необязательными, как сейчас.

### 4.8. `WordMeaningContext`

В MVP оставить `sentence` и одно поле `translation`.

Семантика: перевод контекста записывается на основном языке перевода курса. Не создавать отдельную таблицу переводов контекстов в этом этапе.

### 4.9. `VocabularyList`

Добавить:

- `LanguageCourse course`, обязательная связь;
- `SmartListType smartType`, nullable, `@Enumerated(EnumType.STRING)`.

Новый enum:

```java
public enum SmartListType {
    RECENTLY_ADDED,
    PROBLEM_WORDS
}
```

Для минимизации миграции временно сохранить существующее поле `smart`:

- `smart = true` и `smartType != null` для автоматических списков;
- `smart = false` и `smartType = null` для обычных.

Поиск автоматического списка выполнять по `(courseId, smartType)`, а не по имени. Имя остаётся отображаемой строкой и может локализоваться позже.

Добавить DB-ограничение unique `(course_id, smart_type)`. SQLite и H2 допускают несколько NULL в уникальном индексе, поэтому обычные списки с `smartType = null` не будут конфликтовать. Дополнительно сервис ищет/создаёт автоматический список в транзакции.

### 4.10. `WordToAddLater`

Заменить первичный ключ `word` на:

- `Long id`;
- `LanguageCourse course`;
- `String word`;
- `int timesAdded`.

Добавить unique `(course_id, word)`. Репозиторий становится `ListCrudRepository<WordToAddLater, Long>`.

### 4.11. `SM2WordMeaningMeta`

Сущность и формулы не менять.

Меняются только запросы: путь курса `meta.word.word.course.id`.

Не переименовывать существующую колонку `sm2_meta.word_id` в рамках этого этапа.

## 5. DTO и JSON-контракты

### 5.1. Новые DTO

```java
public record LanguageDto(
        String code,
        String name,
        String defaultSpeechLocale
) {}
```

```java
public record CourseDto(
        Long id,
        String targetLanguageCode,
        String targetLanguageName,
        String defaultSpeechLocale,
        List<String> translationLanguageCodes,
        long totalWords,
        int dueToday
) {}
```

```java
public record CreateCourseRequestDto(
        String targetLanguageCode,
        List<String> translationLanguageCodes
) {}
```

```java
public record EditWordMeaningTranslationsDto(
        Map<String, String> translations
) {}
```

`translationLanguageCodes` всегда отсортирован по `displayOrder`.

### 5.2. Изменения существующих DTO

В `ForeignWordDto` и `WordMeaningDto` заменить:

```text
russianVariant: String
englishVariant: String
```

на:

```text
translations: Map<String, String>
```

В `WordMeaningCollocationDto` заменить `translationRussian` и `translationEnglish` на `translations: Map<String, String>`.

Во frontend использовать `Record<string, string>`; порядок вывода определяется не порядком ключей JSON, а `translationLanguageCodes` активного курса.

`CreateWordRequestDto` не получает `courseId`: курс уже присутствует в URL.

### 5.3. Пример создания курса

```json
{
  "targetLanguageCode": "en",
  "translationLanguageCodes": ["ru"]
}
```

Ответ: `201 Created` и `CourseDto`.

### 5.4. Пример создания английского слова

```json
{
  "word": "table",
  "addedDate": "2026-11-12",
  "meanings": [
    {
      "pos": "NOUN",
      "gender": null,
      "translations": {
        "ru": "стол"
      },
      "definition": "a piece of furniture",
      "frequency": 1,
      "collocations": [],
      "contexts": [],
      "lists": []
    }
  ]
}
```

### 5.5. Обновление переводов

`PATCH /api/courses/{courseId}/meanings/{meaningId}/translations`:

```json
{
  "translations": {
    "ru": "стол; таблица"
  }
}
```

Семантика PATCH:

- обновлять только переданные языки;
- неизменённые языки не трогать;
- пустая строка удаляет необязательный перевод;
- основной перевод нельзя удалить или сделать пустым;
- неизвестный язык или язык вне курса — `400 Bad Request`.

## 6. Целевой REST API

### 6.1. Глобальные endpoints

| Метод | URL | Назначение |
|---|---|---|
| GET | `/api/languages` | Справочник доступных языков |
| GET | `/api/courses` | Список курсов со счётчиками |
| POST | `/api/courses` | Создать курс |

Для `GET /api/courses` счётчики вычислять по каждому курсу. Оптимизацию агрегированным запросом делать только при необходимости.

### 6.2. Words: префикс `/api/courses/{courseId}/words`

Сохранить текущие операции, добавив префикс курса:

- `POST /`;
- `GET /`;
- `GET /autosuggestions?word=...`;
- `GET /{word}`;
- `DELETE /{word}`;
- `GET /total`;
- `GET /check?word=...`;
- `POST /later/{word}`;
- `GET /later`;
- `DELETE /later/{word}`;
- `GET /statistics`.

Важно сохранить объявление конкретных путей `/total`, `/check`, `/later` до универсального `/{word}`, если Spring потребует устранить неоднозначность. Существующее сопоставление Spring обычно выбирает более конкретный путь.

Во frontend path variable передавать через `encodeURIComponent`.

### 6.3. Meanings: префикс `/api/courses/{courseId}/meanings`

- `POST /{meaningId}/contexts`;
- `POST /{meaningId}/collocations`;
- `PATCH /{meaningId}/translations` вместо `/variants`;
- `PATCH /{meaningId}/definition`;
- `PATCH /{meaningId}/image`;
- `PUT /{meaningId}/collocations/{collocationId}`;
- `DELETE /{meaningId}/collocations/{collocationId}`;
- `GET /statistics`.

### 6.4. Lists: префикс `/api/courses/{courseId}/lists`

- `POST /`;
- `GET /?excludeSmart=...`;
- `GET /{listId}`;
- `GET /pos/{pos}`;
- `GET /date/{date}`;
- `DELETE /{listId}/words/{wordMeaningId}`;
- `DELETE /{listId}`.

### 6.5. Repetition: префикс `/api/courses/{courseId}/repetition`

- `GET /today/total`;
- `GET /today`;
- `GET /summary/graph`;
- `PUT /meanings/{meaningId}?newScore=...`.

### 6.6. Ошибки

- отсутствующий курс — `404`;
- слово, значение, список или коллокация вне курса — `404`, без раскрытия чужого ресурса;
- невалидный состав языков курса — `400`;
- пустой основной перевод — `400`;
- некорректная оценка SM-2 — сохранить текущую валидацию `0..5`.

Добавить небольшой `NotFoundException` с `@ResponseStatus(HttpStatus.NOT_FOUND)` вместо `NoSuchElementException` для новых проверок.

## 7. Изменения backend по файлам

### 7.1. Новые файлы

Модели:

- `models/Language.java`;
- `models/LanguageCourse.java`;
- `models/CourseTranslationLanguage.java`;
- `models/WordMeaningTranslation.java`;
- `models/WordMeaningCollocationTranslation.java`.

Enum:

- `enums/SmartListType.java`.

DTO:

- `dto/LanguageDto.java`;
- `dto/CourseDto.java`;
- `dto/CreateCourseRequestDto.java`;
- `dto/EditWordMeaningTranslationsDto.java`.

Репозитории:

- `repositories/LanguagesRepository.java`;
- `repositories/LanguageCoursesRepository.java`;
- `repositories/CourseTranslationLanguagesRepository.java`.

Сервисы:

- `services/LanguageCoursesService.java`;
- `services/impl/LanguageCoursesServiceImpl.java`.

Контроллеры:

- `controllers/LanguagesController.java`;
- `controllers/LanguageCoursesController.java`.

Исключение:

- `services/NotFoundException.java` или отдельный пакет `exceptions`, если появятся другие типы ошибок.

### 7.2. `ForeignWordsRepository`

Заменить глобальные методы на курсовые:

- `findByCourseIdOrderByAddedDesc(Long courseId, Pageable pageable)`;
- `findFirst10ByCourseIdAndWordContaining(Long courseId, String word)`;
- `findByCourseIdAndWord(Long courseId, String word)`;
- `findByCourseIdAndWordIgnoreCase(Long courseId, String word)`;
- `existsByCourseIdAndWord(Long courseId, String word)`;
- `countByCourseId(Long courseId)`.

В `getAddedWordsStatistics` добавить `w.course.id = :courseId`.

### 7.3. `WordsToAddLaterRepository`

Методы:

- `findByCourseIdAndWordIgnoreCase(Long courseId, String word)`;
- `findAllByCourseId(Long courseId)`;
- `deleteByCourseIdAndWord(Long courseId, String word)`.

Удаление по двум вариантам регистра заменить поиском сущности и удалением по ID, чтобы не зависеть от старого String PK.

### 7.4. `WordMeaningsRepository`

Добавить `courseId` во все методы:

- статистика POS: `WHERE m.word.course.id = :courseId`;
- POS: `findAllByWordCourseIdAndPos(Long courseId, PartOfSpeech pos)`;
- дата: `WHERE m.word.course.id = :courseId AND m.word.added = :date`.

### 7.5. `SM2WordMeaningMetaRepository`

Все запросы получают `courseId`:

- количество сегодня;
- список сегодня;
- статистика планируемых повторений.

Пример условия JPQL:

```java
m.word.word.course.id = :courseId
```

Метод `findByWordId` сохранить для внутренних операций, но контроллерский score должен дополнительно проверить курс.

### 7.6. `VocabularyListsRepository`

Методы:

- `findAllByCourseId(Long courseId)`;
- `findByCourseIdAndId(Long courseId, Long id)`;
- `findByCourseIdAndSmartType(Long courseId, SmartListType smartType)`.

### 7.7. `ForeignWordsService` и реализация

Добавить `courseId` первым аргументом во все публичные операции.

`createWord(courseId, request)`:

1. Получить курс или выбросить `NotFoundException`.
2. Пометить метод `@Transactional`.
3. Найти слово по `(courseId, trimmedWord)` или создать с этим курсом.
4. Для каждого значения нормализовать переводы: trim, пустые значения удалить.
5. Проверить наличие непустого primary translation.
6. Проверить, что языки перевода принадлежат курсу.
7. Проверить дубликат значения по нормализованной map переводов, сохраняя текущую логику «одинаковые переводы = дубликат».
8. При добавлении в список проверить, что `list.course.id == courseId`.
9. Использовать автоматический список `(courseId, RECENTLY_ADDED)`.
10. Создать переводы коллокаций через новые сущности.
11. Сохранить слово, затем SM-2 для каждого нового значения.
12. Удалить отложенное слово только внутри этого курса.

`getBy`, `checkIfExist`, `delete` и остальные методы работают только с `(courseId, word)`.

### 7.8. `WordMeaningsService` и реализация

Добавить `courseId` во все методы.

Перед изменением значения проверять `meaning.word.course.id`.

`editVariants` переименовать в `editTranslations`; использовать `EditWordMeaningTranslationsDto`.

Для коллокаций:

- обновлять `translations` по языкам;
- проверять, что collocation принадлежит meaning;
- проверять, что meaning принадлежит courseId;
- не сохранять пустые переводы.

### 7.9. `VocabularyListsService` и реализация

Все методы получают `courseId`.

`getSmartListOfRecentlyAdded(courseId)` и `getSmartListOfProblemWords(courseId)` используют `smartType`.

`upsertList(courseId, request)` создаёт список с текущим курсом.

`removeWordFromList` и добавление слова в список проверяют совпадение курса у списка и значения.

`getByDate` использует локаль изучаемого языка курса для названия списка, а не постоянный `es-ES`.

### 7.10. `SpacedRepetitionService` и реализация

Сигнатуры:

- `getTodayCount(Long courseId)`;
- `getTodayWords(Long courseId)`;
- `getSummaryGraph(Long courseId)`;
- `updateWordMeaningInfo(Long courseId, Long meaningId, int grade)`.

Внутренние `addWordMeaning` и `deleteWordMeaning` можно оставить без courseId, поскольку курс доступен через значение.

Сохранить текущее поведение: если среди просроченных есть ранее повторявшиеся значения, новые исключаются из сессии. Не менять это в рамках мультиязычности.

### 7.11. `Mappings`

Добавить mapping для Language и Course.

Для переводов использовать `LinkedHashMap` и порядок `course.translationLanguages`.

Написать единый helper:

```java
private static Map<String, String> mapTranslations(...)
```

Не допускать null вместо map: возвращать пустую map, если переводов нет.

### 7.12. Контроллеры

Перенести courseId из `@PathVariable` в сервисы во всех перечисленных endpoints.

Не фильтровать данные в контроллере после получения глобального списка: фильтр должен быть в repository/service query, чтобы count, pagination и statistics были корректны.

## 8. Миграция SQLite

### 8.1. Инструмент

Добавить Flyway через `org.flywaydb:flyway-core` в `backend/pom.xml`.

Основная конфигурация:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 1
```

Файлы:

- `backend/src/main/resources/db/migration/V1__legacy_baseline.sql`;
- `backend/src/main/resources/db/migration/V2__multilingual_courses.sql`.

`V1` описывает старую схему для новой пустой SQLite-базы. На существующей непустой базе без истории Flyway baseline-version=1 позволяет начать с V2. Перед применением проверить это поведение на двух временных копиях: пустой и заполненной legacy-схемой.

Для H2-тестов:

```yaml
spring:
  flyway:
    enabled: false
  jpa:
    hibernate:
      ddl-auto: create-drop
```

Таким образом H2 создаётся из текущих JPA-сущностей, а SQLite-миграция проверяется отдельным тестом на SQLite.

### 8.2. Обязательная подготовка

1. Остановить backend.
2. Скопировать текущий `${DB_PATH}` в отдельный backup с датой.
3. Не запускать миграцию сначала на рабочем файле.
4. Создать временную копию и запускать приложение с `DB_PATH` этой копии.
5. Не менять файлы в `backups/` автоматически.

### 8.3. Содержание V2

1. Создать `languages`, `courses`, `course_translation_languages`.
2. Вставить `es`, `en`, `fr`, `ru`.
3. Создать один курс испанского с `createdAt = CURRENT_DATE`.
4. Добавить RU с order 0 и EN с order 1.
5. Создать `meaning_translations` и перенести:
   - `russian_variant` → RU;
   - `english_variant` → EN;
   - NULL и пустые строки пропускать.
6. Создать `collocation_translations` и аналогично перенести оба перевода.
7. Перестроить `words`, потому что старое unique(word) нельзя надёжно удалить простым `ALTER TABLE`:
   - остановить backend перед миграцией;
   - выполнить SQLite table rebuild в одной транзакции;
   - сохранить исходные `id`, `word`, `added`;
   - заполнить `course_id` ID испанского курса;
   - создать unique `(course_id, word)`;
   - сохранить FK от `meanings.word_id` к итоговой таблице `words`;
   - после миграции выполнить `PRAGMA foreign_key_check`.
8. Добавить `course_id` и `smart_type` к `vocabulary_lists`:
   - все существующие записи привязать к испанскому курсу;
   - `New words` → `RECENTLY_ADDED`;
   - `Problem words` → `PROBLEM_WORDS`;
   - остальные → NULL.
9. Перестроить `words_to_add_later`:
   - добавить surrogate id и course_id;
   - сохранить word/timesAdded;
   - привязать к испанскому курсу;
   - добавить unique `(course_id, word)`.
10. Старые колонки переводов пока оставить в БД как часть резервного слоя миграции, но удалить из Java-модели. Поле `smart` сохраняется и в БД, и в Java до отдельного будущего рефакторинга. Отдельная миграция удаления старых колонок не требуется в MVP.

При перестроении SQLite использовать явно протестированную последовательность с отключёнными внешними ключами и `PRAGMA legacy_alter_table=ON`, чтобы rename старой таблицы не переписал FK зависимых таблиц на временное имя. Не полагаться на Hibernate для этого шага.

### 8.4. Проверки после миграции

- число слов после = числу слов до;
- число meanings, collocations, contexts и sm2_meta не изменилось;
- каждый старый непустой русский/английский перевод имеет соответствующую новую запись;
- все words/lists/words_to_add_later имеют course_id испанского курса;
- ID всех старых сущностей сохранены;
- `PRAGMA foreign_key_check` возвращает пустой результат;
- приложение запускается с `ddl-auto=validate`;
- все старые слова доступны в UI;
- старые даты и планы повторений не изменились.

Откат: остановить backend и восстановить скопированный DB-файл. Не пытаться обратной Java-миграцией переносить уже изменённые данные.

## 9. Frontend: состояние активного курса

### 9.1. Не добавлять Pinia в MVP

Создать `frontend/src/composables/useCourses.ts` на модульном `reactive`-состоянии.

Состояние:

- `courses: CourseDto[]`;
- `activeCourseId: number | null`;
- `loaded: boolean`;
- `loading: Promise<void> | null`.

Computed:

- `activeCourse`;
- `hasCourses`.

Методы:

- `loadCourses()` — один общий запрос без дублей;
- `selectCourse(id)` — проверить существование, сохранить ID в `localStorage`;
- `createCourse(request)` — POST, добавить курс в список и сделать активным;
- `refreshCourses()` — обновить счётчики.

Ключ localStorage: `lingualeap:activeCourseId`.

Правила выбора при загрузке:

1. Использовать сохранённый ID, если курс существует.
2. Иначе выбрать первый курс.
3. Если курсов нет, оставить null и показать форму создания.

API-клиент не должен неявно читать activeCourseId: каждый метод получает его параметром.

### 9.2. Новые компоненты

`components/CourseSwitcher.vue`:

- select/dropdown в верхней navbar;
- название изучаемого языка и языков перевода;
- dueToday как badge;
- пункт «+ Добавить язык»;
- при переключении вызывает `selectCourse`.

`components/CourseCreateModal.vue`:

- select изучаемого языка;
- primary select основного языка перевода и optional secondary select дополнительного языка;
- primary translation обязателен;
- target исключается из списка переводов;
- submit создаёт курс и закрывает modal;
- ошибки API отображаются внутри modal.

Для простоты и однозначности порядка в MVP использовать два select:

1. «Основной язык перевода» — обязательный;
2. «Дополнительный язык перевода» — необязательный.

На backend модель всё равно остаётся списком с произвольной длиной.

### 9.3. `App.vue`

1. Добавить `CourseSwitcher` рядом с brand.
2. На mount сначала `await loadCourses()`.
3. Все счётчики загружать только при наличии activeCourseId.
4. Добавить `watch(activeCourseId, ...)` для totalWords, dueToday, POS statistics.
5. Изменить key router-view:

```vue
<router-view :key="`${activeCourseId}:${$route.fullPath}`" ... />
```

Это принудительно пересоздаёт текущую страницу и сбрасывает pagination/учебную сессию при смене языка.

6. Не вызывать старые глобальные API без courseId.
7. При создании нового слова обновлять счётчики только активного курса.
8. Если курсов нет, не загружать feed/study/lists и показать `CourseCreateModal`.

### 9.4. `api/backend-api.ts`

Добавить типы `LanguageDto`, `CourseDto`, `CreateCourseRequestDto`.

Все курсовые методы принимают `courseId` первым параметром. Пример:

```ts
getWordsToRepeatToday(courseId: number)
getAllLists(courseId: number, excludeSmart = false)
addNewWord(courseId: number, body: AddNewWordRequestDto)
```

Создать helper:

```ts
const courseBase = (courseId: number) => `/courses/${courseId}`;
```

Обновить интерфейсы:

- `russianVariant`/`englishVariant` → `translations: Record<string, string>`;
- translationRussian/translationEnglish → translations;
- `editVariants` → `editTranslations`.

Heartbeat, security и logout scheduler не менять.

## 10. Frontend: динамические переводы и страницы

### 10.1. Общие helpers

Создать `frontend/src/utils/translations.ts`:

- `translation(item, languageCode): string`;
- `primaryTranslation(item, course): string`;
- `orderedTranslations(item, course): Array<{ code, text }>`;
- `normalizeTranslations(input): Record<string, string>`.

Не читать русский/английский напрямую в компонентах.

### 10.2. `WordFormView.vue`

1. Получать activeCourse через composable.
2. Поля переводов генерировать `v-for` по `translationLanguageCodes`.
3. Основной перевод required; дополнительный optional.
4. Для коллокаций аналогично генерировать поля.
5. Флаги заменить на локализованное название или код языка.
6. При смене курса форма пересоздаётся через router-view key.
7. Списки загружать с activeCourseId.
8. При submit отправлять translations и courseId.
9. После создания обновить courses/counters активного курса.
10. Удалить или явно оставить нерабочий `processWord` вне scope; не связывать его с мультиязычной реализацией.

### 10.3. `WordDetailView.vue`

1. Загружать слово по activeCourseId и word.
2. Выводить переводы в порядке курса.
3. Редактировать каждую доступную языковую версию через общий PATCH translations.
4. Переводы коллокаций сделать динамическими.
5. Все операции definition/image/collocation/delete получают courseId.
6. При открытом слове невозможно переключить курс и оставить старые данные: страница пересоздаётся, затем ищет то же написание уже в новом курсе. Если слова там нет, показать 404/empty state. Это допустимо для MVP.

### 10.4. `FlashCarousel.vue`

Вопрос:

- H1 — primary translation;
- остальные переводы — вторичные подсказки со spoiler;
- collocation translations — по языкам курса.

Ответ:

- изучаемое слово;
- все переводы в порядке курса;
- языковые названия вместо жёстких флагов;
- article перед существительным берётся из language module;
- TTS вызывается с activeCourse, а не с глобальным испанским голосом.

SM-2 score вызывается с courseId. После оценки обновляются счётчики активного курса.

### 10.5. `WordsTable.vue`

Заголовок Meaning может переключать язык перевода, но только среди языков активного курса.

Не использовать boolean `meaningValue`: хранить `selectedTranslationLanguageCode`. При смене курса компонент пересоздаётся.

### 10.6. Остальные компоненты и views

Обновить все вызовы API на courseId:

- `FeedView.vue` — также сбрасывать page, stopLoading и lastLoadedWordId при пересоздании;
- `StudyView.vue`;
- `StudyAsTableView.vue`;
- `ListsView.vue`;
- `ListDetailView.vue`;
- `WordSearchForm.vue`;
- `AddNewContextForm.vue`;
- `AddNewCollocationForm.vue`.

`RecentlyAddedWords.vue` и `WordContextMenu.vue` получают языковой модуль/курс для detail link и внешних словарей.

`ListsView` показывает только списки активного курса; одинаковые имена в разных курсах допустимы.

### 10.7. Локализация

Вынести в `locales/*.json` новые строки:

- названия курсов и языков;
- создание курса;
- primary/secondary translation;
- отсутствие курсов;
- ошибки валидации;
- текущие испанские строки `palabras`, `Total de palabras...`, `Ocultar/Mostrar` перевести на UI locale.

В `constants.ts` убрать использование `labelEsp` в общей статистике. Использовать label текущего UI locale; target-specific термины добавлять только через языковой модуль.

## 11. Языковые модули frontend

### 11.1. Реестр

Создать `frontend/src/languages/`:

- `types.ts`;
- `index.ts` с registry;
- `spanish.ts`;
- `english.ts`;
- `french.ts`;
- `fallback.ts`.

Интерфейс:

```ts
interface LanguageModule {
    code: string;
    speechLocale: string;
    nounArticles: Partial<Record<GrammaticalGender, string>>;
    dictionaries: DictionaryServiceFactory[];
    supportsNumbers: boolean;
    supportsAbbreviations: boolean;
}
```

`DictionaryServiceFactory` получает word и translation language, чтобы ссылки учитывали пару языков.

Для неизвестного языка fallback:

- нет артиклей;
- нет специальных словарей;
- TTS по language code;
- numbers disabled;
- основной словарь и карточки работают.

### 11.2. Словари

Перенести текущие испанские сервисы в `spanish.ts` без изменения их URL.

Для English и French добавить отдельные конфигурации, не смешивая их с испанскими. Если для пары не настроен ни один словарь, не показывать пустой или заведомо неправильный блок ссылок.

### 11.3. Грамматика

`FlashCarousel` не содержит строк EL/LA напрямую.

Испанский модуль: MASCULINE=EL, FEMININE=LA.

Французский модуль: MASCULINE=LE, FEMININE=LA.

Английский модуль: артикли по gender не выводятся.

Общий enum GrammaticalGender оставить.

### 11.4. TTS

Переписать `utils/voice.ts`:

- не создавать статический `voiceList` только для `es-`;
- `getVoices(languageModule)` фильтрует сначала по полной locale, затем по base language;
- подписаться на `speechSynthesis.voiceschanged`;
- перед воспроизведением задавать `utterance.lang`;
- голос можно сохранять по `voiceURI` в localStorage с ключом `lingualeap:voice:{courseId}`;
- если Paulina недоступна, выбирать первый голос нужной локали;
- если голосов нет, скрыть выбор, но оставить текстовые карточки рабочими.

### 11.5. Числа

Текущий `numberToWords.ts` остаётся испанской реализацией.

В `StudyView` пункт Numbers показывать только если `supportsNumbers`.

При прямом открытии `/study/numbers` для unsupported language показывать понятное empty state, а не испанский генератор.

Английский и французский генераторы добавляются позже отдельными функциями и тестами; не копировать испанский алгоритм как универсальный.

### 11.6. Аббревиатуры

В первом этапе заменить hardcoded `items` данными активного курса:

- загрузить `GET /lists/pos/ABBREVIATION`;
- использовать `word` записей;
- при 0 записей показать empty state;
- при 1 записи не входить в бесконечный цикл повтора;
- TTS использовать язык активного курса.

## 12. Тесты backend

### 12.1. Обновить существующие

`ForeignWordsServiceImplTest` и `ForeignWordsControllerTest`:

- в `beforeEach` создавать испанский и английский тестовые курсы;
- передавать courseId во все методы;
- обновить конструкторы DTO на translations;
- очистка репозиториев должна учитывать новые связи и не удалять справочник языков случайно.

`WordMeaningCollocationTest` оставить для pattern logic, обновить только конструкторы при необходимости.

### 12.2. Новые тесты курсов

`LanguageCoursesServiceImplTest`:

1. Создание `EN + [RU]`.
2. Создание `FR + [RU, EN]` с сохранением порядка.
3. Пустой список переводов → 400/IllegalArgumentException.
4. Target среди переводов → ошибка.
5. Дубликаты языков → ошибка.
6. Неизвестный language code → ошибка.
7. `GET /courses` возвращает корректные totalWords/dueToday.

### 12.3. Изоляция слов

`ForeignWordsServiceImplTest` добавить:

1. Одно и то же `table` можно создать в ES и EN.
2. Повторное создание в одном курсе не создаёт второе word.
3. Одинаковые переводы значения не создают дубликат meaning.
4. Разные переводы позволяют добавить новое meaning.
5. Пустой primary translation отклоняется.
6. Secondary translation может отсутствовать.
7. Нельзя передать язык перевода вне курса.
8. Recently added, autocomplete, total, check, statistics возвращают только активный курс.
9. Add later с одинаковым написанием независимы по курсам.
10. Удаление слова одного курса не меняет другой курс.

### 12.4. Изоляция списков

`VocabularyListsServiceImplTest`:

1. Обычные списки разделены по курсам.
2. Каждый курс получает собственный RECENTLY_ADDED.
3. Каждый курс получает собственный PROBLEM_WORDS.
4. Нельзя добавить meaning другого курса в список.
5. POS/date lists фильтруются курсом.
6. Нельзя удалить/изменить список через чужой courseId.
7. Переводы collocation-as-meaning в getByDate корректно собираются в map.

### 12.5. Изоляция повторений

`SpacedRepetitionServiceImplTest`:

1. today count отдельно для двух курсов.
2. today words отдельно.
3. summary graph отдельно.
4. Score через правильный курс меняет только выбранное meaning.
5. Score через чужой курс → 404 и SM-2 не меняется.
6. Problem words добавляется в список того же курса.
7. Recently added удаляется из списка того же курса.
8. Существующие граничные тесты формул SM-2, если они будут добавлены, не зависят от языка.

### 12.6. Контрактные тесты контроллеров

Добавить MockMvc-проверки:

- новые URL содержат courseId;
- старый `/api/words` больше не используется и возвращает 404;
- DTO содержит translations и не содержит russianVariant/englishVariant;
- курс в path проверяется для каждого семейства endpoints.

### 12.7. Тест миграции SQLite

Создать отдельный тест/проверочный сценарий на временном SQLite-файле:

1. Создать legacy-схему и заполнить минимум два words с meanings, translations, lists, contexts, collocations и sm2_meta.
2. Прогнать V1/V2 так же, как production.
3. Выполнить все проверки из раздела 8.4.
4. Удалить только временный файл.

H2 не заменяет этот тест, потому что rebuild таблиц специфичен для SQLite.

## 13. Тесты frontend

Добавить Jest-тесты:

1. `useCourses` выбирает сохранённый курс.
2. Невалидный сохранённый ID заменяется первым курсом.
3. `selectCourse` пишет localStorage.
4. При смене activeCourseId router-view key меняется.
5. API methods формируют URL с нужным courseId.
6. `orderedTranslations` использует порядок курса, а не порядок ключей объекта.
7. WordForm создаёт обязательное primary и optional secondary поле.
8. Для EN+[RU] нет поля перевода EN.
9. FlashCarousel отображает переводы активного курса.
10. Language module возвращает EL/LA для ES, LE/LA для FR и пусто для EN.
11. Voice filter работает для es-ES/en-US/fr-FR и fallback base language.
12. Numbers скрыты для unsupported language.
13. Abbreviations с 0 и 1 элементом не зависают.

Сохранить существующий `numberToWords.spec.ts` без изменения ожиданий.

## 14. Рекомендуемый порядок кодирования

Каждый пункт должен заканчиваться компилируемым состоянием и затронутыми тестами.

### Этап 1. Подготовка

1. Зафиксировать backup текущей SQLite.
2. Добавить тестовые fixtures двух курсов.
3. Добавить Flyway и конфигурации main/test.
4. Написать и проверить миграцию на копии базы.

### Этап 2. Backend model и API курсов

1. Добавить Language, LanguageCourse, CourseTranslationLanguage.
2. Добавить repositories, services, controllers и DTO.
3. Реализовать GET languages, GET/POST courses.
4. Покрыть валидацию курсов тестами.

### Этап 3. Динамические переводы

1. Добавить новые translation entities.
2. Изменить WordMeaning/Collocation и DTO.
3. Обновить Mappings.
4. Обновить создание и редактирование переводов.
5. Обновить backend tests.

### Этап 4. Course scope backend

1. ForeignWord и все word endpoints.
2. WordToAddLater.
3. VocabularyList и smartType.
4. POS/date statistics.
5. Repetition queries и score.
6. Проверки принадлежности courseId.
7. Тесты изоляции.

### Этап 5. Frontend course state

1. Добавить типы и новые API methods.
2. Добавить `useCourses`.
3. Добавить CourseSwitcher и CourseCreateModal.
4. Подключить App.vue и watch активного курса.
5. Обновить все вызовы API.

### Этап 6. Frontend translations

1. Helpers переводов.
2. WordForm.
3. WordDetail.
4. FlashCarousel и WordsTable.
5. Collocation forms.
6. Остальные views и локализация.

### Этап 7. Language modules

1. Registry и Spanish module.
2. English/French modules.
3. TTS.
4. Articles.
5. Dictionaries.
6. Feature flags Numbers/Abbreviations.

### Этап 8. Полная проверка

1. Затронутые backend tests.
2. Frontend unit tests.
3. `npm run lint -- --no-fix`.
4. Полный `mvn -B test --no-transfer-progress` с JDK 21.
5. `mvn -B install --no-transfer-progress`.
6. Ручной сценарий приёмки на копии рабочей базы.

## 15. Ручной сценарий приёмки

1. Открыть приложение после миграции: активен испанский, все старые слова и списки на месте.
2. Число слов, статистика и повторения совпадают с состоянием до миграции.
3. Создать курс English + Russian через верхнюю панель.
4. После создания главная пустая, счётчики равны нулю, испанские слова не видны.
5. Добавить `table` с переводом RU «стол».
6. Вернуться в испанский и добавить там же написание `table`, если его раньше не было: конфликта нет.
7. Убедиться, что поиск в каждом курсе находит только своё слово.
8. Создать список с одинаковым именем в обоих курсах: содержимое раздельно.
9. Добавить слово «позже» в English: оно не появляется в Spanish.
10. Пройти карточку English: озвучка английская, вопрос на русском, SM-2 меняется только у English.
11. Проверить, что badge повторений и heatmap каждого курса различаются.
12. Создать French + Russian/English.
13. Добавить существительное и проверить LE/LA, французскую озвучку и оба перевода.
14. Проверить, что Numbers доступны только там, где модуль объявляет поддержку.
15. Перезагрузить страницу: последний выбранный курс сохраняется.
16. Сменить язык интерфейса: активный курс и данные не меняются.
17. Открыть две вкладки и переключать курсы: сервер не имеет глобального active course; каждая вкладка после собственного выбора запрашивает явный courseId.

## 16. Definition of Done

Функциональность считается готовой только если:

- все правила из раздела 3 выполняются;
- ни одного глобального запроса слов/списков/SM-2 не осталось;
- в Java/TypeScript DTO нет russianVariant/englishVariant и translationRussian/translationEnglish;
- одинаковые слова в разных курсах не конфликтуют;
- smart lists не смешиваются;
- старая база мигрирована без потери ID, дат, переводов и повторений;
- English и French можно использовать для основного словаря и карточек;
- UI locale не связан с active course;
- все автоматические тесты и lint зелёные;
- ручной сценарий выполнен на копии production SQLite;
- сгенерированный `backend/src/main/resources/public` не редактировался вручную;
- чужие незакоммиченные файлы не изменены.

## 17. Что сознательно отложить

После стабильного MVP отдельными задачами:

1. Course-specific URL вида `/courses/:courseId/...`.
2. Удаление/архивирование курсов.
3. Больше двух языков перевода в форме создания курса.
4. Aggregate dashboard по всем курсам.
5. Смешанная учебная сессия.
6. Отдельный SM-2 по направлениям карточек.
7. Английские и французские числительные.
8. Переводы контекстов на несколько языков.
9. Ежедневные цели и лимиты новых слов.
10. Экспорт/импорт курса.
