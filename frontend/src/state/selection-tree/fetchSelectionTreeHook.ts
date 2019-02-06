import * as React from 'react';
import {EncodedUriParameters, Fetch} from '../../types/Types';

interface Props {
  fetchSelectionTree: Fetch;
  parameters: EncodedUriParameters;
}

export const useFetchSelectionTree = ({fetchSelectionTree, parameters}: Props) => {
  React.useEffect(() => {
    fetchSelectionTree(parameters);
  }, [parameters]);
};
