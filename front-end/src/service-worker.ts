/// <reference lib="webworker" />

declare const self: ServiceWorkerGlobalScope;

const CACHE_NAME = 'nova-alianca-v1';
const URLS_TO_CACHE = ['/', '/index.html', '/assets/favicon.ico', '/styles.css', '/main.js'];

// Install event - cache resources
self.addEventListener('install', (event: ExtendableEvent) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(URLS_TO_CACHE).catch((err) => {
        console.warn('Failed to cache some resources:', err);
      });
    })
  );
  self.skipWaiting();
});

// Activate event - clean old caches
self.addEventListener('activate', (event: ExtendableEvent) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.filter((cacheName) => cacheName !== CACHE_NAME).map((cacheName) => caches.delete(cacheName))
      );
    })
  );
  self.clients.claim();
});

// Fetch event - network-first strategy for APIs, cache-first for assets
self.addEventListener('fetch', (event: FetchEvent) => {
  const url = new URL(event.request.url);

  // Skip cross-origin requests
  if (url.origin !== self.location.origin) {
    return;
  }

  // Network-first for API calls
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(
      fetch(event.request)
        .then((response) => {
          if (response.ok) {
            const clone = response.clone();
            caches.open(CACHE_NAME).then((cache) => {
              cache.put(event.request, clone);
            });
          }
          return response;
        })
        .catch(() => {
          return caches.match(event.request).then((response) => response || createOfflineResponse());
        })
    );
  } else {
    // Cache-first for assets
    event.respondWith(
      caches
        .match(event.request)
        .then((response) => {
          return response || fetch(event.request);
        })
        .catch(() => createOfflineResponse())
    );
  }
});

function createOfflineResponse(): Response {
  return new Response(
    JSON.stringify({
      error: 'Offline - Resource not available',
      message: 'You are offline. Some features may not be available.'
    }),
    {
      status: 503,
      statusText: 'Service Unavailable',
      headers: new Headers({
        'Content-Type': 'application/json'
      })
    }
  );
}
