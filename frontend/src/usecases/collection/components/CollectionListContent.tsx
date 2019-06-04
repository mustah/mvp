import * as React from 'react';
import {compose} from 'recompose';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {RetryLoader} from '../../../components/loading/Loader';
import {firstUpperTranslated} from '../../../services/translationService';
import {WithChildren} from '../../../types/Types';
import {DispatchToProps, StateToProps} from '../containers/CollectionListContainer';
import {CollectionStatList} from './CollectionStatList';

export type CollectionListProps = StateToProps & DispatchToProps;

type WrapperProps = CollectionListProps & WithEmptyContentProps;

const CollectionListWrapper = compose<WrapperProps & ThemeContext, WrapperProps>(
  withCssStyles,
  withEmptyContent
)(CollectionStatList);

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
  }, [page, parameters, sort]);

  const {children, ...otherProps} = props;
  const hasContent = result.length > 0;

  const wrapperProps: WrapperProps = {
    ...otherProps,
    hasContent,
    noContentText: firstUpperTranslated('no meters'),
  };

  return (
    <RetryLoader isFetching={isFetching} clearError={clearError} error={error}>
      <CollectionListWrapper {...wrapperProps}/>
    </RetryLoader>
  );
};
