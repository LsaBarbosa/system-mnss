import { DestroyRef, Directive, Input, TemplateRef, ViewContainerRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import type { RoleName } from '../../core/auth/auth.models';
import { AuthService } from '../../core/auth/auth.service';

@Directive({
  selector: '[mnssHasRole]',
  standalone: true
})
export class HasRoleDirective {
  private readonly destroyRef = inject(DestroyRef);
  private readonly templateRef = inject(TemplateRef<unknown>);
  private readonly viewContainerRef = inject(ViewContainerRef);
  private readonly authService = inject(AuthService);
  private requiredRoles: readonly RoleName[] = [];
  private hasView = false;

  constructor() {
    this.authService.currentUser$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.updateView());
  }

  @Input()
  set mnssHasRole(requiredRoles: readonly RoleName[]) {
    this.requiredRoles = requiredRoles;
    this.updateView();
  }

  private updateView(): void {
    const canRender = this.authService.hasAnyRole(this.requiredRoles);

    if (canRender && !this.hasView) {
      this.viewContainerRef.createEmbeddedView(this.templateRef);
      this.hasView = true;
      return;
    }

    if (!canRender && this.hasView) {
      this.viewContainerRef.clear();
      this.hasView = false;
    }
  }
}
