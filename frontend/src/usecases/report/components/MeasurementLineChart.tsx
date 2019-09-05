import * as React from 'react';
import {RetryLoader} from '../../../components/loading/Loader';
import '../../../components/timestamp-info-message/TimestampInfoMessage.scss';
import {GraphContents} from '../../../state/report/reportModels';
import {useFetchMeasurements} from '../../../state/ui/graph/measurement/measurementHook';
import {useGraphContents} from '../../../state/ui/graph/measurement/measurementSelectors';
import {DispatchToProps, StateToProps} from '../containers/ReportMeasurementsContainer';
import {LineChartComponent} from './line-chart/LineChartComponent';
import {VisibilitySummary} from './VisibilitySummary';

export type Props = StateToProps & DispatchToProps;

export const MeasurementLineChart = ({
  clearError,
  hiddenLines,
  hasContent,
  hasLegendItems,
  isFetching,
  isSideMenuOpen,
  measurement: {error, measurementResponse},
  selectedQuantities,
  threshold,
  visibilitySummary,
  ...fetchMeasurementProps
}: Props) => {
  useFetchMeasurements(fetchMeasurementProps);

  const graphContents: GraphContents = useGraphContents(measurementResponse, selectedQuantities);

  const hiddenLinesNote = visibilitySummary
    ? <VisibilitySummary {...visibilitySummary}/>
    : null;

  return (
    <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
      <>
        {hiddenLinesNote}
        <LineChartComponent
          graphContents={graphContents}
          hasContent={hasContent}
          hasMeters={hasLegendItems}
          isSideMenuOpen={isSideMenuOpen}
          outerHiddenKeys={hiddenLines}
          threshold={threshold}
          visibilitySummary={visibilitySummary}
        />
      </>
    </RetryLoader>
  );
};
