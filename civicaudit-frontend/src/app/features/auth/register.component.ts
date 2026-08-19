import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  registerForm: FormGroup;
  chargement = false;
  erreur = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/.*\d.*/)]],
      confirmPassword: ['', [Validators.required]],
      acceptCgu: [false, [Validators.requiredTrue]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const password = group.get('password');
    const confirmPassword = group.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.chargement = true;
    this.erreur = '';

    const { email, password } = this.registerForm.value;
    this.authService.register({ email, password }).subscribe({
      next: () => {
        this.chargement = false;
        this.router.navigate(['/marches']);
      },
      error: (err) => {
        this.chargement = false;
        this.erreur = err.error?.message || 'Une erreur est survenue lors de l\'inscription';
        this.cdr.detectChanges();
      }
    });
  }

  getErrorMessage(field: string): string {
    const control = this.registerForm.get(field);
    if (!control || !control.touched) return '';

    if (control.hasError('required')) return 'Ce champ est requis';
    if (control.hasError('email')) return 'Email invalide';
    if (control.hasError('minlength')) return 'Minimum 8 caractères';
    if (control.hasError('pattern')) return 'Doit contenir au moins 1 chiffre';
    if (control.hasError('passwordMismatch')) return 'Les mots de passe ne correspondent pas';
    return '';
  }
}
