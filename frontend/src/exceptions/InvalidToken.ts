import {Unauthorized} from '../usecases/auth/authModels';

export class InvalidToken implements Error, Unauthorized {

  name: string;
  message: string;
  timestamp: number;
  error: string;
  path: string;
  status: number;

  constructor(message: string) {
    this.message = message;
    this.name = 'InvalidToken';
    this.timestamp = -1;
  }
}
