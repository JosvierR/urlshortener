# URL Shortener

## Logs en contenedor
La aplicación escribe logs en `/var/log/urlshortener/urlshortener.log` usando Logback/SLF4J.

## Docker Hub

### Publicar imagen en Docker Hub
1. Construir imagen con nombre, usuario y tag:
   ```bash
   docker build -t <usuario>/<imagen>:<tag> .
   ```
2. Iniciar sesión en Docker Hub:
   ```bash
   docker login
   ```
3. Publicar la imagen:
   ```bash
   docker push <usuario>/<imagen>:<tag>
   ```

### Convención de versionado de tags
- `latest`: última versión estable publicada.
- `v1.0.0`: versión semántica fija para producción.
- `v1.0.1`, `v1.1.0`, `v2.0.0`: parches, nuevas funcionalidades y cambios mayores respectivamente.

## Ejecutar con Docker (variables obligatorias + volumen de logs)
Variables de entorno obligatorias:
- `MONGODB_URL`
- `PORT`

Ejemplo de ejecución:
```bash
docker run --rm \
  -p 7070:7070 \
  -e MONGODB_URL="mongodb://host.docker.internal:27017/urlshortener" \
  -e PORT=7070 \
  -v $(pwd)/logs:/var/log/urlshortener \
  <usuario>/<imagen>:<tag>
```

Verificar logs en host:
```bash
tail -f ./logs/urlshortener.log
```

## Eventos registrados en archivo
- Inicio de servidor (REST y gRPC).
- Errores no controlados en requests.
- Requests y respuestas relevantes (`REQ`/`RES` con método, ruta y estado HTTP).
