import * as React from 'react';
import {compose} from 'recompose';
import {firstUpperTranslated} from '../../services/translationService';
import {useFetchMeters} from '../../state/domain-models-paginated/meter/fetchMetersHook';
import {OnDeleteMeter} from '../../state/domain-models-paginated/meter/meterApiActions';
import {LegendItem} from '../../state/report/reportModels';
import {Fetch, OnClickWith, OnClickWithId} from '../../types/Types';
import {MeterList, Props} from '../../usecases/meter/components/MeterList';
import {EmptyContentProps} from '../error-message/EmptyContent';
import {withEmptyContent} from '../hoc/withEmptyContent';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import {DispatchToProps} from '../infinite-list/InfiniteList';

export interface MeterDispatchToProps extends DispatchToProps {
  addToReport: OnClickWith<LegendItem>;
  deleteMeter: OnDeleteMeter;
  fetchLegendItems: Fetch;
  syncWithMetering: OnClickWithId;
}

type WrapperProps = Props & EmptyContentProps;

const MeterListWrapper = compose<WrapperProps & ThemeContext, WrapperProps>(
  withCssStyles,
  withEmptyContent
)(MeterList);

export const MeterGrid = (props: Props) => {
  const {
    fetchPaginated,
    fetchLegendItems,
    pagination: {page},
    parameters,
    sort,
  } = props;
  useFetchMeters({fetchPaginated, parameters, sort, page});

  React.useEffect(() => {
    fetchLegendItems(parameters);
  }, [parameters]);

  const wrapperProps: Props & EmptyContentProps = {
    ...props,
    noContentText: firstUpperTranslated('no meters'),
  };

  return <MeterListWrapper {...wrapperProps}/>;
};
