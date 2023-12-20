export enum LoginError {
  None = 'None',
  InvalidCredentials = 'InvalidCredentials',
  TooManyAttempts = 'TooManyAttempts',
  Unknown = 'Unknown',
}

export class LoginErrors {
  static fromStatusCode(statusCode: number): LoginError {
    switch (statusCode) {
      case 401:
        return LoginError.InvalidCredentials;
      case 429:
        return LoginError.TooManyAttempts;
      default:
        return LoginError.Unknown;
    }
  }
}
