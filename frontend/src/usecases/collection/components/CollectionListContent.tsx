import * as React from 'react';
import {compose} from 'recompose';
import {EmptyContentProps} from '../../../components/error-message/EmptyContent';
import {withEmptyContent} from '../../../components/hoc/withEmptyContent';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {DispatchToProps, StateToProps} from '../../../components/infinite-list/InfiniteList';
import {firstUpperTranslated} from '../../../services/translationService';
import {CollectionStat} from '../../../state/domain-models/collection-stat/collectionStatModels';
import {Callback, EncodedUriParameters, Fetch} from '../../../types/Types';
import {CollectionStatList} from './CollectionStatList';

export interface CollectionStatsStateToProps extends StateToProps<CollectionStat> {
  excelExportParameters: EncodedUriParameters;
  isExportingToExcel: boolean;
  itemsToExport: CollectionStat[];
  paddingBottom?: number;
}

export interface CollectionStatsDispatchToProps extends DispatchToProps {
  exportToExcelSuccess: Callback;
  fetchAllCollectionStats: Fetch;
}

export type Props = CollectionStatsStateToProps & CollectionStatsDispatchToProps;

const CollectionListWrapper = compose<Props & ThemeContext, Props>(
  withCssStyles,
  withEmptyContent
)(CollectionStatList);

export const CollectionListContent = (props: Props) => {
  const {
    excelExportParameters,
    fetchAllCollectionStats,
    fetchPaginated,
    isExportingToExcel,
    pagination: {page},
    parameters,
    sort,
  } = props;

  React.useEffect(() => {
    fetchPaginated(page, parameters, sort);
  }, [page, parameters, sort]);

  React.useEffect(() => {
    if (isExportingToExcel) {
      fetchAllCollectionStats(excelExportParameters);
    }
  }, [isExportingToExcel]);

  const wrapperProps: Props & EmptyContentProps = {
    ...props,
    noContentText: firstUpperTranslated('no meters'),
  };

  return <CollectionListWrapper {...wrapperProps}/>;
};
