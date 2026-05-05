# Astore (Spring Boot + React SPA)

Система управления интернет-магазином, построенная на базе Spring Framework. Проект включает REST API и SPA-клиент для управления товарами, категориями, пользователями, заказами и позициями заказа.

## Технологический стек

- **Runtime:** Java 21
- **Backend:** Spring Boot, Spring MVC, Spring Data JPA
- **Frontend:** React + Vite
- **Build Tool:** Maven, Node.js package manager

## Функциональные возможности

- ✅ CRUD для товаров, категорий, пользователей, заказов и позиций заказа
- ✅ Фильтрация данных на клиенте
- ✅ Просмотр всех товаров или товаров конкретной категории через быстрые кнопки и `/api/products/category/{categoryId}`
- ✅ Серверная фильтрация товаров по пользователю и категории через `/api/products/search`
- ✅ Сохранённая корзина для каждого покупателя через `/api/carts/user/{userId}`
- ✅ Отображение OneToMany: пользователь → заказы → позиции заказа
- ✅ Отображение ManyToMany: товары ↔ категории

## Запуск backend

Перед запуском укажите пароль от PostgreSQL в переменной окружения `DB_PASSWORD`.

```bash
mvn spring-boot:run
```

Backend API будет доступен на `http://localhost:8080`.

## Очистка и красивое demo-заполнение базы

Команда ниже удаляет старые записи из таблиц магазина и создаёт аккуратный демо-каталог: 26 товаров с SVG-изображениями, 6 категорий маркетплейса, 5 покупателей, 5 заказов и несколько сохранённых корзин.

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--astore.demo.seed=true"
```

Флаг `astore.demo.seed=true` нужен только для пересоздания демо-данных. Для обычного запуска используйте команду без этого флага.

## Запуск SPA-клиента

```bash
cd frontend
npm install
npm run dev
```

React-приложение будет доступно на `http://localhost:5173`. Vite проксирует запросы `/api` на `http://localhost:8080`, поэтому CORS-настройки для локальной разработки не нужны.

Если используется другой адрес backend, можно задать переменную:

```bash
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

## Структура SPA

- `frontend/src/api.js` — единый слой работы с REST API.
- `frontend/src/App.jsx` — экраны CRUD, фильтрации и визуализации связей.
- `frontend/src/styles.css` — адаптивный интерфейс приложения.
- `frontend/public/product-images/` — локальные SVG-изображения для товаров.

## Переменные окружения

Для локального запуска через Docker Compose скопируйте пример:

```bash
cp .env.example .env
```

Минимально нужно заменить `POSTGRES_PASSWORD` и `DB_PASSWORD` на одинаковый свой пароль. Основные переменные:

- `FRONTEND_PORT` — порт SPA на локальной машине, по умолчанию `5173`.
- `BACKEND_PORT` — порт backend API на локальной машине, по умолчанию `8080`.
- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` — настройки контейнера PostgreSQL.
- `DB_NAME`, `DB_USER`, `DB_PASSWORD` — имя БД, пользователь и пароль PostgreSQL.
- `SPRING_JPA_HIBERNATE_DDL_AUTO` — режим схемы Hibernate, для учебного запуска подходит `update`.
- `ASTORE_DEMO_SEED` — `true`, если нужно пересоздать демо-данные при старте.
- `BACKEND_SCHEME`, `BACKEND_HOSTPORT` — адрес backend для Nginx-прокси frontend-контейнера.

Реальный `.env` не хранится в Git. В Dockerfile-ах нет `ENV`, а в `docker-compose.yml` нет блока `environment`: контейнеры получают значения только из локального `.env` через `env_file`.

## Запуск через Docker Compose

Docker Compose поднимает PostgreSQL, backend и frontend одним стеком:

```bash
docker compose up --build
```

После запуска:

- SPA через Nginx: `http://localhost:5173`
- API backend: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- healthcheck: `http://localhost:8080/actuator/health`

Остановить стек можно командой:

```bash
docker compose down
```

Если нужно удалить и данные PostgreSQL:

```bash
docker compose down -v
```

## Dockerfile

Backend и frontend разделены:

- `Dockerfile` — backend-only образ: Maven собирает Spring Boot jar, runtime запускается на Java 21 JRE.
- `frontend/Dockerfile` — frontend-only образ: Vite собирает SPA, Nginx отдаёт статику и проксирует `/api` на backend.

Локальная сборка образов:

```bash
docker build -t astore-backend .
docker build -t astore-frontend ./frontend
```

## Развертывание на Render

В проект добавлен `render.yaml` для Render Blueprint: отдельно создаются `astore-backend`, `astore-frontend` и Render PostgreSQL. Значения окружения не хранятся в репозитории, поэтому их нужно задать вручную в Render Dashboard.

Что нужно сделать один раз:

1. Загрузить проект на GitHub.
2. Зарегистрироваться или войти в Render.
3. Создать новый Blueprint и выбрать этот GitHub-репозиторий.
4. Проверить, что Render видит `render.yaml`, затем создать сервисы.
5. В `astore-backend` добавить переменные окружения в Render Dashboard:
   - `DATABASE_URL` — internal connection string созданной Render PostgreSQL.
   - `SPRING_JPA_HIBERNATE_DDL_AUTO=update`.
   - `SPRING_JPA_SHOW_SQL=false`.
   - `SPRING_JPA_FORMAT_SQL=false`.
   - `ASTORE_DEMO_SEED=false`.
6. В `astore-frontend` добавить переменные окружения:
   - `BACKEND_SCHEME=https`.
   - `BACKEND_HOSTPORT=<backend-service>.onrender.com`.
7. В настройках `astore-backend` и `astore-frontend` открыть Deploy Hook и скопировать оба URL.
8. В GitHub открыть `Settings -> Secrets and variables -> Actions`.
9. Добавить секрет `RENDER_BACKEND_DEPLOY_HOOK_URL` со значением backend Deploy Hook.
10. Добавить секрет `RENDER_FRONTEND_DEPLOY_HOOK_URL` со значением frontend Deploy Hook.
11. Добавить секрет `BACKEND_HEALTH_URL=https://<backend-service>.onrender.com/actuator/health`.
12. Добавить секрет `FRONTEND_HEALTH_URL=https://<frontend-service>.onrender.com/health`.
13. Сделать push в ветку `main` или запустить workflow вручную через `Actions -> CI/CD -> Run workflow`.

В `render.yaml` указан `autoDeployTrigger: off`, поэтому деплой запускается из GitHub Actions только после успешных сборок и тестов.

Важно: бесплатный тариф Render удобен для учебного задания, но имеет ограничения. Free web service может запускаться с задержкой после простоя, а free PostgreSQL на Render рассчитан на ограниченный период.

## CI/CD

Workflow находится в `.github/workflows/ci-cd.yml` и выполняет:

1. Backend: `mvn -B test` и `mvn -B package -DskipTests`.
2. Frontend: `npm ci` и `npm run build`.
3. Docker: сборка backend-образа из `Dockerfile` и frontend-образа из `frontend/Dockerfile`.
4. Deploy: вызов двух Render Deploy Hook после push в `main` или ручного запуска workflow на ветке `main`.
5. Healthcheck: проверка backend `/actuator/health` и frontend `/health`.

Для pull request выполняются сборка, тесты и Docker build. Деплой и production healthcheck запускаются для push в `main` и ручного запуска workflow на ветке `main`.


## Качество кода (Checkstyle)

Проект проходит статический анализ кода с использованием **Checkstyle** для обеспечения высоких стандартов разработки.


##  Ссылка на Sonar:
https://sonarcloud.io/project/issues?impactSeverities=HIGH&issueStatuses=OPEN%2CCONFIRMED&id=iamFOOSA_PNAY&open=AZlejTQlS2hnDgcu4_37
