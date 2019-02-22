import * as React from 'react';
import {Props} from '../../../../usecases/report/components/MeasurementLineChart';

export const useFetchMeasurements = ({
  fetchMeasurements,
  measurement,
  parameters,
  requestParameters,
}: Props) => {
  React.useEffect(() => {
    fetchMeasurements(requestParameters);
  }, [requestParameters, parameters]);
};
