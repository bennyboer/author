import { Project } from './project';

export { Project } from './project';

export interface State {
  loading: boolean;
  creating: boolean;
  removing: boolean;
  renaming: boolean;
  errorMessage?: string;
  accessibleProjects: Project[];
}

export const initialState: State = {
  loading: true,
  creating: false,
  removing: false,
  renaming: false,
  accessibleProjects: [],
};
