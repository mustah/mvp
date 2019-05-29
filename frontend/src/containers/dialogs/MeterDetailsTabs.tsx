import {Grid, GridColumn, GridDetailRowProps} from '@progress/kendo-react-grid';
import {toArray} from 'lodash';
import * as React from 'react';
import {compose} from 'recompose';
import {makeGridClassName} from '../../app/themes';
import {ListActionsDropdown} from '../../components/actions-dropdown/ListActionsDropdown';
import {WrappedDateTime} from '../../components/dates/WrappedDateTime';
import {withEmptyContent, WithEmptyContentProps} from '../../components/hoc/withEmptyContent';
import {ThemeContext, withCssStyles} from '../../components/hoc/withThemeProvider';
import {Row} from '../../components/layouts/row/Row';
import {ColoredEvent, Status} from '../../components/status/Status';
import {Tab} from '../../components/tabs/components/Tab';
import {TabContent} from '../../components/tabs/components/TabContent';
import {TabHeaders} from '../../components/tabs/components/TabHeaders';
import {Tabs} from '../../components/tabs/components/Tabs';
import {TabSettings} from '../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../components/tabs/components/TabTopBar';
import {TimestampInfoMessage} from '../../components/timestamp-info-message/TimestampInfoMessage';
import {useForceUpdate} from '../../hooks/forceUpdateHook';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {GatewayMandatory} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {EventLogType} from '../../state/domain-models-paginated/meter/meterModels';
import {eventsDataFormatter} from '../../state/domain-models-paginated/meter/meterSchema';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {LegendItem} from '../../state/report/reportModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {OnClickWith, OnClickWithId} from '../../types/Types';
import {ResponsiveMap} from '../../usecases/map/components/Map';
import {MarkerCluster} from '../../usecases/map/components/MarkerCluster';
import {MapMarkerClusters, OnCenterMapEvent} from '../../usecases/map/mapModels';
import {CollectionContentContainer} from '../../usecases/meter/collection/containers/CollectionContentContainer';
import {MeterMeasurementsContentContainer} from '../../usecases/meter/measurements/containers/MeterMeasurementsContentContainer';
import {OwnProps} from '../../usecases/meter/measurements/meterDetailModels';
import './MeterDetailsTabs.scss';

export interface MeterDetailsState {
  selectedTab: TabName;
}

interface MeterGatewayProps {
  gateways: GatewayMandatory[];
}

interface MapProps extends OnCenterMapEvent {
  meter: MeterDetails;
  mapMarkerClusters: MapMarkerClusters;
}

interface Props extends MapProps {
  addToReport: OnClickWith<LegendItem>;
  syncWithMetering: OnClickWithId;
  useCollectionPeriod: boolean;
}

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

const MapContent = ({meter: {location: {position}, id}, mapMarkerClusters, onCenterMap}: MapProps) => (
  <ResponsiveMap center={position} id={'meterDetails'} key={`meterDetails-${id}`} onCenterMap={onCenterMap} zoom={17}>
    <MarkerCluster mapMarkerClusters={mapMarkerClusters}/>
  </ResponsiveMap>
);

const ExtraInfo = ({dataItem: {extraInfo}}: GridDetailRowProps) => {
  const rows = extraInfo && Object.keys(extraInfo).length && Object.keys(extraInfo)
    .sort()
    .map(key =>
      (
        <tr key={key}>
          <td>{key}</td>
          <td>{extraInfo[key]}</td>
        </tr>
      )
    );

  const content = rows
    ? <table className="GatewayDetails">{rows}</table>
    : <p>{firstUpperTranslated('no additional data exists for this gateway')}</p>;

  return (
    <section>
      {content}
    </section>
  );
};

const GatewayContent = ({cssStyles, gateways}: MeterGatewayProps & ThemeContext) => {
  const forceUpdate = useForceUpdate();

  const onExpandChange = (ev) => {
    ev.dataItem.expanded = !ev.dataItem.expanded;
    forceUpdate();
  };

  return (
    <Grid
      className={makeGridClassName(cssStyles)}
      scrollable="none"
      data={gateways}
      detail={ExtraInfo}
      expandField="expanded"
      onExpandChange={onExpandChange}
    >
      <GridColumn field="serial" title={translate('gateway serial')} className="left-most" headerClassName="left-most"/>
      <GridColumn field="productModel" title={translate('product model')}/>
      <GridColumn field="ip" title={translate('ip')}/>
      <GridColumn field="phoneNumber" title={translate('phone number')}/>
      <GridColumn title={translate('collection')} cell={renderStatus}/>
      <GridColumn title={translate('status change')} cell={renderStatusChange}/>
    </Grid>
  );
};

export const initialMeterDetailsState: MeterDetailsState = {
  selectedTab: TabName.values,
};

type WrapperProps = MeterGatewayProps & WithEmptyContentProps;

const GatewayContentWrapper = compose<WrapperProps & ThemeContext, WrapperProps>(
  withCssStyles,
  withEmptyContent
)(GatewayContent);

const MapContentWrapper = withEmptyContent<MapProps & WithEmptyContentProps>(MapContent);

export class MeterDetailsTabs extends React.Component<Props, MeterDetailsState> {

  constructor(props) {
    super(props);
    this.state = {...initialMeterDetailsState};
  }

  render() {
    const {selectedTab} = this.state;
    const {meter, mapMarkerClusters, addToReport, onCenterMap, syncWithMetering, useCollectionPeriod} = this.props;

    const gateways: GatewayMandatory[] = meter.gateway ? [meter.gateway] : [];

    const eventLog = toArray(eventsDataFormatter(meter).entities);

    const mapWrapperProps: MapProps & WithEmptyContentProps = {
      meter,
      mapMarkerClusters,
      noContentText: firstUpperTranslated('no reliable position'),
      hasContent: mapMarkerClusters.markers.length > 0,
      onCenterMap,
    };

    const gatewaysWrapperProps: WrapperProps = {
      gateways,
      noContentText: firstUpperTranslated('no gateway connected'),
      hasContent: gateways.length > 0,
    };

    const meterDetailProps: OwnProps = {meter, useCollectionPeriod};

    return (
      <Row>
        <Tabs className="MeterDetailsTabs full-width first-letter">
          <TabTopBar>
            <TabHeaders selectedTab={selectedTab} onChangeTab={this.changeTab}>
              <Tab tab={TabName.values} title={translate('measurements')}/>
              <Tab tab={TabName.collection} title={translate('collection')}/>
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
            {selectedTab === TabName.values && <MeterMeasurementsContentContainer {...meterDetailProps}/>}
          </TabContent>
          <TabContent tab={TabName.collection} selectedTab={selectedTab}>
            {selectedTab === TabName.collection && <CollectionContentContainer {...meterDetailProps}/>}
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
