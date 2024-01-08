import { RootState } from './state';

export const selectFeature = <T>(featureName: string) => {
  return (state: RootState) => state[featureName] as T;
};
