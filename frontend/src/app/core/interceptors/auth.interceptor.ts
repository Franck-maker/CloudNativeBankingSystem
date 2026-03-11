import { HttpInterceptorFn } from "@angular/common/http";


/**
 * 
 * This intercepts every outgoing request and 
 * automatically attaches the Base64-encoded credentials for Basic Authentication.
 * 
 * This way, we don't have to manually add the Authorization header in every service method. 
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
    // The credentials we injected via the DatabaseSeeder
    const username = "admin@bank.local";
    const password = "admin123";

    // Encode the credentials in Base64 for Basic Authentication
    const encodedCredentials = btoa(`${username}:${password}`); 
    const basicAuthHeader = `Basic ${encodedCredentials}`;

    // Clone the outgoing request and securely set the Authorization header
    const secureReq = req.clone({
        setHeaders: {
            Authorization: basicAuthHeader
        }
    });
    // Send the modified request to the Gateway
    return next(secureReq); 
}
