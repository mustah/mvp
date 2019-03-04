import * as React from 'react';
import {EncodedUriParameters, FetchPaginated} from '../../../types/Types';
import {ObjectsById} from '../../domain-models/domainModels';
import {ApiRequestSortingOptions} from '../../ui/pagination/paginationModels';
import {Meter} from './meterModels';

interface FetchMetersProps {
  fetchMeters: FetchPaginated;
  page: number;
  parameters: EncodedUriParameters;
  sort?: ApiRequestSortingOptions[];
  entities: ObjectsById<Meter>;
}

export const useFetchMeters = ({fetchMeters, page, parameters, sort, entities}: FetchMetersProps) => {
  React.useEffect(() => {
    fetchMeters(page, parameters, sort);
  }, [parameters, sort, page, entities]);
};
