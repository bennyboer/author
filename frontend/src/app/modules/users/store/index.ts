import { FEATURE_NAME } from './options';
import { reducer } from './reducer';
import { actions } from './actions';
import { selectors } from './selectors';

export const usersStore = {
  featureName: FEATURE_NAME,
  reducer,
  selectors,
  actions,
};

export { UsersStoreEffects } from './effects';
export { UsersService } from './service';

export {
  RemoteUsersService,
  HttpRemoteUsersService,
  LocalstorageRemoteUsersService,
} from './remote';
