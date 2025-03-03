# Projekt

## Opis
Aplikacja oparta na Spring Boot, wykorzystująca PostgreSQL, OMDB API oraz Docker Compose. Projekt został napisany w Javie 23. 

## Technologie
- **Java 23**
- **Spring Boot**
- **PostgreSQL**
- **Docker Compose**
- **OMDB API**
- **Swagger UI**

## Dokumentacja Swagger UI
Dostępna pod adresem - `http://localhost:8080/swagger-ui/index.html#/`. Ten interface umożliwia przeglądanie i testowanie API.


## Wymagania
- Java 23
- Maven
- Docker i Docker Compose

## Konfiguracja
1. **Klonowanie repozytorium**
   ```sh
   git clone https://github.com/jakubkras/Project
   ```

2. **Ustawienie zmiennych środowiskowych**- Utwórz plik `application.properties` w katalogu `resources` projektu i dodaj następujące zmienne:
   ```sh
   spring.datasource.url=jdbc:postgresql://postgres-db:5432/omdb
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

   #Opcjonalnie
   spring.jpa.hibernate.ddl-auto=update

   omdb.api.key=44dc3657
   omdb.api.url=http://www.omdbapi.com/
   ```

## Uruchomienie aplikacji
### 1. Uruchomienie za pomocą Docker Compose
   ```sh
   docker-compose up --build
   ```
Aplikacja powinna być dostępna pod adresem `http://localhost:8080`

### 2. Uruchomienie lokalnie
   ```sh
   mvn clean install
   mvn spring-boot:run
   ```

## Struktura projektu
```
<root>
│── src/
│   ├── main/
│   │   ├── java/ 
│   │   │   ├──  resources/
│   │   │   ├──com/jakubkras/project
│── docker-compose.yml
│── Dockerfile
│── pom.xml
│── README.md
```

## API

**Endpointy frontend**

- `GET /movies/home/` – Strona główna
- `POST /movies/home/searchByTitle` – Szuka filmu po jego tytule
- `POST /movies/home/searchByQuery` – Szuka filmu po zapytaniu
- `POST /movies/home/searchByCategory` – Szuka film po kategorii i zapytaniu
- `POST /movies/home/createMovie` – Tworzy nowy film
- `POST /movies/home/deleteMovie` – Usuwa film po IMDB ID
- `POST /movies/home/updateMovie` – Aktualizuje filmy po IMDB ID
- `POST /movies/home/addRating` – Dodaje ocenę do filmu
- `POST /movies/home/enableMovie` – Przywraca usunięty film

**Endpointy backend**

- `GET /movies/search/{title}` – Szuka filmu po jego tytule
- `GET /movies/search` – Szuka filmu po zapytaniu
- `GET /movies/searchByCategory` – Szuka film po kategorii i zapytaniu
- `POST /movies/movie` – Tworzy nowy film
- `DELETE /movies` – Usuwa film po IMDB ID
- `PUT /movies` – Aktualizuje filmy po IMDB ID
- `POST /movies/rating` – Dodaje ocenę do filmu
- `PATCH movies/` – Przywraca usunięty film


## Baza danych
- **PostgreSQL** uruchamiany w kontenerze Dockera
- Domyślna konfiguracja znajduje się w `application.properties`


## Autor
Jakub Kras

