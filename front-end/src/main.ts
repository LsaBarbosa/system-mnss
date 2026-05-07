import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { registerServiceWorker } from './register-service-worker';

(window as any).global = window;

bootstrapApplication(AppComponent, appConfig).then(() => {
  registerServiceWorker();
}).catch((error: unknown) => {
  console.error(error);
});
