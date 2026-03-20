import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AccountService, Account } from './account.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  accounts: Account[] = [];

  selectedSenderId: string = '';
  selectedReceiverId: string = '';
  transferAmount: number = 0;

  constructor(private accountService: AccountService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.accountService.getAllAccounts().subscribe({
      next: (data) => {
        this.accounts = data;
        this.cdr.markForCheck();
      },
      error: (err) => console.error("Erreur de chargement", err)
    });
  }

  executeTransfer(): void {
    if (!this.selectedSenderId || !this.selectedReceiverId || this.transferAmount <= 0) {
      alert("Veuillez remplir correctement les champs.");
      return;
    }

    this.accountService.transferFunds(this.selectedSenderId, this.selectedReceiverId, this.transferAmount)
      .subscribe({
        next: () => {
          this.loadAccounts();
          this.transferAmount = 0;
          this.selectedSenderId = '';
          this.selectedReceiverId = '';
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error("Erreur de transfert :", err);
          alert("Le transfert a échoué.");
        }
      });
  }
}