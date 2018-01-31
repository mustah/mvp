import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {ListActionsDropdown} from '../../components/actions-dropdown/ListActionsDropdown';
import {Loader} from '../../components/loading/Loader';
import {MeterListItem} from '../../components/meters/MeterListItem';
import {Separator} from '../../components/separators/Separator';
import {Status} from '../../components/status/Status';
import {Table, TableColumn} from '../../components/table/Table';
import {TableHead} from '../../components/table/TableHead';
import {RootState} from '../../reducers/rootReducer';
import {translate} from '../../services/translationService';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {Flag} from '../../state/domain-models/flag/flagModels';
import {Meter} from '../../state/domain-models/meter/meterModels';
import {getMeterEntities, getMeterResult, getMetersIsFetching} from '../../state/domain-models/meter/meterSelectors';
import {fetchMeters} from '../../state/domain-models/paginatedDomainModelsActions';
import {getEncodedUriParametersForMeters} from '../../state/search/selection/selectionSelectors';
import {OnClickWithId, uuid} from '../../types/Types';
import {selectEntryAdd} from '../../usecases/report/reportActions';

interface StateToProps {
  result: uuid[];
  entities: ObjectsById<Meter>;
  isFetching: boolean;
  encodedUriParametersForMeters: string;
}

interface DispatchToProps {
  selectEntryAdd: OnClickWithId;
  fetchMeters: (component: uuid, encodedUriParameters: string) => void;
}

type Props = StateToProps & DispatchToProps;

const componentId = 'validationMeterList';

class MeterList extends React.Component<Props> {

  componentDidMount() {
    const {fetchMeters, encodedUriParametersForMeters} = this.props;
    fetchMeters(componentId, encodedUriParametersForMeters);
  }

  render() {
    const {result, entities, selectEntryAdd, isFetching} = this.props;

    const renderMeterListItem = (meter: Meter) => <MeterListItem meter={meter}/>;
    const renderStatusCell = ({status}: Meter) => <Status {...status}/>;
    const renderCityName = ({city}: Meter) => city.name;
    const renderAddressName = ({address}: Meter) => address.name;
    const renderFlags = ({flags}: Meter) => flags.map((flag: Flag) => flag.title).join(', ');
    const renderActionDropdown = ({id, manufacturer}: Meter) =>
      <ListActionsDropdown item={{id, name: manufacturer}} selectEntryAdd={selectEntryAdd}/>;
    const renderGatewayId = ({gatewayId}: Meter) => gatewayId;
    const renderManufacturer = ({manufacturer}: Meter) => manufacturer;
    const renderStatusChanged = ({statusChanged}: Meter) => statusChanged || <Separator/>;
    const renderMedium = ({medium}: Meter) => medium;
// TODO: Add pagination control
    return (
      <Loader isFetching={isFetching}>
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
      </Loader>
    );
  }
}

const mapStateToProps = ({searchParameters, domainModels: {paginatedMeters}}: RootState): StateToProps => ({
  entities: getMeterEntities({...paginatedMeters, componentId}),
  result: getMeterResult({...paginatedMeters, componentId}),
  encodedUriParametersForMeters: getEncodedUriParametersForMeters(searchParameters),
  isFetching: getMetersIsFetching({...paginatedMeters, componentId}),
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  selectEntryAdd,
  fetchMeters,
}, dispatch);

export const MeterListContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MeterList);
