# AGENTS.md

## 1. Суть проекта

LinguaLeap — однопользовательское веб-приложение для ведения словаря иностранных слов и регулярного повторения.
Пользователь добавляет слова и фразы, для каждого значения хранит переводы на выбранные языки, грамматические
признаки, определение, изображение, контексты и коллокации. Слова можно объединять в обычные и автоматически обновляемые
списки, смотреть статистику пополнения словаря и проходить карточки с оценкой знания.

Приложение мультиязычно по курсам: язык изучения заводится как курс (целевой язык + упорядоченный список языков
перевода). Все слова, списки и повторения скоуплены по курсу; переводы значений и коллокаций хранятся динамически
(`language_code → text`), а не в захардкоженных колонках русского/английского. Активный курс выбирается в UI
и передаётся явным `courseId` во все запросы.

Главная учебная механика — интервальное повторение по модифицированному алгоритму SM-2: после каждой карточки
пользователь ставит оценку от 0 до 5, а приложение вычисляет дату следующего повторения. Отдельно есть режимы изучения
чисел и аббревиатур, таблица слов и прогноз нагрузки повторений по датам.

- Maven-реактор: `frontend` → `backend`.
- Vue собирается в `frontend/target/dist`, затем копируется в Spring Boot.
- Spring Boot отдаёт REST API `/api/*`, статику SPA и fallback на `index.html`.
- Backend: controllers → services (`interface` + `impl`) → Spring Data repositories → JPA.
- DTO передаются наружу; маппинг — вручную через `mapping/Mappings.java`.
- Основная БД — SQLite `${DB_PATH}`, `hibernate.ddl-auto=none` (схема принадлежит миграциям); тесты — H2 (`create-drop`).
- Схема БД версионируется собственным `DatabaseMigrationRunner` (SQL из `db/migration/`, таблица `schema_migrations`).
- SM-2 ведётся отдельно для каждого `WordMeaning`, оценки: `0..5`.
- Аутентификация: form login + HTTP Basic; профиль `local` отключает защиту.

## 2. Карта директорий

| Путь                                           | Назначение                                                                                                                 |
|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `pom.xml`                                      | Корневой Maven-реактор, Java 21, JaCoCo                                                                                    |
| `backend/src/main/java/es/e1sordo/lingualeap/` | Backend по слоям: `controllers`, `services`, `repositories`, `models`, `dto`, `configuration`, `mapping`, `utils`, `enums` |
| `backend/src/main/resources/db/migration/`     | SQL-миграции схемы (`V1__legacy_baseline.sql`, `V2__multilingual_courses.sql`, `V3__course_created_at_epoch.sql`) для `DatabaseMigrationRunner` |
| `backend/src/main/resources/application.yml`   | Порт 8098, SQLite, JPA, Security; параметры из env                                                                         |
| `backend/src/test/`                            | JUnit 5, Spring Boot Test, MockMvc; профиль `test`, H2                                                                     |
| `frontend/src/api/`                            | Axios-клиент и TypeScript-контракты backend API; курсовые методы принимают `courseId` первым параметром                    |
| `frontend/src/composables/`                    | `useCourses` — модульное reactive-состояние курсов и активный курс                                                         |
| `frontend/src/languages/`                      | Языковые модули (`types`, `index` — реестр, `spanish`, `english`, `french`, `fallback`): артикли, словари, feature flags   |
| `frontend/src/components/`                     | Переиспользуемые Vue-компоненты; `CourseSwitcher`, `CourseCreateModal` — управление курсами                                |
| `frontend/src/views/`                          | Страницы; учебные режимы — в `views/study/`                                                                                |
| `frontend/src/router/`                         | Маршруты SPA                                                                                                               |
| `frontend/src/locales/`                        | Локализации `en`, `ru`, `es`                                                                                               |
| `frontend/src/utils/`                          | Чистые вспомогательные функции; `translations.ts` — работа с динамическими переводами                                      |
| `frontend/tests/unit/`                         | Jest-тесты `*.spec.ts`                                                                                                     |
| `frontend/public/`                             | Исходные публичные статические файлы                                                                                       |
| `.github/workflows/`                           | PR-сборка и deploy из `main`                                                                                               |
| `.kimi-code/mcp.json`                          | Проектное MCP-подключение к IDEA                                                                                           |
| `backups/`, `*.db`                             | Локальные данные и резервные копии; не изменять без явного запроса                                                         |

Не сканировать и не редактировать как исходники:

- `.git/`, `.idea/`, `.DS_Store`., `backups/`
- `backend/target/`, `frontend/target/`, `frontend/node/`, `frontend/node_modules/`.
- `backend/src/main/resources/public/` — генерируется из frontend-сборки.
- `coverage/`, `test-output/`, отчёты и артефакты тестов.

## 3. Стек и стандарты

- Java 21, Spring Boot 3.2, Spring MVC/Data JPA/Security/Cache/Actuator.
- Lombok, Hibernate, SQLite JDBC; H2 только для тестов.
- Миграции схемы — собственный `DatabaseMigrationRunner` (Flyway официально не поддерживает SQLite).
- JUnit 5, MockMvc, Rest Assured, JaCoCo.
- Vue 3, TypeScript strict, Vue CLI 5, Composition API, `<script setup lang="ts">`.
- Vue Router, Vue I18n, Axios, Bootstrap 5, Sass.
- Jest/Vue Test Utils; Cypress подключён, но e2e-спецификаций сейчас нет.
- Node 20.3.1 устанавливается Maven-плагином; основной lock — `package-lock.json`.
- Docker: единый образ с executable Spring Boot JAR.

Стиль:

- Java: пакет `es.e1sordo.lingualeap`, 4 пробела, классы `PascalCase`, члены `camelCase`.
- DTO — суффикс `Dto`; сервисы — интерфейс и `impl/*ServiceImpl`.
- DI — через конструктор, обычно Lombok `@RequiredArgsConstructor`.
- Vue: файлы `PascalCase.vue`, страницы `*View.vue`, тесты `*.spec.ts`.
- Импорты внутри frontend — через alias `@/`; отступ 4 пробела, точки с запятой.
- HTTP-вызовы централизовать в `frontend/src/api/backend-api.ts`.
- Нет Checkstyle/Prettier: соблюдать стиль соседнего кода, не делать массовых реформатов.
- `npm run lint` может исправлять файлы; для read-only проверки использовать `--no-fix`.

## 4. Команды

Требуется JDK именно 21; на JDK 27 текущий Lombok ломает компиляцию.
`./mvnw` неполон (`.mvn/wrapper` отсутствует) — использовать системный `mvn`.

### Полная проверка из корня

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)  # macOS
mvn -B test --no-transfer-progress
mvn -B install --no-transfer-progress           # команда CI
```

### Backend

```bash
mvn -pl backend test
mvn -pl backend -Dtest=ForeignWordsControllerTest test

DB_PATH=/absolute/path/data.db USER_NAME=user USER_SECRET=secret \
  mvn -pl backend spring-boot:run -Dspring-boot.run.profiles=local
```

Backend слушает `http://localhost:8098`.

### Frontend

```bash
cd frontend
npm ci --legacy-peer-deps
npm run serve
npm run test:unit -- --runInBand
npm run lint -- --no-fix
npm run build
```

Dev-server проксирует `/api` на `localhost:8098`.

## 5. Правила работы

- Перед изменениями проверять `git status`; чужие незакоммиченные правки сохранять.
- Не редактировать сгенерированный `backend/src/main/resources/public`.
- Меняя API, синхронно обновлять Java DTO и `frontend/src/api/backend-api.ts`.
- `LearningStatus` хранится через `EnumType.ORDINAL`: порядок enum менять опасно.
- Все API слов, списков и повторений — под `/api/courses/{courseId}/...`; сервисы принимают `courseId` и
  проверяют принадлежность сущностей курсу. Глобальных запросов слов/списков/SM-2 не осталось.
- Переводы значений и коллокаций — только через `Map<String, String> translations` по кодам языков курса;
  primary-перевод — язык с индексом 0 в `translationLanguageCodes`. В DTO не возвращать
  `russianVariant`/`englishVariant`.
- Схема БД версионируется `DatabaseMigrationRunner`: SQL-файлы в `backend/src/main/resources/db/migration/`
  (`V{n}__*.sql`), применённые версии — в таблице `schema_migrations`. Раннер вызывается из `main()` до старта
  Spring; если таблица `words` уже существует, V1 помечается применённой без выполнения (baseline).
- Старые колонки `russian_variant`/`english_variant` в `meanings`/`meaning_collocations` не удалять и не маппить
  в JPA — это исторические данные.
- Менять схему — только новой миграцией, не полагаться на `ddl-auto` (main = `none`, тесты H2 = `create-drop`).
  Не экспериментировать на боевых SQLite-файлах; проверку миграции гонять на копии из `backups/`.
- SQLite и конкурентность: WAL запрещён (персистентен в файле БД и даёт `SQLITE_BUSY_SNAPSHOT` при
  read-then-write транзакциях); раннер миграций принудительно держит `journal_mode=DELETE`, пул Hikari —
  `busy_timeout=5000` на каждом соединении. Генерация ID — только `GenerationType.IDENTITY` (колонки
  `INTEGER PRIMARY KEY AUTOINCREMENT`); табличные генераторы Hibernate (`*_seq`, isolated work на втором
  соединении пула) с миграцией V5 удалены и не вводить заново. `ddl-auto=validate` не использовать:
  rowid-алиас `INTEGER PRIMARY KEY` диалект SQLite несовместим с проверкой типа BIGINT для `Long`.
- Даты в SQLite хранятся в формате epoch-millis (integer) — так их пишет и читает SQLite JDBC стек. Миграции не
  должны записывать даты текстом (`CURRENT_DATE`, `'YYYY-MM-DD'`): чтение такого значения падает
  с `ParseException` (драйвер ждёт `yyyy-MM-dd HH:mm:ss.SSS` либо epoch-millis).
- Frontend: активный курс — только через `useCourses` (localStorage `lingualeap:activeCourseId`); API-клиент
  не читает курс неявно. При смене курса страницы пересоздаются ключом router-view `${activeCourseId}:${$route.fullPath}`.
- TTS, артикли, внешние словари и feature flags (числа/аббревиатуры) — только через языковые модули
  `frontend/src/languages/`; захардкоженный испанский (EL/LA, es-голоса) не вводить.
- Тесты запускать с профилем `test`/H2, не подключая их к рабочей БД.
- Секреты передавать через `DB_PATH`, `USER_NAME`, `USER_SECRET`; `.env` не коммитить.
- После изменений прогонять затронутые тесты и lint; перед завершением — полный `mvn test`.
- Не публиковать Docker-образ и не запускать deploy без явного запроса.
- IDEA MCP настроен через `.kimi-code/mcp.json`; после его изменения нужен новый клиентский сеанс.

### Журнал архитектурных решений

Добавлять записи в формате:
`YYYY-MM-DD — решение; причина; ограничения/последствия.`

- 2026-11-12 — мультиязычная архитектура курсов: `Language`, `LanguageCourse`, `CourseTranslationLanguage`,
  динамические `WordMeaningTranslation`/`WordMeaningCollocationTranslation`, course-scoped API, smart-списки
  (`RECENTLY_ADDED`, `PROBLEM_WORDS`) по курсам; причина — выход за пределы одного испанского словаря;
  ограничения — в UI создания курса максимум 2 языка перевода, удаление курсов не реализовано.
- 2026-11-12 — собственный `DatabaseMigrationRunner` вместо Flyway; причина — Flyway официально не поддерживает
  SQLite; ограничения — ручной baseline V1 по существованию таблицы `words`, миграции выполняются из `main()`
  до поднятия контекста Spring.
- 2026-11-12 — сохранение legacy-колонок `russian_variant`/`english_variant` в БД; причина — запрет утраты
  исторических данных при миграции V2; ограничения — колонки не маппены в JPA, актуальные данные только
  в таблицах переводов.
- 2026-11-12 — `hibernate.ddl-auto=validate` в main-профиле; причина — схема теперь принадлежит миграциям,
  Hibernate только проверяет; ограничения — любая смена сущностей требует новой миграции. (Отменено той же
  датой: заменено на `none` в связке с миграцией V5 — см. запись про IDENTITY.)
- 2026-11-12 — модульное reactive-состояние курсов (`useCourses`) без Pinia; причина — минимальный MVP по плану;
  ограничения — счётчики курсов обновляются через `refreshCourses`/`updateActiveCourseCounts`, состояние живёт
  в памяти вкладки.
- 2026-11-12 — языковые модули frontend (реестр `es`/`en`/`fr` + fallback) с артиклями, словарями и feature flags;
  причина — убрать захардкоженный испанский из карточек, TTS и ссылок; ограничения — числительные реализованы
  только для испанского, генераторы en/fr — отдельной задачей.
- 2026-11-12 — миграция V3: нормализация `courses.created_at` из текстовой даты в epoch-millis; причина — V2
  писала `CURRENT_DATE` текстом, а SQLite JDBC стек читает DATE через `getTimestamp` и падает с `ParseException`;
  ограничения — день создания уже созданного курса трактуется как UTC-полночь (смещение часов не значимо).
- 2026-11-12 — `busy_timeout=5000` на каждом соединении пула Hikari (`connection-init-sql`) и принудительный
  `PRAGMA journal_mode=DELETE` в раннере миграций; причина — SQLite по умолчанию падает с `SQLITE_BUSY` при любом
  конфликте блокировок, а WAL, наоборот, персистентен в файле БД и ломает read-then-write транзакции
  (`SQLITE_BUSY_SNAPSHOT`); ограничения — журнал rollback держит эксклюзивную блокировку на запись, писатель
  ждёт читателей до 5 с.
- 2026-11-12 — миграция V4: пересинхронизация всех `*_seq` со значением `MAX(id) + 50`; причина — V2 засидила
  `course_translation_languages_seq=51`, а `PooledOptimizer` раздаёт первые ID из диапазона `[next_val-49, next_val]`,
  что немедленно конфликтовало с существующими строками (UNIQUE constraint failed); ограничения — после V5
  таблицы `*_seq` удалены, запись сохранена как этап истории.
- 2026-11-12 — миграция V5 + `GenerationType.IDENTITY` для всех сущностей; причина — табличные генераторы
  Hibernate выполняли «isolated work» (независимый коммит `*_seq` на втором соединении пула посреди транзакции
  приложения), что под SQLite не работает: в rollback-журнале коммит генератора голодал (`SQLITE_BUSY`), в WAL —
  падала основная транзакция (`SQLITE_BUSY_SNAPSHOT`); ограничения — колонки `id` пересозданы как
  `INTEGER PRIMARY KEY AUTOINCREMENT`, таблицы `*_seq` удалены, `ddl-auto` переведён с `validate` на `none`
  (rowid-алиас несовместим с проверкой типа BIGINT для `Long`).
