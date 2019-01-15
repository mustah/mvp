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
import {EventLog, EventLogType} from '../../state/domain-models-paginated/meter/meterModels';
import {eventsDataFormatter} from '../../state/domain-models-paginated/meter/meterSchema';
import {DomainModel} from '../../state/domain-models/domainModels';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {Children, OnClickWithId} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {OnLogout} from '../../usecases/auth/authModels';
import {Map as MapComponent} from '../../usecases/map/components/Map';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {MapMarker} from '../../usecases/map/mapModels';
import {MeterMeasurementsContainer} from './MeterMeasurements';

export interface MeterDetailsState {
  selectedTab: TabName;
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

const renderEvent = ({name, type}: EventLog): Children =>
  type === EventLogType.newMeter
    ? <ColoredEvent label={translate('new meter: {{name}}', {name})} type={type}/>
    : <Status label={name}/>;

const renderDate = ({start}: EventLog): Children =>
  <WrappedDateTime date={start} hasContent={!!start}/>;

const renderSerial = ({serial}: Gateway): string => serial;

const MapContent = ({meter, meterMapMarker}: MapProps) => (
  <MapComponent height={400} viewCenter={meter.location.position}>
    {meterMapMarker.isJust() && <ClusterContainer markers={meterMapMarker.get()}/>}
  </MapComponent>
);

export const initialMeterDetailsState: MeterDetailsState = {
  selectedTab: TabName.values,
};

const MapContentWrapper = withEmptyContent<MapProps & WithEmptyContentProps>(MapContent);

class MeterDetailsTabs extends React.Component<Props, MeterDetailsState> {

  constructor(props) {
    super(props);
    this.state = {...initialMeterDetailsState};
  }

  render() {
    const {selectedTab} = this.state;
    const {meter, meterMapMarker, selectEntryAdd, syncWithMetering} = this.props;

    const gateway = meter.gateway;

    const normalizedGateways: DomainModel<GatewayMandatory> = {
      entities: {[gateway.id]: gateway},
      result: [gateway.id],
    };

    const eventLog = eventsDataFormatter(meter);

    const hasContent: boolean = meterMapMarker
      .filter(({status}: MapMarker) => status !== undefined)
      .isJust();

    const wrapperProps: MapProps & WithEmptyContentProps = {
      meter,
      meterMapMarker,
      noContentText: firstUpperTranslated('no reliable position'),
      hasContent,
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
            <Table {...eventLog}>
              <TableColumn
                header={<TableHead>{translate('date')}</TableHead>}
                renderCell={renderDate}
              />
              <TableColumn
                header={<TableHead>{translate('event')}</TableHead>}
                renderCell={renderEvent}
              />
            </Table>
            <TimestampInfoMessage/>
          </TabContent>
          <TabContent tab={TabName.map} selectedTab={selectedTab}>
            <MapContentWrapper {...wrapperProps}/>
          </TabContent>
          <TabContent tab={TabName.connectedGateways} selectedTab={selectedTab}>
            <Row>
              <Table result={normalizedGateways.result} entities={normalizedGateways.entities}>
                <TableColumn
                  header={<TableHead>{translate('gateway serial')}</TableHead>}
                  renderCell={renderSerial}
                />
              </Table>
            </Row>
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
