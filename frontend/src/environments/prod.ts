import { Environment } from './environment';

export const environment: Environment = {
  production: true,
  apiUrl: 'http://localhost:7070/api', // TODO We do not have a production backend yet
  webSocketUrl: 'ws://localhost:7070/ws', // TODO We do not have a production backend yet
};
