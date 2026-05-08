import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'mnss-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  template: `
    <router-outlet />
    <footer class="app-footer">
      <div class="footer-content">
        <span>&copy; 2026 Nova Aliança — Sistema MNSS</span>
        <span class="version-tag">v0.0.1-MVP</span>
      </div>
    </footer>
  `,
  styles: [
    `
      .app-footer {
        padding: 1rem 2rem;
        background: #f8fafc;
        border-top: 1px solid #e2e8f0;
        font-size: 0.75rem;
        color: #64748b;
        margin-top: auto;
      }
      .footer-content {
        display: flex;
        justify-content: space-between;
        max-width: 1200px;
        margin: 0 auto;
      }
      .version-tag {
        font-weight: 600;
        color: #2563eb;
      }
    `
  ]
})
export class AppComponent {}
