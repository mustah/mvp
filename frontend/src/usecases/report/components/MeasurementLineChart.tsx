import * as React from 'react';
import {RetryLoader} from '../../../components/loading/Loader';
import {useFetchMeasurements} from '../../../state/ui/graph/measurement/measurementHook';
import {useGraphContents} from '../../../state/ui/graph/measurement/measurementSelectors';
import {MeasurementLineChartContainer} from '../containers/MeasurementLineChartContainer';
import {DispatchToProps, StateToProps} from '../containers/MeasurementsContainer';
import {GraphContents} from '../reportModels';

export type Props = StateToProps & DispatchToProps;

export const MeasurementLineChart = (props: Props) => {
  const {
    clearError,
    hiddenLines,
    measurement: {error, isFetching, measurementResponse},
  } = props;
  useFetchMeasurements(props);

  const graphContents: GraphContents = useGraphContents(measurementResponse);

  return (
    <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
      <MeasurementLineChartContainer graphContents={graphContents} outerHiddenKeys={hiddenLines}/>
    </RetryLoader>
  );
};
