import { Component, signal, OnInit } from '@angular/core';
import { AccountService, Account } from './account.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true, 
  imports: [CommonModule], // We need CommonModule for async pipes and basic directives
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {

  // We'll store the fetched account here
  accountData: Account | null = null;

  private accountId = "c5ba7da0-887f-403a-af5a-97436244a3ae";
 

  constructor(private accountService: AccountService) {}

  ngOnInit(): void {
    // When the component loads, fetch the data from Spring Boot
    this.accountService.getAccount(this.accountId).subscribe({
      next:(data) => {
        this.accountData = data; // Store the fetched account data
        console.log('Fetched account data:', data);
      },
      error: (err) => {
        console.error('Failed to fetch account. Check CORS or UUID!', err);
      }
    }); 
      
      
    
  }
}
