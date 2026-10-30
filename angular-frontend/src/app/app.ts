import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

// The navbar and footer live here (app.html). Abhinaya adds the notification bell, Nikitha the cart badge.
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly auth = inject(AuthService);
  protected readonly menuOpen = signal(false);      // the mobile menu
  protected readonly accountOpen = signal(false);   // the account menu under the avatar
  protected readonly year = new Date().getFullYear();

  // "Kiran Kumar" -> "KK", shown in the avatar circle
  protected readonly initials = computed(() => {
    const name = this.auth.user()?.fullName ?? '';
    return name.split(' ').filter(Boolean).slice(0, 2).map(part => part[0].toUpperCase()).join('');
  });

  toggleAccount(event: Event): void {
    event.stopPropagation();   // keeps the mobile menu open
    this.accountOpen.update(open => !open);
  }

  closeMenus(): void {
    this.menuOpen.set(false);
    this.accountOpen.set(false);
  }

  logout(): void {
    this.closeMenus();
    this.auth.logout();
  }
}
