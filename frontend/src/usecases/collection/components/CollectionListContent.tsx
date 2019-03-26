import * as React from 'react';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {RetryLoader} from '../../../components/loading/Loader';
import {firstUpperTranslated} from '../../../services/translationService';
import {ComponentId, WithChildren} from '../../../types/Types';
import {DispatchToProps, StateToProps} from '../containers/CollectionListContainer';
import {CollectionStatList} from './CollectionStatList';

export type CollectionListProps = StateToProps & DispatchToProps & ComponentId;

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
