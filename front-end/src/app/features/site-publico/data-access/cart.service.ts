import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface CartItem {
  productId: string;
  name: string;
  price: number;
  quantity: number;
  observation?: string;
  imageUrl?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private itemsSubject = new BehaviorSubject<CartItem[]>([]);
  items$ = this.itemsSubject.asObservable();

  constructor() {
    this.loadFromStorage();
  }

  private loadFromStorage() {
    const saved = localStorage.getItem('cart_items');
    if (saved) {
      try {
        this.itemsSubject.next(JSON.parse(saved));
      } catch (e) {
        console.error('Error loading cart from storage', e);
      }
    }
  }

  private saveToStorage(items: CartItem[]) {
    localStorage.setItem('cart_items', JSON.stringify(items));
  }

  addItem(item: CartItem) {
    const current = this.itemsSubject.value;
    const existing = current.find((i) => i.productId === item.productId && i.observation === item.observation);

    let updated: CartItem[];
    if (existing) {
      updated = current.map((i) => (i === existing ? { ...i, quantity: i.quantity + item.quantity } : i));
    } else {
      updated = [...current, item];
    }

    this.itemsSubject.next(updated);
    this.saveToStorage(updated);
  }

  removeItem(productId: string, observation?: string) {
    const updated = this.itemsSubject.value.filter(
      (i) => !(i.productId === productId && i.observation === observation)
    );
    this.itemsSubject.next(updated);
    this.saveToStorage(updated);
  }

  updateQuantity(productId: string, quantity: number, observation?: string) {
    if (quantity <= 0) {
      this.removeItem(productId, observation);
      return;
    }

    const updated = this.itemsSubject.value.map((i) =>
      i.productId === productId && i.observation === observation ? { ...i, quantity } : i
    );
    this.itemsSubject.next(updated);
    this.saveToStorage(updated);
  }

  clear() {
    this.itemsSubject.next([]);
    localStorage.removeItem('cart_items');
  }

  getSubtotal(): Observable<number> {
    return this.items$.pipe(map((items) => items.reduce((acc, item) => acc + item.price * item.quantity, 0)));
  }

  getItemCount(): Observable<number> {
    return this.items$.pipe(map((items) => items.reduce((acc, item) => acc + item.quantity, 0)));
  }
}
