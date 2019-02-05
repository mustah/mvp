import {Grid, GridColumn} from '@progress/kendo-react-grid';
import {toArray} from 'lodash';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {ListActionsDropdown} from '../../components/actions-dropdown/ListActionsDropdown';
import {WrappedDateTime} from '../../components/dates/WrappedDateTime';
import {withEmptyContent, WithEmptyContentProps} from '../../components/hoc/withEmptyContent';
import {Row} from '../../components/layouts/row/Row';
import {ColoredEvent, Status} from '../../components/status/Status';
import {Table, TableColumn} from '../../components/table/Table';
import '../../components/table/Table.scss';
import {TableHead} from '../../components/table/TableHead';
import {Tab} from '../../components/tabs/components/Tab';
import {TabContent} from '../../components/tabs/components/TabContent';
import {TabHeaders} from '../../components/tabs/components/TabHeaders';
import {Tabs} from '../../components/tabs/components/Tabs';
import {TabSettings} from '../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../components/tabs/components/TabTopBar';
import {TimestampInfoMessage} from '../../components/timestamp-info-message/TimestampInfoMessage';
import {Maybe} from '../../helpers/Maybe';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Gateway, GatewayMandatory} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {EventLogType} from '../../state/domain-models-paginated/meter/meterModels';
import {eventsDataFormatter} from '../../state/domain-models-paginated/meter/meterSchema';
import {DomainModel} from '../../state/domain-models/domainModels';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {OnClickWithId} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {OnLogout} from '../../usecases/auth/authModels';
import {Map as MapComponent} from '../../usecases/map/components/Map';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {MapMarker} from '../../usecases/map/mapModels';
import {MeterMeasurementsContainer} from './MeterMeasurements';

export interface MeterDetailsState {
  selectedTab: TabName;
}

interface MeterGatewayProps {
  gateways: DomainModel<GatewayMandatory>;
}

interface MapProps {
  meter: MeterDetails;
  meterMapMarker: Maybe<MapMarker>;
}

interface OwnProps extends MapProps {
  selectEntryAdd: OnClickWithId;
  syncWithMetering: OnClickWithId;
}

interface DispatchToProps {
  logout: OnLogout;
}

type Props = OwnProps & DispatchToProps;

const renderEvent = ({dataItem: {name, type}}) => {
  const content = type === EventLogType.newMeter
    ? <ColoredEvent label={translate('new meter: {{name}}', {name})} type={type}/>
    : <Status label={name}/>;
  return <td>{content}</td>;
};

const renderDate = ({dataItem: {start}}) =>
  <td><WrappedDateTime date={start} hasContent={!!start}/></td>;

const renderSerial = ({serial}: Gateway): string => serial;

const renderProductModel = ({productModel}: Gateway): string => productModel;

const renderIp = ({ip}: Gateway): string => ip;

const renderPhoneNumber = ({phoneNumber}: Gateway): string => phoneNumber;

const renderStatus = ({status: {name}}: Gateway) => <Status label={name}/>;

const renderStatusChange = ({statusChanged}: Gateway) =>
  <WrappedDateTime date={statusChanged} hasContent={!!statusChanged}/>;

const MapContent = ({meter, meterMapMarker}: MapProps) => (
  <MapComponent height={400} viewCenter={meter.location.position}>
    {meterMapMarker.isJust() && <ClusterContainer markers={meterMapMarker.get()}/>}
  </MapComponent>
);

const GatewayContent = ({gateways}: MeterGatewayProps) => (
  <Row>
    <Table result={gateways.result} entities={gateways.entities}>
      <TableColumn
        header={<TableHead>{translate('gateway serial')}</TableHead>}
        renderCell={renderSerial}
      />
      <TableColumn
        header={<TableHead>{translate('product model')}</TableHead>}
        renderCell={renderProductModel}
      />
      <TableColumn
        header={<TableHead>{translate('ip')}</TableHead>}
        renderCell={renderIp}
      />
      <TableColumn
        header={<TableHead>{translate('phone number')}</TableHead>}
        renderCell={renderPhoneNumber}
      />
      <TableColumn
        header={<TableHead>{translate('collection')}</TableHead>}
        renderCell={renderStatus}
      />
      <TableColumn
        header={<TableHead>{translate('status change')}</TableHead>}
        renderCell={renderStatusChange}
      />
    </Table>
  </Row>
);

export const initialMeterDetailsState: MeterDetailsState = {
  selectedTab: TabName.values,
};

const ConnectedGatewaysWrapper = withEmptyContent<MeterGatewayProps & WithEmptyContentProps>(GatewayContent);

const MapContentWrapper = withEmptyContent<MapProps & WithEmptyContentProps>(MapContent);

class MeterDetailsTabs extends React.Component<Props, MeterDetailsState> {

  constructor(props) {
    super(props);
    this.state = {...initialMeterDetailsState};
  }

  render() {
    const {selectedTab} = this.state;
    const {meter, meterMapMarker, selectEntryAdd, syncWithMetering} = this.props;

    const {gateway} = meter;
    const gateways: DomainModel<GatewayMandatory> =
      gateway ?
        {
          entities: {[gateway.id]: gateway},
          result: [gateway.id],
        } :
        {
          entities: {},
          result: [],
        };

    const eventLog = eventsDataFormatter(meter);

    const mapWrapperProps: MapProps & WithEmptyContentProps = {
      meter,
      meterMapMarker,
      noContentText: firstUpperTranslated('no reliable position'),
      hasContent: meterMapMarker
        .filter(({status}: MapMarker) => status !== undefined)
        .isJust(),
    };

    const connectedGatewaysWrapperProps: MeterGatewayProps & WithEmptyContentProps = {
      gateways,
      noContentText: firstUpperTranslated('no gateway connected'),
      hasContent: gateways.result.length > 0
    };

    return (
      <Row>
        <Tabs className="MeterDetailsTabs full-width first-letter">
          <TabTopBar>
            <TabHeaders selectedTab={selectedTab} onChangeTab={this.changeTab}>
              <Tab tab={TabName.values} title={translate('measurements')}/>
              <Tab tab={TabName.log} title={translate('events')}/>
              <Tab tab={TabName.map} title={translate('map')}/>
              <Tab tab={TabName.connectedGateways} title={translate('gateways')}/>
            </TabHeaders>
            <TabSettings>
              <ListActionsDropdown
                item={{id: meter.id, name: meter.manufacturer}}
                selectEntryAdd={selectEntryAdd}
                syncWithMetering={syncWithMetering}
              />
            </TabSettings>
          </TabTopBar>
          <TabContent tab={TabName.values} selectedTab={selectedTab}>
            <MeterMeasurementsContainer meter={meter}/>
          </TabContent>
          <TabContent tab={TabName.log} selectedTab={selectedTab}>
            <Grid
              data={toArray(eventLog.entities)}
              scrollable="none"
            >
              <GridColumn title={translate('date')} cell={renderDate}/>
              <GridColumn title={translate('event')} cell={renderEvent}/>
            </Grid>
            <TimestampInfoMessage/>
          </TabContent>
          <TabContent tab={TabName.map} selectedTab={selectedTab}>
            {selectedTab === TabName.map && <MapContentWrapper {...mapWrapperProps}/>}
          </TabContent>
          <TabContent tab={TabName.connectedGateways} selectedTab={selectedTab}>
            <ConnectedGatewaysWrapper {...connectedGatewaysWrapperProps}/>
          </TabContent>
        </Tabs>
      </Row>
    );
  }

  changeTab = (selectedTab: TabName) => this.setState({selectedTab});
}

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  logout,
}, dispatch);

export const MeterDetailsTabsContainer = connect<{}, DispatchToProps, OwnProps>(
  mapDispatchToProps,
)(MeterDetailsTabs);
