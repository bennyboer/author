import { RootState } from './state';

export const selectFeature = (featureName: string) => {
  return (state: RootState) => state[featureName];
};
