import * as React from 'react';
import {EncodedUriParameters, FetchPaginated} from '../../../types/Types';
import {SortOption} from '../../ui/pagination/paginationModels';

interface FetchMetersProps {
  fetchMeters: FetchPaginated;
  page: number;
  parameters: EncodedUriParameters;
  sortOptions?: SortOption[];
}

export const useFetchMeters = ({fetchMeters, page, parameters, sortOptions}: FetchMetersProps) => {
  React.useEffect(() => {
    fetchMeters(page, parameters, sortOptions);
  }, [page, parameters, sortOptions]);
};
