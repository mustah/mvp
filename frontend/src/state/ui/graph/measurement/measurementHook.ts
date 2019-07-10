import * as React from 'react';
import {EncodedUriParameters, Fetch} from '../../../../types/Types';
import {FetchMeasurements, MeasurementParameters} from './measurementModels';

export type FetchMeasurementProps = StateToProps & DispatchToProps;

export interface StateToProps {
  parameters: EncodedUriParameters;
  measurementParameters: MeasurementParameters;
  shouldFetchMeasurements: boolean;
}

export interface DispatchToProps {
  fetchMeasurements: FetchMeasurements;
  fetchUserSelections: Fetch;
}

export const useFetchMeasurements = ({
  fetchMeasurements,
  fetchUserSelections,
  measurementParameters,
  parameters,
  shouldFetchMeasurements,
}: FetchMeasurementProps) => {
  React.useEffect(() => {
    fetchUserSelections();
    if (shouldFetchMeasurements) {
      fetchMeasurements(measurementParameters);
    }
  }, [measurementParameters, parameters, shouldFetchMeasurements]);
};
