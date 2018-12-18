import * as React from 'react';
import {compose} from 'recompose';
import {Maybe} from '../../helpers/Maybe';
import {firstUpperTranslated} from '../../services/translationService';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {EntityTypes, OnChangePage, Pagination} from '../../state/ui/pagination/paginationModels';
import {
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
import {MeterList} from '../../usecases/validation/components/MeterList';
import {SelectionResultActionsDropdown} from '../actions-dropdown/SelectionResultActionsDropdown';
import {componentOrNothing} from '../hoc/hocs';
import {withContent} from '../hoc/withContent';
import {withEmptyContent, WithEmptyContentProps} from '../hoc/withEmptyContent';
import {Column} from '../layouts/column/Column';
import {Loader} from '../loading/Loader';

interface SelectionPage {
  isSelectionPage: boolean;
}

export interface MeterListStateToProps extends SelectionPage {
  result: uuid[];
  entities: ObjectsById<Meter>;
  isFetching: boolean;
  parameters: EncodedUriParameters;
  pagination: Pagination;
  error: Maybe<ErrorResponse>;
  entityType: EntityTypes;
  isSuperAdmin: boolean;
}

export interface MeterListDispatchToProps {
  selectEntryAdd: OnClickWithId;
  syncWithMetering: OnClickWithId;
  syncMeters: CallbackWithIds;
  showMetersInGraph: CallbackWithIds;
  fetchMeters: FetchPaginated;
  changePage: OnChangePage;
  clearError: ClearErrorPaginated;
}

export type MeterListProps = MeterListStateToProps & MeterListDispatchToProps & ComponentId;

export interface MeterListActionDropdownProps {
  syncMeters: OnClick;
  showMetersInGraph: OnClick;
}

const MeterListWrapper = withEmptyContent<MeterListProps & WithEmptyContentProps>(MeterList);

const selectionPageEnhancer = componentOrNothing<SelectionPage>(({isSelectionPage}) => isSelectionPage);

const enhance = compose<MeterListActionDropdownProps, MeterListActionDropdownProps & HasContent & SelectionPage>(
  selectionPageEnhancer,
  withContent,
);

const SelectionPageActionsDropdown = enhance(SelectionResultActionsDropdown);

export const MeterListContent = (props: MeterListProps & WithChildren) => {
  React.useEffect(() => {
    const {fetchMeters, parameters, pagination: {page}} = props;
    fetchMeters(page, parameters);
  });
  const {
    clearError,
    syncMeters,
    showMetersInGraph,
    pagination: {page},
    result: meterIds,
    isFetching,
    isSelectionPage,
    error
  } = props;
  const {children, ...otherProps} = props;
  const hasContent = meterIds.length > 0;
  const wrapperProps: MeterListProps & WithEmptyContentProps = {
    ...otherProps,
    noContentText: firstUpperTranslated('no meters'),
    hasContent,
  };

  const onClearError = () => clearError({page});
  const onSyncMeters = () => syncMeters(meterIds);
  const onShowMetersInGraph = () => showMetersInGraph(meterIds);

  return (
    <Loader isFetching={isFetching} error={error} clearError={onClearError}>
      <Column>
        <SelectionPageActionsDropdown
          syncMeters={onSyncMeters}
          showMetersInGraph={onShowMetersInGraph}
          hasContent={hasContent}
          isSelectionPage={isSelectionPage}
        />
        <MeterListWrapper {...wrapperProps}/>
      </Column>
    </Loader>
  );
};
