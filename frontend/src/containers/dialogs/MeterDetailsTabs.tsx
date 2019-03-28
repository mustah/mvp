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
import '../../components/table/Table.scss';
import {Tab} from '../../components/tabs/components/Tab';
import {TabContent} from '../../components/tabs/components/TabContent';
import {TabHeaders} from '../../components/tabs/components/TabHeaders';
import {Tabs} from '../../components/tabs/components/Tabs';
import {TabSettings} from '../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../components/tabs/components/TabTopBar';
import {TimestampInfoMessage} from '../../components/timestamp-info-message/TimestampInfoMessage';
import {Maybe} from '../../helpers/Maybe';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {GatewayMandatory} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {EventLogType} from '../../state/domain-models-paginated/meter/meterModels';
import {eventsDataFormatter} from '../../state/domain-models-paginated/meter/meterSchema';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {OnClickWith, OnClickWithId} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {OnLogout} from '../../usecases/auth/authModels';
import {Map as MapComponent} from '../../usecases/map/components/Map';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {MapMarker} from '../../usecases/map/mapModels';

import {MeterMeasurementsContentContainer} from '../../usecases/meter/measurements/containers/MeterMeasurementsContentContainer';
import {LegendItem} from '../../state/report/reportModels';

export interface MeterDetailsState {
  selectedTab: TabName;
}

interface MeterGatewayProps {
  gateways: GatewayMandatory[];
}

interface MapProps {
  meter: MeterDetails;
  meterMapMarker: Maybe<MapMarker>;
}

interface OwnProps extends MapProps {
  addToReport: OnClickWith<LegendItem>;
  syncWithMetering: OnClickWithId;
  useCollectionPeriod?: boolean;
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
  <td className="left-most"><WrappedDateTime date={start} hasContent={!!start}/></td>;

const renderStatus = ({dataItem: {status: {name}}}) => <td><Status label={name}/></td>;

const renderStatusChange = ({dataItem: {statusChanged}}) =>
  <td><WrappedDateTime date={statusChanged} hasContent={!!statusChanged}/></td>;

const MapContent = ({meter, meterMapMarker}: MapProps) => (
  <MapComponent height={400} viewCenter={meter.location.position}>
    {meterMapMarker.isJust() && <ClusterContainer markers={meterMapMarker.get()}/>}
  </MapComponent>
);

const GatewayContent = ({gateways}: MeterGatewayProps) => (
  <Grid scrollable="none" data={gateways}>
    <GridColumn field="serial" title={translate('gateway serial')} className="left-most" headerClassName="left-most"/>
    <GridColumn field="productModel" title={translate('product model')}/>
    <GridColumn field="ip" title={translate('ip')}/>
    <GridColumn field="phoneNumber" title={translate('phone number')}/>
    <GridColumn title={translate('collection')} cell={renderStatus}/>
    <GridColumn title={translate('status change')} cell={renderStatusChange}/>
  </Grid>
);

export const initialMeterDetailsState: MeterDetailsState = {
  selectedTab: TabName.values,
};

const GatewayContentWrapper = withEmptyContent<MeterGatewayProps & WithEmptyContentProps>(GatewayContent);

const MapContentWrapper = withEmptyContent<MapProps & WithEmptyContentProps>(MapContent);

class MeterDetailsTabs extends React.Component<Props, MeterDetailsState> {

  constructor(props) {
    super(props);
    this.state = {...initialMeterDetailsState};
  }

  render() {
    const {selectedTab} = this.state;
    const {meter, meterMapMarker, addToReport, syncWithMetering, useCollectionPeriod} = this.props;

    const gateways: GatewayMandatory[] = meter.gateway ? [meter.gateway] : [];

    const eventLog = toArray(eventsDataFormatter(meter).entities);

    const mapWrapperProps: MapProps & WithEmptyContentProps = {
      meter,
      meterMapMarker,
      noContentText: firstUpperTranslated('no reliable position'),
      hasContent: meterMapMarker
        .filter(({status}: MapMarker) => status !== undefined)
        .isJust(),
    };

    const gatewaysWrapperProps: MeterGatewayProps & WithEmptyContentProps = {
      gateways,
      noContentText: firstUpperTranslated('no gateway connected'),
      hasContent: gateways.length > 0,
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
                item={meter}
                addToReport={addToReport}
                syncWithMetering={syncWithMetering}
              />
            </TabSettings>
          </TabTopBar>
          <TabContent tab={TabName.values} selectedTab={selectedTab}>
            <MeterMeasurementsContentContainer meter={meter} useCollectionPeriod={useCollectionPeriod}/>
          </TabContent>
          <TabContent tab={TabName.log} selectedTab={selectedTab}>
            <Grid data={eventLog} scrollable="none">
              <GridColumn
                title={translate('date')}
                cell={renderDate}
                headerClassName="left-most"
              />
              <GridColumn title={translate('event')} cell={renderEvent}/>
            </Grid>
            <TimestampInfoMessage/>
          </TabContent>
          <TabContent tab={TabName.map} selectedTab={selectedTab}>
            {selectedTab === TabName.map && <MapContentWrapper {...mapWrapperProps}/>}
          </TabContent>
          <TabContent tab={TabName.connectedGateways} selectedTab={selectedTab}>
            <GatewayContentWrapper {...gatewaysWrapperProps}/>
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
