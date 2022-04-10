# Gradebook API 

## Az alkalmazás célja

A *Gradebook API* egy Spring Boot segítségével létrehozott háromrétegű alkalmazás. Az alkalmazás a következő feladatokat
látja el:
- hitelesíti a felhasználót
- a felhasználó jogosultságától függően lekérdezések küldését teszi lehetővé az adatbázis felé
- ellenőrzi a beérkező adatokat az adatbázisban való tárolás előtt

## Az adatbázis felépítése

Az adatbázis felépítése a következő:

<img src="https://github.com/gyoritamas/vasvari-gradebook-api/blob/master/docs/images/db-schema.png" alt="schema"></a>

A *student* és *teacher* táblák a tanulók ill. tanárok személyes adatait tárolják.

A *subject* tábla tantárgy nevét és a tantárgyat tanító tanár azonosítóját tartalmazza.

Az *assignment* tábla tárolja a tanár által kiosztott feladatokat és osztályozásra kerülő tevékenységeket (pl. házi feladat, dolgozat, projektmunka). A feladat neve és típusa mellett opcionálisan megadható egy rövid leírás. 

A *gradebook_entry* táblában kerülnek tárolásra a naplóbejegyzések. Egy naplóbejegyzés áll
- egy tanulói azonosítóból
- egy tantárgy azonosítóból
- egy feladat azonosítóból
- és egy érdemjegyből

A *user* tábla tartalmazza a felhasználók bejelentkezési adatait, szerepkörüket (ADMIN, TEACHER vagy STUDENT) és egy boolean értéket ami meghatározza, hogy aktív-e a felhasználói fiók.

A *school_actor_application_user_relation* tábla kapcsolja össze a felhasználókat az iskolai szereplőkkel (TEACHER vagy STUDENT).

## Az alkalmazás futtatása

A futtatáshoz szükség van maven-re, a telepítés lépései [itt](https://maven.apache.org/install.html) találhatók.

### Futtatás XAMPP-pal

Indítsuk el a XAMPP-ot, majd [phpMyAdmin](http://localhost/phpmyadmin/) felületen hozzuk létre az adatbázist a *create-database.sql* állomány importálásával.
A gradebook-api project gyökérkönyvtárából indított parancssorból adjuk ki a 
```
mvn exec:java -D exec.mainClass=com.codecool.gradebookapi.GradebookApiApplication
```
parancsot vagy futtassuk a *gradebook-api.bat* állományt.

### Futtatás Dockerrel
Indítsuk el a Dockert, majd adjuk ki a project gyökérkönyvtárából a
```
docker-compose -f docker-compose.dev.yml up --build
```
parancsot, vagy használjuk a *gradebook-api-docker.bat* állományt.

## Dokumentáció
A dokumentáció a
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
címen érhető el.


