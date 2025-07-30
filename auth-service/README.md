# auth-service

Spring Boot сервис аутентификации и авторизации с использованием JWT, ротации секретов, blacklisting токенов и разграничением доступа по ролям.

---

## Описание

`auth-service` реализует полный цикл управления пользователями и их сессиями:
- Регистрация и вход пользователей с безопасными паролями (bcrypt)
- Выдача access и refresh токенов (JWT), безопасное хранение refresh токена в БД
- Ротация секретов (kid), blacklist/отзыв токенов по jti
- Разграничение доступа к REST-эндпоинтам по ролям (ADMIN, PREMIUM_USER, GUEST)
- Интеграционные и unit тесты на все сценарии

---

## Основные эндпоинты

- `POST /auth/signup` — регистрация нового пользователя
- `POST /auth/signin` — вход (выдача access/refresh токенов)
- `POST /auth/refresh` — обновление access токена по refresh токену
- `POST /auth/logout` — отзыв токенов и удаление cookies
- `POST /api/admin/update-roles` — изменение ролей пользователя (только для ADMIN)
- `GET /api/admin/hello`, `/api/premium/hello`, `/api/guest/hello` — примеры защищённых эндпоинтов

---

## Тестирование

```bash
./mvnw test
```
Покрыты сценарии регистрации, входа, refresh/logout, проверки ролей, ошибок авторизации, unit-тесты сервисов

---

## Безопасность

- Токены только HttpOnly/Secure cookies (SameSite=Strict)
- Blacklist реализован по jti токена (refresh и access)
- Защита всех эндпоинтов через Spring Security
