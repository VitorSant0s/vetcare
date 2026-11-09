.PHONY: up down logs api web test test-back test-front build

up:
	docker compose up -d

down:
	docker compose down

logs:
	docker compose logs -f

api:
	cd backend && ./mvnw spring-boot:run

web:
	cd frontend && npm run dev

test: test-back test-front

test-back:
	cd backend && ./mvnw test

test-front:
	cd frontend && npm test

build:
	cd backend && ./mvnw -DskipTests package
	cd frontend && npm run build
