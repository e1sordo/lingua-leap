# AGENTS.md

## 1. Суть проекта

LinguaLeap — однопользовательское веб-приложение для ведения испанского словаря и регулярного повторения слов.
Пользователь добавляет слова и фразы, для каждого значения хранит переводы на русский и английский, грамматические
признаки, определение, изображение, контексты и коллокации. Слова можно объединять в обычные и автоматически обновляемые
списки, смотреть статистику пополнения словаря и проходить карточки с оценкой знания.

Главная учебная механика — интервальное повторение по модифицированному алгоритму SM-2: после каждой карточки
пользователь ставит оценку от 0 до 5, а приложение вычисляет дату следующего повторения. Отдельно есть режимы изучения
чисел и аббревиатур, таблица слов и прогноз нагрузки повторений по датам.

- Maven-реактор: `frontend` → `backend`.
- Vue собирается в `frontend/target/dist`, затем копируется в Spring Boot.
- Spring Boot отдаёт REST API `/api/*`, статику SPA и fallback на `index.html`.
- Backend: controllers → services (`interface` + `impl`) → Spring Data repositories → JPA.
- DTO передаются наружу; маппинг — вручную через `mapping/Mappings.java`.
- Основная БД — SQLite `${DB_PATH}`, `hibernate.ddl-auto=update`; тесты — H2.
- SM-2 ведётся отдельно для каждого `WordMeaning`, оценки: `0..5`.
- Аутентификация: form login + HTTP Basic; профиль `local` отключает защиту.

## 2. Карта директорий

| Путь                                           | Назначение                                                                                                                 |
|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `pom.xml`                                      | Корневой Maven-реактор, Java 21, JaCoCo                                                                                    |
| `backend/src/main/java/es/e1sordo/lingualeap/` | Backend по слоям: `controllers`, `services`, `repositories`, `models`, `dto`, `configuration`, `mapping`, `utils`, `enums` |
| `backend/src/main/resources/application.yml`   | Порт 8098, SQLite, JPA, Security; параметры из env                                                                         |
| `backend/src/test/`                            | JUnit 5, Spring Boot Test, MockMvc; профиль `test`, H2                                                                     |
| `frontend/src/api/`                            | Axios-клиент и TypeScript-контракты backend API                                                                            |
| `frontend/src/components/`                     | Переиспользуемые Vue-компоненты                                                                                            |
| `frontend/src/views/`                          | Страницы; учебные режимы — в `views/study/`                                                                                |
| `frontend/src/router/`                         | Маршруты SPA                                                                                                               |
| `frontend/src/locales/`                        | Локализации `en`, `ru`, `es`                                                                                               |
| `frontend/src/utils/`                          | Чистые вспомогательные функции                                                                                             |
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
- Миграций БД нет; схема обновляется Hibernate. Не экспериментировать на боевых SQLite-файлах.
- Тесты запускать с профилем `test`/H2, не подключая их к рабочей БД.
- Секреты передавать через `DB_PATH`, `USER_NAME`, `USER_SECRET`; `.env` не коммитить.
- После изменений прогонять затронутые тесты и lint; перед завершением — полный `mvn test`.
- Не публиковать Docker-образ и не запускать deploy без явного запроса.
- IDEA MCP настроен через `.kimi-code/mcp.json`; после его изменения нужен новый клиентский сеанс.

### Журнал архитектурных решений

Добавлять записи в формате:
`YYYY-MM-DD — решение; причина; ограничения/последствия.`
