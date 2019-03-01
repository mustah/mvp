import * as React from 'react';
import {Props} from '../../../../usecases/report/components/MeasurementLineChart';

export const useFetchMeasurements = ({
  fetchMeasurements,
  fetchUserSelections,
  parameters,
  requestParameters,
  userSelections,
}: Props) => {
  React.useEffect(() => {
    fetchUserSelections();
    if (userSelections.isSuccessfullyFetched) {
      fetchMeasurements(requestParameters);
    }
  }, [userSelections, requestParameters, parameters]);
};
