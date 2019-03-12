import * as React from 'react';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {RetryLoader} from '../../../components/loading/Loader';
import {Maybe} from '../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../services/translationService';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {CollectionStat} from '../../../state/domain-models/collection-stat/collectionStatModels';
import {
  ApiRequestSortingOptions,
  EntityTypes,
  OnChangePage,
  Pagination
} from '../../../state/ui/pagination/paginationModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {
  CallbackWith,
  ClearErrorPaginated,
  ComponentId,
  EncodedUriParameters,
  ErrorResponse, FetchPaginated,
  uuid, WithChildren
} from '../../../types/Types';
import {CollectionStatList} from './CollectionStatList';

export interface CollectionStateToProps {
  result: uuid[];
  entities: ObjectsById<CollectionStat>;
  isFetching: boolean;
  parameters: EncodedUriParameters;
  sort?: ApiRequestSortingOptions[];
  pagination: Pagination;
  error: Maybe<ErrorResponse>;
  entityType: EntityTypes;
  timePeriod: SelectionInterval;
}

export interface CollectionDispatchToProps {
  changePage: OnChangePage;
  clearError: ClearErrorPaginated;
  sortTable: CallbackWith<ApiRequestSortingOptions[]>;
  fetchCollectionStatsFacilityPaged: FetchPaginated;
}

export type CollectionListProps = CollectionStateToProps & CollectionDispatchToProps & ComponentId;

const CollectionListWrapper = withEmptyContent<CollectionListProps & WithEmptyContentProps>(CollectionStatList);

export const CollectionListContent = (props: CollectionListProps & WithChildren) => {
  const {
    error,
    clearError,
    fetchCollectionStatsFacilityPaged,
    isFetching,
    pagination: {page},
    parameters,
    result,
    sort,
  } = props;

  React.useEffect(() => {
    fetchCollectionStatsFacilityPaged(page, parameters, sort);
  }, [parameters, sort, page, result]);

  const {children, ...otherProps} = props;
  const hasContent = result.length > 0;

  const wrapperProps: CollectionListProps & WithEmptyContentProps = {
    ...otherProps,
    noContentText: firstUpperTranslated('no meters'),
    hasContent,
  };

  return (
    <RetryLoader isFetching={isFetching} clearError={clearError} error={error}>
        <CollectionListWrapper {...wrapperProps}/>
    </RetryLoader>
  );
};
