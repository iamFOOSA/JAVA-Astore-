# Что реализовано по заданию

## 1. Асинхронная бизнес-операция через `@Async` / `CompletableFuture`

- Добавлен отдельный асинхронный сервис `ProductAsyncService`.
- Настроен пул потоков `productTaskExecutor` в `AsyncConfig`.
- Эндпоинт `POST /api/products/async/report` возвращает `taskId` и исходный статус `CREATED`.
- Эндпоинт `GET /api/products/async/report/{taskId}/status` возвращает текущий статус задачи:
  - `CREATED`
  - `IN_PROGRESS`
  - `COMPLETED`
  - `FAILED`
- После завершения задача возвращает итог `reportSummary`:
  - общее число товаров
  - число категорий
  - общее количество единиц на складе
  - суммарную стоимость остатков
- Эндпоинт `GET /api/products/async/stats` показывает агрегированную статистику по задачам.

## 2. Потокобезопасный счётчик

- Для подсчёта просмотров товаров используется `AtomicInteger productViewCounter`.
- Счётчик увеличивается в `ProductService.findById(...)`.
- Для демонстрации альтернативы добавлен `SynchronizedCounter` внутри сервиса продукта.

## 3. Демонстрация race condition и решение

- Эндпоинт `GET /api/products/race-condition?ops=2000` запускает 50 потоков.
- Внутри сравниваются три подхода:
  - небезопасный `UnsafeCounter`
  - потокобезопасный `SynchronizedCounter`
  - потокобезопасный `AtomicInteger`
- Возвращается JSON с ожидаемым и фактическими значениями.

Пример ответа:

```json
{
  "threads": 50,
  "operationsPerThread": 2000,
  "expectedTotal": 100000,
  "unsafeCounter": 51298,
  "synchronizedCounter": 100000,
  "atomicCounter": 100000,
  "raceConditionDetected": true
}
```

## 4. Нагрузочное тестирование JMeter

- JMeter-сценарий: `jmeter/async-task-status-load-test.jmx`
- Профиль для локального запуска без PostgreSQL: `src/main/resources/application-loadtest.properties`
- Результаты прогона:
  - дата: `2026-04-26`
  - потоков: `40`
  - циклов на поток: `8`
  - ramp-up: `5` секунд
  - задержка перед проверкой статуса: `250` мс
  - всего HTTP-запросов: `640`
  - ошибок: `0`
  - общее время прогона: `8663` мс
  - среднее время ответа: `1.63` мс
  - `p95`: `3` мс
  - максимум: `40` мс

### Детализация по запросам

| Запрос | Количество | Среднее, мс | Min, мс | Max, мс | p95, мс | Ошибки |
|---|---:|---:|---:|---:|---:|---:|
| `Start Async Report` | 320 | 1.73 | 0 | 40 | 3 | 0 |
| `Get Async Status` | 320 | 1.53 | 0 | 10 | 3 | 0 |

### Файлы результатов

- CSV: `jmeter/results/async-task-status-load-test.csv`
- Лог JMeter: `jmeter/results/jmeter.log`

## Дополнительно исправлено

- Добавлен `EntityGraph` для `findAll(Pageable)`, чтобы избежать проблем с ленивой загрузкой категорий при маппинге в DTO.
- Кэш поиска продуктов переведён на `computeIfAbsent`, чтобы убрать лишние гонки при одновременных запросах.
- В `saveWithTransaction(...)` и `saveWithoutTransaction(...)` добавлена привязка категорий, чтобы логика сохранения была согласованной с `create(...)`.
- Для `bulkImportWithoutTransaction(...)` добавлена корректная инвалидация кэша даже при частично успешном импорте и последующей ошибке.
- Для тестов добавлен H2-конфиг и настройка Mockito/JaCoCo, чтобы `mvn test` стабильно выполнялся локально.
