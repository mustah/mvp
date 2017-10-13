import {SearchParameter} from '../../search/models/searchParameterModels';

export const searchParametersOf = (key: string, values: string[]): SearchParameter[] => {
  return values
    .map((value: string) => ({
        name: key,
        value,
      }
    ));
};
