import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

//Matches the AccountResponse DTO from the backend
export interface Account {
    id : string;
    owner: string;
    balance : number; 
}

@Injectable({
    providedIn: 'root'
})
export class AccountService {
    private apiUrl = 'http://localhost:8080/api/v1/accounts';

    // Injecting the HttpClient to make HTTP requests
    constructor(private http: HttpClient) {}

    // Method to fetch all accounts from the backend
    getAccount(id: string) : Observable<Account> {
        return this.http.get<Account>(`${this.apiUrl}/${id}`);
    }
}