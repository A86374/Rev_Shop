import { Component, OnDestroy, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { concat, map, of, switchMap, tap, toArray } from 'rxjs';
import {
  Category,
  ProductPayload,
  ProductPhoto,
  SellerProductService,
} from './seller-product.service';

interface PendingPhoto {
  file: File;
  preview: string; // local URL, shown before upload
}

// same rule as the backend: selling price can't be above MRP
const priceNotAboveMrp: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const mrp = Number(group.get('mrp')?.value);
  const price = Number(group.get('price')?.value);
  return mrp > 0 && price > mrp ? { priceAboveMrp: true } : null;
};

@Component({
  selector: 'app-product-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './product-form.component.html',
})
export class ProductFormComponent implements OnDestroy {
  private fb = inject(FormBuilder);
  private service = inject(SellerProductService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  readonly maxPhotos = 5;
  private readonly allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
  private readonly maxBytes = 2 * 1024 * 1024; // US-06: 2 MB per photo

  productId = signal<number | null>(null); // null while adding a new product
  categories = signal<Category[]>([]);
  photos = signal<ProductPhoto[]>([]); // already on S3
  pending = signal<PendingPhoto[]>([]); // picked now, uploaded on Save
  saving = signal(false);
  error = signal('');
  photoError = signal('');

  form = this.fb.nonNullable.group(
    {
      name: ['', [Validators.required, Validators.maxLength(150)]],
      description: ['', Validators.maxLength(1000)],
      categoryId: [0, Validators.min(1)],
      mrp: [0, [Validators.required, Validators.min(0.01)]],
      price: [0, [Validators.required, Validators.min(0.01)]],
      stock: [0, [Validators.required, Validators.min(0)]],
      lowStockThreshold: [5, [Validators.required, Validators.min(0)]],
    },
    { validators: priceNotAboveMrp },
  );

  constructor() {
    this.service.categories().subscribe((list) => this.categories.set(list));
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.productId.set(id);
      this.loadProduct(id);
    }
  }

  showError(name: string): boolean {
    const control = this.form.get(name);
    return !!control && control.invalid && (control.dirty || control.touched);
  }

  priceAboveMrp(): boolean {
    return this.form.hasError('priceAboveMrp');
  }

  onFiles(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);
    input.value = ''; // lets the same file be picked again
    this.photoError.set('');

    for (const file of files) {
      if (this.photos().length + this.pending().length >= this.maxPhotos) {
        this.photoError.set(`A product can have at most ${this.maxPhotos} photos`);
        break;
      }
      if (!this.allowedTypes.includes(file.type)) {
        this.photoError.set(`${file.name}: only JPG, PNG or WEBP photos`);
        continue;
      }
      if (file.size > this.maxBytes) {
        this.photoError.set(`${file.name} is bigger than 2 MB`);
        continue;
      }
      this.pending.update((list) => [...list, { file, preview: URL.createObjectURL(file) }]);
    }
  }

  removePending(index: number): void {
    URL.revokeObjectURL(this.pending()[index].preview);
    this.pending.update((list) => list.filter((_, i) => i !== index));
  }

  deletePhoto(photo: ProductPhoto): void {
    const id = this.productId();
    if (!id) {
      return;
    }
    this.service.deletePhoto(id, photo.id).subscribe({
      next: () => this.photos.update((list) => list.filter((p) => p.id !== photo.id)),
      error: (err) => this.photoError.set(err.error?.message ?? 'Could not delete the photo'),
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set('');

    const payload: ProductPayload = this.form.getRawValue();
    const id = this.productId();
    const save$ = id ? this.service.update(id, payload) : this.service.add(payload);

    save$
      .pipe(
        // remember the id: if an upload fails, the next Save edits instead of adding a second product
        tap((product) => this.productId.set(product.id)),
        switchMap((product) => {
          const uploads = this.pending().map((p) =>
            this.service.uploadPhoto(product.id, p.file).pipe(
              tap(() => this.removePending(0)), // uploads run one by one, so the first one just finished
            ),
          );
          return uploads.length
            ? concat(...uploads).pipe(
                toArray(),
                map(() => product),
              )
            : of(product);
        }),
      )
      .subscribe({
        next: () => this.router.navigate(['/seller/products']),
        error: (err) => {
          this.error.set(err.error?.message ?? 'Could not save the product');
          this.saving.set(false);
        },
      });
  }

  ngOnDestroy(): void {
    this.pending().forEach((p) => URL.revokeObjectURL(p.preview));
  }

  private loadProduct(id: number): void {
    // the seller list includes inactive products; GET /api/products/{id} would hide them
    this.service.myProducts().subscribe((list) => {
      const p = list.find((x) => x.id === id);
      if (!p) {
        this.error.set('Product not found');
        return;
      }
      this.form.patchValue({
        name: p.name,
        description: p.description ?? '',
        categoryId: p.category.id,
        mrp: p.mrp,
        price: p.price,
        stock: p.stock,
        lowStockThreshold: p.lowStockThreshold,
      });
    });
    this.service.photos(id).subscribe((list) => this.photos.set(list));
  }
}
