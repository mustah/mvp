import * as React from 'react';
import {Maybe} from '../../helpers/Maybe';
import {firstUpperTranslated} from '../../services/translationService';
import {OnDeleteMeter} from '../../state/domain-models-paginated/meter/meterApiActions';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {
  ApiRequestSortingOptions,
  EntityTypes,
  OnChangePage,
  Pagination
} from '../../state/ui/pagination/paginationModels';
import {
  CallbackWith,
  CallbackWithIds,
  ClearErrorPaginated,
  ComponentId,
  EncodedUriParameters,
  ErrorResponse,
  FetchPaginated,
  HasContent,
  OnClick,
  OnClickWithId,
  uuid,
  WithChildren
} from '../../types/Types';
import {MeterList} from '../../usecases/meter/components/MeterList';
import {MeterListActionsDropdown} from '../actions-dropdown/MeterListActionsDropdown';
import {withContent} from '../hoc/withContent';
import {withEmptyContent, WithEmptyContentProps} from '../hoc/withEmptyContent';
import {Column} from '../layouts/column/Column';
import {Loader} from '../loading/Loader';

export interface MeterListStateToProps {
  result: uuid[];
  entities: ObjectsById<Meter>;
  isFetching: boolean;
  parameters: EncodedUriParameters;
  sort?: ApiRequestSortingOptions[];
  pagination: Pagination;
  error: Maybe<ErrorResponse>;
  entityType: EntityTypes;
  isSuperAdmin: boolean;
  page: number;
}

export interface MeterListDispatchToProps {
  deleteMeter: OnDeleteMeter;
  selectEntryAdd: OnClickWithId;
  syncWithMetering: OnClickWithId;
  syncMeters: CallbackWithIds;
  fetchMeters: FetchPaginated;
  changePage: OnChangePage;
  clearError: ClearErrorPaginated;
  sortTable: CallbackWith<ApiRequestSortingOptions[]>;
}

export type MeterListProps = MeterListStateToProps & MeterListDispatchToProps & ComponentId;

export interface MeterListActionDropdownProps {
  syncMeters: OnClick;
}

const MeterListWrapper = withEmptyContent<MeterListProps & WithEmptyContentProps>(MeterList);

const MeterListActionsDropdownEnhanced =
  withContent<HasContent & MeterListActionDropdownProps>(MeterListActionsDropdown);

export const MeterListContent = (props: MeterListProps & WithChildren) => {
  React.useEffect(() => {
    const {fetchMeters, parameters, pagination: {page}, sort} = props;
    fetchMeters(page, parameters, sort);
  });

  const {
    clearError,
    syncMeters,
    pagination: {page},
    result,
    isFetching,
    error
  } = props;

  const {children, ...otherProps} = props;
  const hasContent = result.length > 0;
  const wrapperProps: MeterListProps & WithEmptyContentProps = {
    ...otherProps,
    noContentText: firstUpperTranslated('no meters'),
    hasContent,
  };

  const onClearError = () => clearError({page});
  const onSyncMeters = () => syncMeters(result);

  return (
    <Loader isFetching={isFetching} error={error} clearError={onClearError}>
      <Column className="MeterListContent">
        <MeterListActionsDropdownEnhanced
          syncMeters={onSyncMeters}
          hasContent={hasContent}
        />
        <MeterListWrapper {...wrapperProps}/>
      </Column>
    </Loader>
  );
};
