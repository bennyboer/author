import { FEATURE_NAME } from './options';
import { reducer } from './reducer';
import { selectors } from './selectors';
import { actions } from './actions';

export const structureStore = {
  featureName: FEATURE_NAME,
  reducer,
  selectors,
  actions,
};

export { StructureStoreEffects } from './effects';
export { StructureService } from './service';

export {
  RemoteStructureService,
  HttpStructureService,
  LocalStorageStructureService,
} from './remote';
