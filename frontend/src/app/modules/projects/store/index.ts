import { reducer } from './reducer';
import { selectors } from './selectors';
import { actions } from './actions';
import { FEATURE_NAME } from './options';

export const projectsStore = {
  featureName: FEATURE_NAME,
  reducer,
  selectors,
  actions,
};

export { ProjectsStoreEffects } from './effects';
export { ProjectsService } from './service';

export {
  RemoteProjectsService,
  HttpProjectsService,
  LocalStorageProjectsService,
} from './remote';
