import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { environment } from './environments/environment';
import { registerServiceWorker } from './register-service-worker';

(window as unknown as Record<string, unknown>)['global'] = window;

bootstrapApplication(AppComponent, appConfig)
  .then(() => {
    if (environment.production) {
      registerServiceWorker();
    }
  })
  .catch((error: unknown) => {
    console.error(error);
  });
