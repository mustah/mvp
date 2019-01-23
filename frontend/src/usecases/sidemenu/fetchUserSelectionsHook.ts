import * as React from 'react';
import {Fetch} from '../../types/Types';

export const useFetchUserSelections = (fetchUserSelections: Fetch) => {
  React.useEffect(() => fetchUserSelections());
};
