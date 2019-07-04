import * as React from 'react';
import {Props} from '../../../../usecases/report/components/MeasurementLineChart';

export const useFetchMeasurements = ({
  fetchMeasurements,
  fetchUserSelections,
  measurementParameters,
  parameters,
  userSelections,
}: Props) => {
  React.useEffect(() => {
    fetchUserSelections();
    if (userSelections.isSuccessfullyFetched) {
      fetchMeasurements(measurementParameters);
    }
  }, [userSelections, measurementParameters, parameters]);
};
