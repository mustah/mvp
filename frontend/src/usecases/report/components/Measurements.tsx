import * as React from 'react';
import {compose} from 'recompose';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {RetryLoader} from '../../../components/loading/Loader';
import {firstUpperTranslated} from '../../../services/translationService';
import {useFetchMeasurements} from '../../../state/ui/graph/measurement/measurementHook';
import {Props} from './MeasurementLineChart';
import {MeasurementList, MeasurementListProps} from './MeasurementList';

type WrapperProps = MeasurementListProps & WithEmptyContentProps;

const MeasurementListWrapper = compose<WrapperProps & ThemeContext, WrapperProps>(
  withCssStyles,
  withEmptyContent
)(MeasurementList);

export const Measurements = (props: Props) => {
  const {
    clearError,
    exportToExcelSuccess,
    hasContent,
    hasLegendItems,
    isFetching,
    measurement: {error, measurementResponse: {average, measurements}, isExportingToExcel}
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
