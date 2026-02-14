# URL Shortener

## Logs en contenedor
La aplicación escribe logs en `/var/log/urlshortener/urlshortener.log` usando Logback/SLF4J.

## Ejecutar con Docker y volumen para logs
1. Construir imagen:
   docker build -t urlshortener .

2. Ejecutar montando logs en host:
   docker run --rm \
     -p 7070:7070 \
     -e MONGODB_URL="<tu_mongodb_url>" \
     -e PORT=7070 \
     -v $(pwd)/logs:/var/log/urlshortener \
     urlshortener

3. Verificar logs en host:
   tail -f ./logs/urlshortener.log

## Eventos registrados en archivo
- Inicio de servidor (REST y gRPC).
- Errores no controlados en requests.
- Requests y respuestas relevantes (`REQ`/`RES` con método, ruta y estado HTTP).
