import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import { environment } from '../environments/environment';

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
    private apiUrl = `${environment.apiUrl}/accounts`;

    // Injecting the HttpClient to make HTTP requests
    constructor(private http: HttpClient) {}

    // Method to fetch all accounts from the backend
    getAccount(id: string) : Observable<Account> {
        return this.http.get<Account>(`${this.apiUrl}/${id}`);
    }

    transferFunds(senderId: string, receiverId: string, amount: number): Observable<void> {
        const transferRequest = {
            senderId,
            receiverId,
            amount
        };
        return this.http.post<void>(`${this.apiUrl}/transfer`, transferRequest, { responseType: 'text' as 'json' });
    }

    getAllAccounts(): Observable<Account[]>{
        return this.http.get<Account[]>(this.apiUrl);
    }
}