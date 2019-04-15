import * as React from 'react';
import {RetryLoader} from '../../../components/loading/Loader';
import {GraphContents} from '../../../state/report/reportModels';
import {useFetchMeasurements} from '../../../state/ui/graph/measurement/measurementHook';
import {useGraphContents} from '../../../state/ui/graph/measurement/measurementSelectors';
import {DispatchToProps, StateToProps} from '../containers/MeasurementsContainer';
import {LineChartComponent} from './line-chart/LineChartComponent';

export type Props = StateToProps & DispatchToProps;

export const MeasurementLineChart = (props: Props) => {
  const {
    clearError,
    hiddenLines,
    hasContent,
    hasLegendItems,
    isFetching,
    isSideMenuOpen,
    measurement: {error, measurementResponse},
    threshold,
  } = props;
  useFetchMeasurements(props);

  const graphContents: GraphContents = useGraphContents(measurementResponse);

  return (
    <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
      <LineChartComponent
        graphContents={graphContents}
        hasContent={hasContent}
        hasMeters={hasLegendItems}
        isSideMenuOpen={isSideMenuOpen}
        outerHiddenKeys={hiddenLines}
        threshold={threshold}
      />
    </RetryLoader>
  );
};
