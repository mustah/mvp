import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {ListActionsDropdown} from '../../../components/actions-dropdown/ListActionsDropdown';
import {HasContent} from '../../../components/content/HasContent';
import {Loader} from '../../../components/loading/Loader';
import {PaginationControl} from '../../../components/pagination-control/PaginationControl';
import {Separator} from '../../../components/separators/Separator';
import {Status} from '../../../components/status/Status';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {MissingDataTitle} from '../../../components/texts/Titles';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {clearErrorGateways, fetchGateways} from '../../../state/domain-models-paginated/gateway/gatewayApiActions';
import {Gateway} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {
  getPageError,
  getPageIsFetching,
  getPageResult,
  getPaginatedEntities,
} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getPaginatedGatewayParameters} from '../../../state/user-selection/userSelectionSelectors';
import {changePaginationPage} from '../../../state/ui/pagination/paginationActions';
import {EntityTypes, OnChangePage, Pagination} from '../../../state/ui/pagination/paginationModels';
import {getPagination} from '../../../state/ui/pagination/paginationSelectors';
import {ClearErrorPaginated, ErrorResponse, FetchPaginated, OnClickWithId, uuid} from '../../../types/Types';
import {selectEntryAdd} from '../../report/reportActions';
import {GatewayListItem} from './GatewayListItem';

interface StateToProps {
  result: uuid[];
  entities: ObjectsById<Gateway>;
  isFetching: boolean;
  encodedUriParametersForGateways: string;
  pagination: Pagination;
  error: Maybe<ErrorResponse>;
  entityType: EntityTypes;
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

type Props = StateToProps & DispatchToProps & OwnProps;

class GatewayList extends React.Component<Props> {

  componentDidMount() {
    const {fetchGateways, encodedUriParametersForGateways, pagination: {page}} = this.props;
    fetchGateways(page, encodedUriParametersForGateways);
  }

  componentWillReceiveProps({fetchGateways, encodedUriParametersForGateways, pagination: {page}}: Props) {
    fetchGateways(page, encodedUriParametersForGateways);
  }

  clearError = () => this.props.clearError({page: this.props.pagination.page});

  render() {
    const {
      result,
      entities,
      selectEntryAdd,
      isFetching,
      pagination,
      changePaginationPage,
      componentId,
      error,
      entityType,
    } = this.props;

    const renderGatewayListItem = (gateway: Gateway) => <GatewayListItem gateway={gateway}/>;
    const renderStatusCell = ({status: {name}}: Gateway) => <Status name={name}/>;
    const renderCity = ({location: {city}}: Gateway) => city.name;
    const renderAddress = ({location: {address}}: Gateway) => address.name;
    const renderActionDropdown = ({id, productModel}: Gateway) =>
      <ListActionsDropdown item={{id, name: productModel}} selectEntryAdd={selectEntryAdd}/>;
    const renderStatusChanged = ({statusChanged}: Gateway) => statusChanged || <Separator/>;
    const renderProductModel = ({productModel}: Gateway) => productModel;

    const changePage = (page: number) => changePaginationPage({
      entityType,
      componentId,
      page,
    });

    return (
      <Loader isFetching={isFetching} error={error} clearError={this.clearError}>
        <HasContent
          hasContent={result.length > 0}
          fallbackContent={<MissingDataTitle title={firstUpperTranslated('no gateways')}/>}
        >
          <div>
            <Table result={result} entities={entities}>
              <TableColumn
                header={<TableHead className="first">{translate('gateway')}</TableHead>}
                renderCell={renderGatewayListItem}
              />
              <TableColumn
                header={<TableHead>{translate('city')}</TableHead>}
                cellClassName={'first-uppercase'}
                renderCell={renderCity}
              />
              <TableColumn
                header={<TableHead>{translate('address')}</TableHead>}
                cellClassName={'first-uppercase'}
                renderCell={renderAddress}
              />
              <TableColumn
                header={<TableHead>{translate('product model')}</TableHead>}
                renderCell={renderProductModel}
              />
              <TableColumn
                header={<TableHead className="TableHead-status">{translate('status')}</TableHead>}
                renderCell={renderStatusCell}
              />
              <TableColumn
                header={<TableHead>{translate('status change')}</TableHead>}
                renderCell={renderStatusChanged}
              />
              <TableColumn
                header={<TableHead className="actionDropdown">{' '}</TableHead>}
                renderCell={renderActionDropdown}
              />
            </Table>
            <PaginationControl pagination={pagination} changePage={changePage}/>
          </div>
        </HasContent>
      </Loader>
    );
  }
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
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(GatewayList);
