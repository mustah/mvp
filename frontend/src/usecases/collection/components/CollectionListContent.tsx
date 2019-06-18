import * as React from 'react';
import {compose} from 'recompose';
import {EmptyContentProps} from '../../../components/error-message/EmptyContent';
import {withEmptyContent} from '../../../components/hoc/withEmptyContent';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {firstUpperTranslated} from '../../../services/translationService';
import {CollectionStat} from '../../../state/domain-models/collection-stat/collectionStatModels';
import {EntityTyped, OnChangePage, Pagination, SortOption} from '../../../state/ui/pagination/paginationModels';
import {
  Callback,
  CallbackWith,
  EncodedUriParameters,
  Fetch,
  Fetching,
  FetchPaginated,
  HasContent,
  uuid
} from '../../../types/Types';
import {CollectionStatList} from './CollectionStatList';

export interface StateToProps extends EntityTyped, Fetching, HasContent {
  excelExportParameters: EncodedUriParameters;
  isExportingToExcel: boolean;
  items: CollectionStat[];
  itemsToExport: CollectionStat[];
  parameters: EncodedUriParameters;
  pagination: Pagination;
  selectedItemId?: uuid;
  sort?: SortOption[];
}

export interface DispatchToProps {
  changePage: OnChangePage;
  exportToExcelSuccess: Callback;
  fetchAllCollectionStats: Fetch;
  fetchCollectionStatsFacilityPaged: FetchPaginated;
  sortTable: CallbackWith<SortOption[]>;
}

export type Props = StateToProps & DispatchToProps;

const CollectionListWrapper = compose<Props & ThemeContext, Props>(
  withCssStyles,
  withEmptyContent
)(CollectionStatList);

export const CollectionListContent = (props: Props) => {
  const {
    excelExportParameters,
    fetchAllCollectionStats,
    fetchCollectionStatsFacilityPaged,
    isExportingToExcel,
    pagination: {page},
    parameters,
    sort,
  } = props;

  React.useEffect(() => {
    fetchCollectionStatsFacilityPaged(page, parameters, sort);
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
