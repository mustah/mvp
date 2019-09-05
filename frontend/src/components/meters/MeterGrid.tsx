import * as React from 'react';
import {compose} from 'recompose';
import {firstUpperTranslated} from '../../services/translationService';
import {useFetchMeters} from '../../state/domain-models-paginated/meter/fetchMetersHook';
import {OnDeleteMeter} from '../../state/domain-models-paginated/meter/meterApiActions';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {LegendItem} from '../../state/report/reportModels';
import {OnChangePage, Pagination, SortOption} from '../../state/ui/pagination/paginationModels';
import {
  CallbackWith,
  EncodedUriParameters,
  Fetch,
  Fetching,
  FetchPaginated,
  HasContent,
  OnClickWith,
  OnClickWithId,
  uuid
} from '../../types/Types';
import {MeterList, Props} from '../../usecases/meter/components/MeterList';
import {EmptyContentProps} from '../error-message/EmptyContent';
import {withEmptyContent} from '../hoc/withEmptyContent';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';

export interface StateToProps extends Fetching, HasContent {
  items: Meter[];
  pagination: Pagination;
  parameters: EncodedUriParameters;
  sort?: SortOption[];
  selectedItemId?: uuid;
}

export interface DispatchToProps {
  addToReport: OnClickWith<LegendItem>;
  changePage: OnChangePage;
  deleteMeter: OnDeleteMeter;
  fetchLegendItems: Fetch;
  fetchMeters: FetchPaginated;
  sortTable: CallbackWith<SortOption[]>;
  syncWithMetering: OnClickWithId;
}

type WrapperProps = Props & EmptyContentProps;

const MeterListWrapper = compose<WrapperProps & ThemeContext, WrapperProps>(
  withCssStyles,
  withEmptyContent
)(MeterList);

export const MeterGrid = (props: Props) => {
  const {
    fetchMeters,
    fetchLegendItems,
    pagination: {page},
    parameters,
    sort,
  } = props;
  useFetchMeters({fetchMeters, parameters, sort, page});

  React.useEffect(() => {
    fetchLegendItems(parameters);
  }, [parameters]);

  const wrapperProps: Props & EmptyContentProps = {
    ...props,
    noContentText: firstUpperTranslated('no meters'),
  };

  return <MeterListWrapper {...wrapperProps}/>;
};
