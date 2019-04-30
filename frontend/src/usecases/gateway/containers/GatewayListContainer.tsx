import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {RetryLoader} from '../../../components/loading/Loader';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {clearErrorGateways, fetchGateways} from '../../../state/domain-models-paginated/gateway/gatewayApiActions';
import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {
  getPageError,
  getPageIsFetching,
  getPageResult,
  getPaginatedEntities,
} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {changePage} from '../../../state/ui/pagination/paginationActions';
import {EntityTypes, OnChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPagination} from '../../../state/ui/pagination/paginationSelectors';
import {getPaginatedGatewayParameters} from '../../../state/user-selection/userSelectionSelectors';
import {ClearErrorPaginated, ErrorResponse, FetchPaginated, uuid} from '../../../types/Types';
import {GatewayList} from '../components/GatewayList';

interface ListProps {
  entities: ObjectsById<Gateway>;
  entityType: EntityTypes;
  pagination: Pagination;
  result: uuid[];
}

interface StateToProps extends ListProps {
  parameters: string;
  error: Maybe<ErrorResponse>;
  isFetching: boolean;
}

interface DispatchToProps {
  fetchGateways: FetchPaginated;
  changePage: OnChangePage;
  clearError: ClearErrorPaginated;
}

export type GatewayListProps = ListProps & DispatchToProps;

type Props = StateToProps & DispatchToProps;

const GatewayListWrapper = withEmptyContent<GatewayListProps & WithEmptyContentProps>(GatewayList);

class GatewayListComponent extends React.Component<Props> {

  componentDidMount() {
    const {fetchGateways, parameters, pagination: {page}} = this.props;
    fetchGateways(page, parameters);
  }

  componentWillReceiveProps({fetchGateways, parameters, pagination: {page}}: Props) {
    fetchGateways(page, parameters);
  }

  render() {
    const {
      children,
      parameters,
      isFetching,
      error,
      result,
      ...otherProps
    } = this.props;

    const wrapperProps: GatewayListProps & WithEmptyContentProps = {
      ...otherProps,
      result,
      noContentText: firstUpperTranslated('no gateways'),
      hasContent: result.length > 0,
    };

    return (
      <RetryLoader isFetching={isFetching} error={error} clearError={this.clearError}>
        <GatewayListWrapper {...wrapperProps}/>
      </RetryLoader>
    );
  }

  clearError = () => this.props.clearError({page: this.props.pagination.page});

}

const mapStateToProps = (
  {
    userSelection,
    paginatedDomainModels: {gateways},
    ui: {pagination}
  }: RootState,
): StateToProps => {
  const entityType: EntityTypes = 'gateways';
  const paginationData: Pagination = getPagination({entityType, pagination});
  const {page} = paginationData;

  return ({
    entities: getPaginatedEntities<Gateway>(gateways),
    result: getPageResult(gateways, page),
    parameters: getPaginatedGatewayParameters({pagination: paginationData, ...userSelection}),
    isFetching: getPageIsFetching(gateways, page),
    pagination: paginationData,
    error: getPageError<Gateway>(gateways, page),
    entityType,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchGateways,
  changePage,
  clearError: clearErrorGateways,
}, dispatch);

export const GatewayListContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(GatewayListComponent);
