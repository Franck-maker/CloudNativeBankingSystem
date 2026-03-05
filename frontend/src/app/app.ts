import { Component, OnInit } from '@angular/core';
import { AccountService, Account } from './account.service';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true, 
  imports: [CommonModule], // We need CommonModule for async pipes and basic directives
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {

  // We'll store the fetched account here
  accountData$: Observable<Account> | undefined;

  private accountId = "73ea66c4-03af-4fcf-8fe1-0f9b3374ea51";
 

  constructor(private accountService: AccountService) {}

  ngOnInit(): void {
    // When the component loads, fetch the data from Spring Boot
    this.accountData$ = this.accountService.getAccount(this.accountId);
  }
}
