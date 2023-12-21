import { Project } from './project';

export { Project } from './project';

export interface State {
  loading: boolean;
  creating: boolean;
  errorMessage?: string;
  accessibleProjects: Project[];
}

export const initialState: State = {
  loading: true,
  creating: false,
  accessibleProjects: [],
};
