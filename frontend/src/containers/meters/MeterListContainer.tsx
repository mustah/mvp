import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withEmptyContent, WithEmptyContentProps} from '../../components/hoc/withEmptyContent';
import {Loader} from '../../components/loading/Loader';
import {now} from '../../helpers/dateHelpers';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {firstUpperTranslated} from '../../services/translationService';
import {
  clearErrorMeters,
  fetchMeters,
} from '../../state/domain-models-paginated/meter/meterApiActions';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {
  getPageError,
  getPageIsFetching,
  getPageResult,
  getPaginatedEntities,
} from '../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {changePaginationPage} from '../../state/ui/pagination/paginationActions';
import {EntityTypes, OnChangePage, Pagination} from '../../state/ui/pagination/paginationModels';
import {getPagination} from '../../state/ui/pagination/paginationSelectors';
import {getPaginatedMeterParameters} from '../../state/user-selection/userSelectionSelectors';
import {
  ClearErrorPaginated,
  EncodedUriParameters,
  ErrorResponse,
  FetchPaginated,
  OnClickWithId,
  uuid,
} from '../../types/Types';
import {selectEntryAdd} from '../../usecases/report/reportActions';
import {syncWithMetering} from '../../usecases/validation/validationActions';
import {MeterList} from './MeterList';

interface StateToProps {
  result: uuid[];
  entities: ObjectsById<Meter>;
  isFetching: boolean;
  parameters: EncodedUriParameters;
  pagination: Pagination;
  error: Maybe<ErrorResponse>;
  entityType: EntityTypes;
}

interface DispatchToProps {
  selectEntryAdd: OnClickWithId;
  syncWithMetering: OnClickWithId;
  fetchMeters: FetchPaginated;
  changePaginationPage: OnChangePage;
  clearError: ClearErrorPaginated;
}

interface OwnProps {
  componentId: string;
}

export type MeterListProps = StateToProps & DispatchToProps & OwnProps;

const MeterListWrapper = withEmptyContent<MeterListProps & WithEmptyContentProps>(MeterList);

class MeterListComponent extends React.Component<MeterListProps> {

  componentDidMount() {
    const {fetchMeters, parameters, pagination: {page}} = this.props;
    fetchMeters(page, parameters);
  }

  componentWillReceiveProps({fetchMeters, parameters, pagination: {page}}: MeterListProps) {
    fetchMeters(page, parameters);
  }

  render() {
    const {result, isFetching, error} = this.props;
    const {children, ...otherProps} = this.props;
    const wrapperProps: MeterListProps & WithEmptyContentProps = {
      ...otherProps,
      noContentText: firstUpperTranslated('no meters'),
      hasContent: result.length > 0,
    };

    return (
      <Loader isFetching={isFetching} error={error} clearError={this.clearError}>
        <MeterListWrapper {...wrapperProps}/>
      </Loader>
    );
  }

  clearError = () => this.props.clearError({page: this.props.pagination.page});
}

const mapStateToProps = (
  {
    userSelection: {userSelection},
    paginatedDomainModels: {meters},
    ui: {pagination},
  }: RootState,
  {componentId}: OwnProps,
): StateToProps => {

  const entityType: EntityTypes = 'meters';
  const paginationData: Pagination = getPagination({componentId, entityType, pagination});
  const {page} = paginationData;

  return ({
    entities: getPaginatedEntities<Meter>(meters),
    result: getPageResult(meters, page),
    parameters: getPaginatedMeterParameters({
      pagination: paginationData,
      userSelection,
      now: now(),
    }),
    isFetching: getPageIsFetching(meters, page),
    pagination: paginationData,
    error: getPageError<Meter>(meters, page),
    entityType,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectEntryAdd,
  syncWithMetering,
  fetchMeters,
  changePaginationPage,
  clearError: clearErrorMeters,
}, dispatch);

export const MeterListContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(MeterListComponent);
