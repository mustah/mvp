import * as React from 'react';
import {Maybe} from '../../helpers/Maybe';
import {firstUpperTranslated} from '../../services/translationService';
import {useFetchMeters} from '../../state/domain-models-paginated/meter/fetchMetersHook';
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
  OnClick,
  OnClickWith,
  OnClickWithId,
  uuid,
  WithChildren
} from '../../types/Types';
import {MeterList} from '../../usecases/meter/components/MeterList';
import {toLegendItem} from '../../usecases/report/helpers/legendHelper';
import {LegendItem} from '../../usecases/report/reportModels';
import {MeterListActionsDropdown} from '../actions-dropdown/MeterListActionsDropdown';
import {withContent} from '../hoc/withContent';
import {withEmptyContent, WithEmptyContentProps} from '../hoc/withEmptyContent';
import {Column} from '../layouts/column/Column';
import {RetryLoader} from '../loading/Loader';

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
}

export interface MeterListDispatchToProps {
  deleteMeter: OnDeleteMeter;
  addAllToReport: OnClickWith<LegendItem[]>;
  addToReport: OnClickWith<LegendItem>;
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
  addAllToReport: OnClick;
}

const MeterListWrapper = withEmptyContent<MeterListProps & WithEmptyContentProps>(MeterList);

const MeterListActionsDropdownWrapper = withContent<MeterListActionDropdownProps>(MeterListActionsDropdown);

export const MeterListContent = (props: MeterListProps & WithChildren) => {
  const {
    addAllToReport,
    clearError,
    entities,
    error,
    fetchMeters,
    isFetching,
    pagination: {page},
    parameters,
    result,
    sort,
    syncMeters,
  } = props;
  useFetchMeters({fetchMeters, parameters, sort, page, entities});
  const {children, ...otherProps} = props;
  const hasContent = result.length > 0;
  const wrapperProps: MeterListProps & WithEmptyContentProps = {
    ...otherProps,
    noContentText: firstUpperTranslated('no meters'),
    hasContent,
  };

  const onAddAllToReport = () => addAllToReport(result.map(id => entities[id]).map(toLegendItem));
  const onClearError = () => clearError({page});
  const onSyncMeters = () => syncMeters(result);

  return (
    <RetryLoader isFetching={isFetching} error={error} clearError={onClearError}>
      <Column className="MeterListContent">
        <MeterListActionsDropdownWrapper
          addAllToReport={onAddAllToReport}
          syncMeters={onSyncMeters}
          hasContent={hasContent}
        />
        <MeterListWrapper {...wrapperProps}/>
      </Column>
    </RetryLoader>
  );
};
