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
import {clearErrorMeters, fetchMeters} from '../../state/domain-models-paginated/meter/meterApiActions';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {
  getPageError,
  getPageIsFetching,
  getPageResult,
  getPaginatedEntities,
} from '../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {Flag} from '../../state/domain-models/flag/flagModels';
import {getPaginatedMeterParameters} from '../../state/search/selection/selectionSelectors';
import {changePaginationPage} from '../../state/ui/pagination/paginationActions';
import {EntityTypes, OnChangePage, Pagination} from '../../state/ui/pagination/paginationModels';
import {getPagination} from '../../state/ui/pagination/paginationSelectors';
import {
  ClearErrorPaginated,
  EncodedUriParameters,
  ErrorResponse,
  FetchPaginated,
  OnClickWithId,
  uuid,
} from '../../types/Types';
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
  changePaginationPage: OnChangePage;
  clearError: ClearErrorPaginated;
}

interface OwnProps {
  componentId: string;
}

type Props = StateToProps & DispatchToProps & OwnProps;

class MeterList extends React.Component<Props> {

  componentDidMount() {
    const {fetchMeters, parameters, pagination: {page}} = this.props;
    fetchMeters(page, parameters);
  }

  componentWillReceiveProps({fetchMeters, parameters, pagination: {page}}: Props) {
    fetchMeters(page, parameters);
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

    const renderMeterListItem = (meter: Meter) => <MeterListItem meter={meter}/>;
    const renderStatusCell = ({status: {name}}: Meter) => <Status name={name}/>;
    const renderCityName = ({location: {city}}: Meter) => city ? city.name : null;
    const renderAddressName = ({location: {address}}: Meter) => address ? address.name : null;
    const renderFlags = ({flags}: Meter) => flags
      ? flags.map((flag: Flag) => flag.title).join(', ')
      : null;
    const renderActionDropdown = ({id, manufacturer}: Meter) =>
      <ListActionsDropdown item={{id, name: manufacturer}} selectEntryAdd={selectEntryAdd}/>;
    const renderGatewaySerial = ({gateway: {serial}}: Meter) => serial;
    const renderManufacturer = ({manufacturer}: Meter) => manufacturer;
    const renderStatusChanged = ({statusChanged}: Meter) => statusChanged || <Separator/>;
    const renderMedium = ({medium}: Meter) => medium;

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
                renderCell={renderGatewaySerial}
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
  {searchParameters, paginatedDomainModels: {meters}, ui: {pagination}}: RootState,
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
      ...searchParameters,
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
  changePaginationPage,
  clearError: clearErrorMeters,
}, dispatch);

export const MeterListContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeterList);
