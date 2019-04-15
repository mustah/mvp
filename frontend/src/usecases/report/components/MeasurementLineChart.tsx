import * as React from 'react';
import {RetryLoader} from '../../../components/loading/Loader';
import {GraphContents} from '../../../state/report/reportModels';
import {useFetchMeasurements} from '../../../state/ui/graph/measurement/measurementHook';
import {useGraphContents} from '../../../state/ui/graph/measurement/measurementSelectors';
import {MeasurementLineChartContainer} from '../containers/MeasurementLineChartContainer';
import {DispatchToProps, StateToProps} from '../containers/MeasurementsContainer';

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
  } = props;
  useFetchMeasurements(props);

  const graphContents: GraphContents = useGraphContents(measurementResponse);

  return (
    <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
      <MeasurementLineChartContainer
        graphContents={graphContents}
        outerHiddenKeys={hiddenLines}
        hasContent={hasContent}
        hasMeters={hasLegendItems}
        isSideMenuOpen={isSideMenuOpen}
      />
    </RetryLoader>
  );
};
