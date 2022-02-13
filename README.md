# Gradebook API 

## Az alkalmazás célja

A *Gradebook API* egy Spring Boot segítségével létrehozott háromrétegű alkalmazás. Célja, hogy napjlóbejegyzéset tárolhassunk 
egy adatbázisban, ill. HTTP-kéréseken keresztül hozzáférhessünk azokhoz. Az alkalmazást Docker kapcsolja össze egy 
MySQL-szerverrel. Swagger-rel készített dokumentáció biztosítja az elérhető végpontok ellenőrzését. 

## Az adatbázis felépítése

Az adatbázis felépítése a következő:

<img src="https://github.com/gyoritamas/vasvari-gradebook-api/blob/development/docs/images/db-schema.png" alt="schema"></a>

A *student* tábla tartalma:
- tanuló vezeték- és keresztneve,
- évfolyama,
- email- és lakcíme, telefonszáma
- születési dátuma

A *course* tábla a kurzus vagy tantárgy nevét tárolja.

Az *assignment* tábla tárolja a tanár által kiosztott feladatokat és osztályozásra kerülő tevékenységeket (pl. házi feladat, dolgozat, projektmunka). A feladat neve és típusa mellett opcionálisan megadható egy rövid leírás.

A *gradebook_entry* táblában kerülnek tárolásra a naplóbejegyzések. Egy naplóbejegyzés áll
- egy tanulói azonosítóból
- egy tantárgy azonosítóból
- egy feladat azonosítóból
- és egy érdemjegyből

## Az alkalmazás futtatása

Az alkalmazás futtatásához indítsuk el a Dockert, majd adjuk ki a
```
docker-compose -f docker-compose.dev.yml up --build
```
parancsot, vagy használjuk a gradebookapi_run.bat fájlt.

## Dokumentáció
A dokumentáció böngészőből érhető el a

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

címen.


