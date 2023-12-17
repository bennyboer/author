import { FEATURE_NAME } from './options';
import { reducer } from './reducer';
import { actions } from './actions';
import { selectors } from './selectors';

export const loginStore = {
  featureName: FEATURE_NAME,
  reducer,
  actions,
  selectors,
};

export { LoginStoreEffects } from './effects';
export { LoginService } from './service';

export {
  RemoteLoginService,
  HttpLoginService,
  LocalStorageLoginService,
} from './remote';
