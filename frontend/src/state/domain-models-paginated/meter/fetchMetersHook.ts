import * as React from 'react';
import {EncodedUriParameters, FetchPaginated} from '../../../types/Types';
import {ApiRequestSortingOptions} from '../../ui/pagination/paginationModels';

interface FetchMetersProps {
  fetchMeters: FetchPaginated;
  page: number;
  parameters: EncodedUriParameters;
  sort?: ApiRequestSortingOptions[];
}

export const useFetchMeters = ({fetchMeters, page, parameters, sort}: FetchMetersProps) => {
  React.useEffect(() => {
    fetchMeters(page, parameters, sort);
  }, [parameters, sort, page]);
};
