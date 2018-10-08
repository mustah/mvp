import * as React from 'react';
import {connect} from 'react-redux';
import {compose} from 'recompose';
import {bindActionCreators} from 'redux';
import {SelectionResultActionsDropdown} from '../../components/actions-dropdown/SelectionResultActionsDropdown';
import {componentOrNull} from '../../components/hoc/hocs';
import {withContent} from '../../components/hoc/withContent';
import {withEmptyContent, WithEmptyContentProps} from '../../components/hoc/withEmptyContent';
import {superAdminOnly} from '../../components/hoc/withRoles';
import {Column} from '../../components/layouts/column/Column';
import {Loader} from '../../components/loading/Loader';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {isSelectionPage} from '../../selectors/routerSelectors';
import {firstUpperTranslated} from '../../services/translationService';
import {clearErrorMeters, fetchMeters} from '../../state/domain-models-paginated/meter/meterApiActions';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {
  getPageError,
  getPageIsFetching,
  getPageResult,
  getPaginatedEntities,
} from '../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {isSuperAdmin} from '../../state/domain-models/user/userSelectors';
import {changePage} from '../../state/ui/pagination/paginationActions';
import {EntityTypes, OnChangePage, Pagination} from '../../state/ui/pagination/paginationModels';
import {getPagination} from '../../state/ui/pagination/paginationSelectors';
import {getPaginatedMeterParameters} from '../../state/user-selection/userSelectionSelectors';
import {
  ClearErrorPaginated,
  Clickable,
  EncodedUriParameters,
  ErrorResponse,
  FetchPaginated,
  HasContent,
  OnClickWithId,
  uuid,
} from '../../types/Types';
import {selectEntryAdd} from '../../usecases/report/reportActions';
import {MeterList} from '../../usecases/validation/components/MeterList';
import {syncAllMeters, syncWithMetering} from '../../usecases/validation/validationActions';

interface SelectionPage {
  isSelectionPage: boolean;
}

interface StateToProps extends SelectionPage {
  result: uuid[];
  entities: ObjectsById<Meter>;
  isFetching: boolean;
  parameters: EncodedUriParameters;
  pagination: Pagination;
  error: Maybe<ErrorResponse>;
  entityType: EntityTypes;
  isSuperAdmin: boolean;
}

interface DispatchToProps {
  selectEntryAdd: OnClickWithId;
  syncWithMetering: OnClickWithId;
  syncAllMeters: (ids: uuid[]) => void;
  fetchMeters: FetchPaginated;
  changePage: OnChangePage;
  clearError: ClearErrorPaginated;
}

interface OwnProps {
  componentId: string;
}

export type MeterListProps = StateToProps & DispatchToProps & OwnProps;

const MeterListWrapper = withEmptyContent<MeterListProps & WithEmptyContentProps>(MeterList);

const selectionPageEnhancer = componentOrNull<SelectionPage>(({isSelectionPage}: SelectionPage) => isSelectionPage);

const enhance = compose<Clickable, Clickable & HasContent & SelectionPage>(
  selectionPageEnhancer,
  superAdminOnly,
  withContent,
);

const SuperAdminSelectionResultActionsDropdown = enhance(SelectionResultActionsDropdown);

class MeterListComponent extends React.Component<MeterListProps> {

  componentDidMount() {
    const {fetchMeters, parameters, pagination: {page}} = this.props;
    fetchMeters(page, parameters);
  }

  componentWillReceiveProps({fetchMeters, parameters, pagination: {page}}: MeterListProps) {
    fetchMeters(page, parameters);
  }

  render() {
    const {result, isFetching, isSelectionPage, error} = this.props;
    const {children, ...otherProps} = this.props;
    const hasContent = result.length > 0;
    const wrapperProps: MeterListProps & WithEmptyContentProps = {
      ...otherProps,
      noContentText: firstUpperTranslated('no meters'),
      hasContent,
    };

    return (
      <Loader isFetching={isFetching} error={error} clearError={this.clearError}>
        <Column>
          <SuperAdminSelectionResultActionsDropdown
            onClick={this.syncAllMeters}
            hasContent={hasContent}
            isSelectionPage={isSelectionPage}
          />
          <MeterListWrapper {...wrapperProps}/>
        </Column>
      </Loader>
    );
  }

  clearError = () => this.props.clearError({page: this.props.pagination.page});

  syncAllMeters = () => this.props.syncAllMeters(this.props.result);
}

const mapStateToProps = (
  {
    auth: {user},
    userSelection: {userSelection},
    paginatedDomainModels: {meters},
    routing,
    ui: {pagination},
    search: {validation: {query}},
  }: RootState,
  {componentId}: OwnProps,
): StateToProps => {
  const entityType: EntityTypes = 'meters';
  const paginationData: Pagination = getPagination({componentId, entityType, pagination});
  const selectionPage = isSelectionPage(routing);
  const {page} = paginationData;

  return ({
    entities: getPaginatedEntities<Meter>(meters),
    result: getPageResult(meters, page),
    parameters: getPaginatedMeterParameters({
      pagination: paginationData,
      userSelection,
      query: selectionPage ? undefined : query,
    }),
    isFetching: getPageIsFetching(meters, page),
    isSuperAdmin: isSuperAdmin(user!),
    isSelectionPage: selectionPage,
    pagination: paginationData,
    error: getPageError<Meter>(meters, page),
    entityType,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectEntryAdd,
  syncWithMetering,
  syncAllMeters,
  fetchMeters,
  changePage,
  clearError: clearErrorMeters,
}, dispatch);

export const MeterListContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(MeterListComponent);
