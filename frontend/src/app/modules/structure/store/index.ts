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
export { StructureTreeService } from './service';

export {
  TreeService,
  HttpTreeService,
  LocalStorageTreeService,
} from './remote';
