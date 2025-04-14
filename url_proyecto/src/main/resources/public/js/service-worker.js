const CACHE_NAME = 'url-shortener-cache-v1';
const urlsToCache = [
    '/',
    '/index.html',
    '/css/style.css',
    '/js/script.js',
    '/summary.html',
    '/js/summary.js',
    '/js/qrcode.min.js'
];

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(urlsToCache))
    );
});

self.addEventListener('fetch', event => {
    // Si la solicitud es para la API de URLs, se aplica estrategia network first
    if (event.request.url.includes('/api/urls')) {
        event.respondWith(
            fetch(event.request)
                .then(response => {
                    // Clonar y guardar en cache
                    const responseClone = response.clone();
                    caches.open(CACHE_NAME).then(cache => {
                        cache.put(event.request, responseClone);
                    });
                    return response;
                })
                .catch(() => {
                    // Si falla la red, se devuelve la respuesta cacheada
                    return caches.match(event.request);
                })
        );
        return;
    }
    // Para el resto de las solicitudes se utiliza la estrategia cache first
    event.respondWith(
        caches.match(event.request)
            .then(response => response || fetch(event.request))
    );
});
