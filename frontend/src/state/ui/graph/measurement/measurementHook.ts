import * as React from 'react';
import {Props} from '../../../../usecases/report/components/graph/Graph';

export const useFetchMeasurements = ({
  fetchMeasurements,
  measurement,
  parameters,
  requestParameters,
  showMetersInGraph
}: Props) => {
  React.useEffect(() => {
    fetchMeasurements(requestParameters);
  }, [requestParameters]);
};
