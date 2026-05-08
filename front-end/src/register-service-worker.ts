/**
 * Register Service Worker for PWA offline support
 * Call this from main.ts or app.ts after app initialization
 */
export function registerServiceWorker(): void {
  if (!('serviceWorker' in navigator)) {
    console.warn('Service Workers are not supported in this browser');
    return;
  }

  navigator.serviceWorker
    .register('/service-worker.js', { scope: '/' })
    .then((registration) => {
      console.log('Service Worker registered:', registration);

      // Check for updates periodically
      setInterval(() => {
        registration.update();
      }, 60000); // Check every minute

      // Handle new service worker
      registration.addEventListener('updatefound', () => {
        const newWorker = registration.installing;
        if (newWorker) {
          newWorker.addEventListener('statechange', () => {
            if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
              console.log('New Service Worker available');
              // Notify user about update
              notifyUserAboutUpdate();
            }
          });
        }
      });
    })
    .catch((error) => {
      console.error('Service Worker registration failed:', error);
    });
}

function notifyUserAboutUpdate(): void {
  // This would integrate with your notification system
  console.log('Update available - users should refresh the page');
}

// Optional: Handle controlled (when SW is active)
if (navigator.serviceWorker.controller) {
  console.log('Service Worker is controlling this page');
}
