import { Injectable } from '@angular/core';
import { RemoteUsersService } from './users.service';

@Injectable()
export class LocalstorageRemoteUsersService extends RemoteUsersService {}
