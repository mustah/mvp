import * as React from 'react';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {RetryLoader} from '../../../components/loading/Loader';
import {firstUpperTranslated} from '../../../services/translationService';
import {useFetchMeasurements} from '../../../state/ui/graph/measurement/measurementHook';
import {Props} from './MeasurementLineChart';
import {MeasurementList, MeasurementListProps} from './MeasurementList';

type WrapperProps = MeasurementListProps & WithEmptyContentProps;

const MeasurementListWrapper = withEmptyContent<WrapperProps>(MeasurementList);

export const Measurements = (props: Props) => {
  const {
    clearError,
    measurement: {error, isFetching, measurementResponse: {average, measurements}, isExportingToExcel},
    exportToExcelSuccess,
    hasLegendItems,
    hasContent
  } = props;
  useFetchMeasurements(props);

  const wrapperProps: WrapperProps = {
    exportToExcelSuccess,
    hasContent,
    isExportingToExcel,
    measurements: React.useMemo(() => [...average, ...measurements], [average, measurements]),
    noContentText: firstUpperTranslated(hasLegendItems ? 'no measurements' : 'no meters'),
  };

  return (
    <RetryLoader isFetching={isFetching} error={error} clearError={clearError}>
      <MeasurementListWrapper{...wrapperProps}/>
    </RetryLoader>
  );
};
