import * as React from 'react';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {Loader} from '../../../components/loading/Loader';
import {firstUpperTranslated} from '../../../services/translationService';
import {useFetchMeasurements} from '../../../state/ui/graph/measurement/measurementHook';
import {Props} from './graph/Graph';
import {MeasurementList, MeasurementListProps} from './MeasurementList';

const MeasurementWrapper = withEmptyContent<MeasurementListProps & WithEmptyContentProps>(MeasurementList);

export const Measurements = (props: Props) => {
  const {
    clearError,
    measurement: {error, isFetching, measurementResponse: {measurements}, isExportingToExcel},
    exportToExcelSuccess,
    hasMeters,
  } = props;
  useFetchMeasurements(props);

  return (
    <Loader isFetching={isFetching} error={error} clearError={clearError}>
      <MeasurementWrapper
        hasContent={measurements.length > 0}
        measurements={measurements}
        noContentText={firstUpperTranslated(hasMeters ? 'no measurements' : 'no meters')}
        exportToExcelSuccess={exportToExcelSuccess}
        isExportingToExcel={isExportingToExcel}
      />
    </Loader>
  );
};
