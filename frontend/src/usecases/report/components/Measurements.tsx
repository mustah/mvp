import * as React from 'react';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {RetryLoader} from '../../../components/loading/Loader';
import {firstUpperTranslated} from '../../../services/translationService';
import {useFetchMeasurements} from '../../../state/ui/graph/measurement/measurementHook';
import {Props} from './MeasurementLineChart';
import {MeasurementList, MeasurementListProps} from './MeasurementList';

const MeasurementWrapper = withEmptyContent<MeasurementListProps & WithEmptyContentProps>(MeasurementList);

export const Measurements = (props: Props) => {
  const {
    clearError,
    measurement: {error, isFetching, measurementResponse, isExportingToExcel},
    exportToExcelSuccess,
    hasMeters,
    hasContent
  } = props;
  useFetchMeasurements(props);

  return (
    <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
      <MeasurementWrapper
        hasContent={hasContent}
        measurements={measurementResponse.measurements}
        noContentText={firstUpperTranslated(hasMeters ? 'no measurements' : 'no meters')}
        exportToExcelSuccess={exportToExcelSuccess}
        isExportingToExcel={isExportingToExcel}
      />
    </RetryLoader>
  );
};
