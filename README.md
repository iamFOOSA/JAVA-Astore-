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


## Качество кода (Checkstyle)

Проект проходит статический анализ кода с использованием **Checkstyle** для обеспечения высоких стандартов разработки.


##  Ссылка на Sonar:
https://sonarcloud.io/project/issues?impactSeverities=HIGH&issueStatuses=OPEN%2CCONFIRMED&id=iamFOOSA_PNAY&open=AZlejTQlS2hnDgcu4_37
