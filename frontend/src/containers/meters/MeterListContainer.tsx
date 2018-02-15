import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {ListActionsDropdown} from '../../components/actions-dropdown/ListActionsDropdown';
import {Loader} from '../../components/loading/Loader';
import {MeterListItem} from '../../components/meters/MeterListItem';
import {PaginationControl} from '../../components/pagination-control/PaginationControl';
import {Separator} from '../../components/separators/Separator';
import {Status} from '../../components/status/Status';
import {Table, TableColumn} from '../../components/table/Table';
import {TableHead} from '../../components/table/TableHead';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {translate} from '../../services/translationService';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {HasPageNumber, RestGetPaginated} from '../../state/domain-models-paginated/paginatedDomainModels';
import {clearErrorMeters, fetchMeters} from '../../state/domain-models-paginated/paginatedDomainModelsActions';
import {
  getPageError,
  getPageIsFetching,
  getPageResult,
  getPaginatedEntities,
} from '../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {Flag} from '../../state/domain-models/flag/flagModels';
import {
  getEncodedUriParametersForMeters,
  UriLookupStatePaginated,
} from '../../state/search/selection/selectionSelectors';
import {paginationChangePage} from '../../state/ui/pagination/paginationActions';
import {OnChangePage, Pagination} from '../../state/ui/pagination/paginationModels';
import {getPagination} from '../../state/ui/pagination/paginationSelectors';
import {ErrorResponse, OnClickWithId, uuid} from '../../types/Types';
import {selectEntryAdd} from '../../usecases/report/reportActions';

interface StateToProps {
  result: uuid[];
  entities: ObjectsById<Meter>;
  isFetching: boolean;
  encodedUriParametersForMeters: string;
  pagination: Pagination;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps {
  selectEntryAdd: OnClickWithId;
  fetchMeters: RestGetPaginated;
  paginationChangePage: OnChangePage;
  clearError: (payload: HasPageNumber) => void;
}

interface OwnProps {
  componentId: string;
}

type Props = StateToProps & DispatchToProps & OwnProps;

class MeterList extends React.Component<Props> {

  componentDidMount() {
    const {fetchMeters, encodedUriParametersForMeters, pagination: {page}} = this.props;
    fetchMeters(page, encodedUriParametersForMeters);
  }

  componentWillReceiveProps({fetchMeters, encodedUriParametersForMeters, pagination: {page}}: Props) {
    fetchMeters(page, encodedUriParametersForMeters);
  }

  clearError = () => {
    this.props.clearError({page: this.props.pagination.page});
  }

  render() {
    const {
      result,
      entities,
      selectEntryAdd,
      isFetching,
      pagination,
      paginationChangePage,
      componentId,
      error,
    } = this.props;

    const renderMeterListItem = (meter: Meter) => <MeterListItem meter={meter}/>;
    const renderStatusCell = ({status}: Meter) => status ? <Status {...status}/> : <Status id={0} name={'ok'}/>;
    const renderCityName = ({city}: Meter) => city ? city.name : 'NAME';
    const renderAddressName = ({address}: Meter) => address ? address.name : 'NAME';
    const renderFlags = ({flags}: Meter) => flags ? flags.map((flag: Flag) => flag.title).join(', ') : 'FLAGS';
    const renderActionDropdown = ({id, manufacturer}: Meter) =>
      <ListActionsDropdown item={{id, name: manufacturer}} selectEntryAdd={selectEntryAdd}/>;
    const renderGatewayId = ({gatewayId}: Meter) => gatewayId;
    const renderManufacturer = ({manufacturer}: Meter) => manufacturer;
    const renderStatusChanged = ({statusChanged}: Meter) => statusChanged ||
      <Separator/>;
    const renderMedium = ({medium}: Meter) => medium;

    const changePage = (page: number) => paginationChangePage({
      entityType: 'meters',
      componentId,
      page,
    });

    // TODO: Add pagination control
    return (
      <Loader isFetching={isFetching} error={error} clearError={this.clearError}>
        <div>
          <Table result={result} entities={entities}>
            <TableColumn
              header={<TableHead className="first">{translate('facility')}</TableHead>}
              renderCell={renderMeterListItem}
            />
            <TableColumn
              header={<TableHead>{translate('city')}</TableHead>}
              renderCell={renderCityName}
            />
            <TableColumn
              header={<TableHead>{translate('address')}</TableHead>}
              renderCell={renderAddressName}
            />
            <TableColumn
              header={<TableHead>{translate('manufacturer')}</TableHead>}
              renderCell={renderManufacturer}
            />
            <TableColumn
              header={<TableHead>{translate('medium')}</TableHead>}
              renderCell={renderMedium}
            />
            <TableColumn
              header={<TableHead>{translate('gateway')}</TableHead>}
              renderCell={renderGatewayId}
            />
            <TableColumn
              header={<TableHead className="TableHead-status">{translate('status')}</TableHead>}
              renderCell={renderStatusCell}
            />
            <TableColumn
              header={<TableHead sortable={true} currentSort="desc">{translate('status change')}</TableHead>}
              renderCell={renderStatusChanged}
            />
            <TableColumn
              header={<TableHead>{translate('flags')}</TableHead>}
              renderCell={renderFlags}
            />
            <TableColumn
              header={<TableHead className="actionDropdown">{' '}</TableHead>}
              renderCell={renderActionDropdown}
            />
          </Table>
          <PaginationControl pagination={pagination} changePage={changePage}/>
        </div>
      </Loader>
    );
  }
}

const mapStateToProps = (
  {
    searchParameters,
    paginatedDomainModels: {meters},
    ui: {pagination},
  }: RootState,
  {componentId}: OwnProps,
): StateToProps => {

  const uriLookupState: UriLookupStatePaginated = {
    ...searchParameters,
    componentId,
    entityType: 'meters',
    pagination,
  };
  const paginationData: Pagination = getPagination(uriLookupState);

  return ({
    entities: getPaginatedEntities<Meter>(meters),
    result: getPageResult<Meter>(meters, paginationData.page),
    encodedUriParametersForMeters: getEncodedUriParametersForMeters(uriLookupState),
    isFetching: getPageIsFetching(meters, paginationData.page),
    pagination: paginationData,
    error: getPageError(meters, paginationData.page),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectEntryAdd,
  fetchMeters,
  paginationChangePage,
  clearError: clearErrorMeters,
}, dispatch);

export const MeterListContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeterList);
