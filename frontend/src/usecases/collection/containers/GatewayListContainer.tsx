import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {Loader} from '../../../components/loading/Loader';
import {now} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {
  clearErrorGateways,
  fetchGateways,
} from '../../../state/domain-models-paginated/gateway/gatewayApiActions';
import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {
  getPageError,
  getPageIsFetching,
  getPageResult,
  getPaginatedEntities,
} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {changePaginationPage} from '../../../state/ui/pagination/paginationActions';
import {EntityTypes, OnChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPagination} from '../../../state/ui/pagination/paginationSelectors';
import {getPaginatedGatewayParameters} from '../../../state/user-selection/userSelectionSelectors';
import {
  ClearErrorPaginated,
  ErrorResponse,
  FetchPaginated,
  OnClickWithId,
  uuid,
} from '../../../types/Types';
import {selectEntryAdd} from '../../report/reportActions';
import {GatewayList} from '../components/GatewayList';

interface ListProps {
  entities: ObjectsById<Gateway>;
  entityType: EntityTypes;
  pagination: Pagination;
  result: uuid[];
}

interface StateToProps extends ListProps {
  encodedUriParametersForGateways: string;
  error: Maybe<ErrorResponse>;
  isFetching: boolean;
}

interface DispatchToProps {
  selectEntryAdd: OnClickWithId;
  fetchGateways: FetchPaginated;
  changePaginationPage: OnChangePage;
  clearError: ClearErrorPaginated;
}

interface OwnProps {
  componentId: string;
}

export type GatewayListProps = ListProps & DispatchToProps & OwnProps;

type Props = StateToProps & DispatchToProps & OwnProps;

const GatewayListWrapper = withEmptyContent<GatewayListProps & WithEmptyContentProps>(GatewayList);

class GatewayListComponent extends React.Component<Props> {

  componentDidMount() {
    const {fetchGateways, encodedUriParametersForGateways, pagination: {page}} = this.props;
    fetchGateways(page, encodedUriParametersForGateways);
  }

  componentWillReceiveProps({fetchGateways, encodedUriParametersForGateways, pagination: {page}}: Props) {
    fetchGateways(page, encodedUriParametersForGateways);
  }

  render() {
    const {
      children,
      encodedUriParametersForGateways,
      isFetching,
      error,
      result,
      ...otherProps,
    } = this.props;

    const wrapperProps: GatewayListProps & WithEmptyContentProps = {
      ...otherProps,
      result,
      noContentText: firstUpperTranslated('no gateways'),
      hasContent: result.length > 0,
    };

    return (
      <Loader isFetching={isFetching} error={error} clearError={this.clearError}>
        <GatewayListWrapper {...wrapperProps}/>
      </Loader>
    );
  }

  clearError = () => this.props.clearError({page: this.props.pagination.page});

}

const mapStateToProps = (
  {userSelection, paginatedDomainModels: {gateways}, ui: {pagination}}: RootState,
  {componentId}: OwnProps,
): StateToProps => {
  const entityType: EntityTypes = 'gateways';
  const paginationData: Pagination = getPagination({componentId, entityType, pagination});
  const {page} = paginationData;

  return ({
    entities: getPaginatedEntities<Gateway>(gateways),
    result: getPageResult(gateways, page),
    encodedUriParametersForGateways: getPaginatedGatewayParameters({
      pagination: paginationData,
      ...userSelection,
      now: now(),
    }),
    isFetching: getPageIsFetching(gateways, page),
    pagination: paginationData,
    error: getPageError<Gateway>(gateways, page),
    entityType,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectEntryAdd,
  fetchGateways,
  changePaginationPage,
  clearError: clearErrorGateways,
}, dispatch);

export const GatewayListContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(GatewayListComponent);
