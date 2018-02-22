import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {ListActionsDropdown} from '../../components/actions-dropdown/ListActionsDropdown';
import {HasContent} from '../../components/content/HasContent';
import {Loader} from '../../components/loading/Loader';
import {MeterListItem} from '../../components/meters/MeterListItem';
import {PaginationControl} from '../../components/pagination-control/PaginationControl';
import {Separator} from '../../components/separators/Separator';
import {Status} from '../../components/status/Status';
import {Table, TableColumn} from '../../components/table/Table';
import {TableHead} from '../../components/table/TableHead';
import {MissingDataTitle} from '../../components/texts/Titles';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {ClearErrorPaginated} from '../../state/domain-models-paginated/paginatedDomainModels';
import {clearErrorMeters} from '../../state/domain-models-paginated/paginatedDomainModelsActions';
import {ObjectsById, RestGet} from '../../state/domain-models/domainModels';
import {fetchAllMeters} from '../../state/domain-models/domainModelsActions';
import {
  getEntitiesDomainModels,
  getError,
  getResultDomainModels,
} from '../../state/domain-models/domainModelsSelectors';
import {Flag} from '../../state/domain-models/flag/flagModels';
import {getEncodedUriParametersForAllMeters} from '../../state/search/selection/selectionSelectors';
import {changePaginationPage} from '../../state/ui/pagination/paginationActions';
import {OnChangePage, Pagination} from '../../state/ui/pagination/paginationModels';
import {getPagination, getPaginationList} from '../../state/ui/pagination/paginationSelectors';
import {ErrorResponse, OnClickWithId, uuid} from '../../types/Types';
import {selectEntryAdd} from '../../usecases/report/reportActions';

interface StateToProps {
  paginatedList: uuid[];
  entities: ObjectsById<Meter>;
  isFetching: boolean;
  encodedUriParametersForAllMeters: string;
  pagination: Pagination;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps {
  selectEntryAdd: OnClickWithId;
  fetchAllMeters: RestGet;
  changePaginationPage: OnChangePage;
  clearError: ClearErrorPaginated;
}

interface OwnProps {
  componentId: string;
}

type Props = StateToProps & DispatchToProps & OwnProps;

class MeterList extends React.Component<Props> {

  componentDidMount() {
    const {fetchAllMeters, encodedUriParametersForAllMeters} = this.props;
    fetchAllMeters(encodedUriParametersForAllMeters);
  }

  componentWillReceiveProps({fetchAllMeters, encodedUriParametersForAllMeters}: Props) {
    fetchAllMeters(encodedUriParametersForAllMeters);
  }

  clearError = () => {
    this.props.clearError({page: this.props.pagination.page});
  }

  render() {
    const {
      paginatedList,
      entities,
      selectEntryAdd,
      isFetching,
      pagination,
      changePaginationPage,
      componentId,
      error,
    } = this.props;

    const renderMeterListItem = (meter: Meter) => <MeterListItem meter={meter}/>;
    const renderStatusCell = ({status}: Meter) => status ? <Status {...status}/> : null;
    const renderCityName = ({city}: Meter) => city ? city.name : null;
    const renderAddressName = ({address}: Meter) => address ? address.name : null;
    const renderFlags = ({flags}: Meter) => flags ? flags.map((flag: Flag) => flag.title).join(', ') : null;
    const renderActionDropdown = ({id, manufacturer}: Meter) =>
      <ListActionsDropdown item={{id, name: manufacturer}} selectEntryAdd={selectEntryAdd}/>;
    const renderGatewayId = ({gatewayId}: Meter) => gatewayId;
    const renderManufacturer = ({manufacturer}: Meter) => manufacturer;
    const renderStatusChanged = ({statusChanged}: Meter) => statusChanged ||
      <Separator/>;
    const renderMedium = ({medium}: Meter) => medium;

    const changePage = (page: number) => changePaginationPage({
      entityType: 'meters',
      componentId,
      page,
    });
    return (
      <Loader isFetching={isFetching} error={error} clearError={this.clearError}>
        <HasContent
          hasContent={paginatedList.length > 0}
          fallbackContent={<MissingDataTitle title={firstUpperTranslated('no meters')}/>}
        >
          <div>
            <Table result={paginatedList} entities={entities}>
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
        </HasContent>
      </Loader>
    );
  }
}

const mapStateToProps = (
  {searchParameters, domainModels: {allMeters}, ui: {pagination}}: RootState,
  {componentId}: OwnProps,
): StateToProps => {

  const paginationData: Pagination = getPagination({componentId, entityType: 'allMeters', pagination});

  return ({
    entities: getEntitiesDomainModels<Meter>(allMeters),
    paginatedList: getPaginationList({
      ...paginationData,
      result: getResultDomainModels<Meter>(allMeters),
    }),
    encodedUriParametersForAllMeters: getEncodedUriParametersForAllMeters(searchParameters),
    isFetching: allMeters.isFetching,
    pagination: paginationData,
    error: getError<Meter>(allMeters),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectEntryAdd,
  fetchAllMeters,
  changePaginationPage,
  clearError: clearErrorMeters,
}, dispatch);

export const MeterListContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeterList);
