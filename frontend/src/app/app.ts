import { Component, OnInit } from '@angular/core';
import { AccountService, Account } from './account.service';
import { CommonModule } from '@angular/common';
import {FormsModule} from '@angular/forms';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true, 
  imports: [CommonModule, FormsModule], // We need CommonModule for async pipes and basic directives
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {

  // We'll store the fetched account here
  accounts$: Observable<Account[]> | undefined;

  // Form State Variables
  selectedSenderId : string ='';
  selectedReceiverId : string ='';
  transferAmount : number = 0;

  constructor(private accountService: AccountService) {}

  ngOnInit(): void {
    // When the component loads, fetch the data from Spring Boot
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.accounts$ = this.accountService.getAllAccounts();
  }

  executeTransfer(): void {
    // Frontend validation
    if(!this.selectedReceiverId || !this.selectedSenderId){
      alert('Please select both sender and receiver accounts.');
      return;
    }

    if(this.selectedReceiverId === this.selectedSenderId){
      alert('You cannot transfer funds to the same account.');
      return; 
    }

    if(this.transferAmount <= 0){
      alert('Please enter a valid transfer amount greater than zero.');
      return; 
    }

    // Call the service to perform the transfer
    this.accountService.transferFunds(this.selectedSenderId, this.selectedReceiverId, this.transferAmount).subscribe({
      next: (response) => {
        alert('Successfully transferred €' + this.transferAmount + ' from account ' + this.selectedSenderId + ' to account ' + this.selectedReceiverId);
        this.transferAmount = 0; // Reset the transfer amount
        this.loadAccounts(); // Refresh the account data to reflect the new balances
      },
      error: (err) => {
        console.error('Transfer failed: ', err);
        alert('Transfer failed: '+(err.error || 'insufficient funds or invalid account selection. Please try again.'));
      }
    });
  }
}
