import * as React from 'react';
import {EncodedUriParameters, FetchPaginated} from '../../../types/Types';
import {SortOption} from '../../ui/pagination/paginationModels';

interface FetchMetersProps {
  fetchPaginated: FetchPaginated;
  page: number;
  parameters: EncodedUriParameters;
  sort?: SortOption[];
}

export const useFetchMeters = ({fetchPaginated, page, parameters, sort}: FetchMetersProps) => {
  React.useEffect(() => {
    fetchPaginated(page, parameters, sort);
  }, [page, parameters, sort]);
};
