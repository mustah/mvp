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
import {now} from '../../helpers/dateHelpers';
import {roundCollectionPercentage} from '../../helpers/formatters';
import {Maybe} from '../../helpers/Maybe';
import {locationNameTranslation} from '../../helpers/translations';
import {RootState} from '../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {clearErrorMeters, fetchMeters} from '../../state/domain-models-paginated/meter/meterApiActions';
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
  Fetch,
  FetchPaginated,
  OnClickWithId,
  uuid,
} from '../../types/Types';
import {fetchMeterMapMarkers} from '../../usecases/map/meterMapMarkerApiActions';
import {selectEntryAdd} from '../../usecases/report/reportActions';

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
  fetchMeters: FetchPaginated;
  fetchMeterMapMarkers: Fetch;
  changePaginationPage: OnChangePage;
  clearError: ClearErrorPaginated;
}

interface OwnProps {
  componentId: string;
}

type Props = StateToProps & DispatchToProps & OwnProps;

class MeterList extends React.Component<Props> {

  componentDidMount() {
    const {fetchMeters, fetchMeterMapMarkers, parameters, pagination: {page}} = this.props;
    fetchMeters(page, parameters);
    fetchMeterMapMarkers(parameters);
  }

  componentWillReceiveProps({fetchMeters, fetchMeterMapMarkers, parameters, pagination: {page}}: Props) {
    fetchMeters(page, parameters);
    fetchMeterMapMarkers(parameters);
  }

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

    const renderMeterListItem = (meter: Meter) => <MeterListItem meter={meter}/>;
    const renderStatusCell = ({status: {name}}: Meter) => <Status name={name}/>;
    const renderCityName = ({location: {city}}: Meter) => locationNameTranslation(city.name);
    const renderAddressName = ({location: {address}}: Meter) => locationNameTranslation(address.name);
    const renderActionDropdown = ({id, manufacturer}: Meter) =>
      <ListActionsDropdown item={{id, name: manufacturer}} selectEntryAdd={selectEntryAdd}/>;
    const renderGatewaySerial = ({gateway: {serial}}: Meter) => serial;
    const renderManufacturer = ({manufacturer}: Meter) => manufacturer;
    const renderStatusChanged = ({statusChanged}: Meter) => statusChanged || <Separator/>;
    const renderMedium = ({medium}: Meter) => medium;
    const renderCollectionStatus = ({collectionStatus, readIntervalMinutes}: Meter) =>
      readIntervalMinutes === 0 ? '-' : roundCollectionPercentage(collectionStatus);

    const collectionPercentageHeader = (
      <TableHead
        className="number"
      >
        {translate('collection percentage')}
      </TableHead>
    );

    const changePage = (page: number) => changePaginationPage({
      entityType,
      componentId,
      page,
    });

    return (
      <Loader isFetching={isFetching} error={error} clearError={this.clearError}>
        <HasContent
          hasContent={result.length > 0}
          fallbackContent={<MissingDataTitle title={firstUpperTranslated('no meters')}/>}
        >
          <div>
            <Table result={result} entities={entities}>
              <TableColumn
                header={<TableHead className="first">{translate('facility')}</TableHead>}
                renderCell={renderMeterListItem}
              />
              <TableColumn
                header={<TableHead>{translate('city')}</TableHead>}
                cellClassName={'first-uppercase'}
                renderCell={renderCityName}
              />
              <TableColumn
                header={<TableHead>{translate('address')}</TableHead>}
                cellClassName={'first-uppercase'}
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
                renderCell={renderGatewaySerial}
              />
              <TableColumn
                cellClassName="number"
                header={collectionPercentageHeader}
                renderCell={renderCollectionStatus}
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

  clearError = () => this.props.clearError({page: this.props.pagination.page});
}

const mapStateToProps = (
  {userSelection: {userSelection}, paginatedDomainModels: {meters}, ui: {pagination}}: RootState,
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
  fetchMeters,
  fetchMeterMapMarkers,
  changePaginationPage,
  clearError: clearErrorMeters,
}, dispatch);

export const MeterListContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeterList);
