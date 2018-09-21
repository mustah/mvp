import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {ListActionsDropdown} from '../../components/actions-dropdown/ListActionsDropdown';
import {WrappedDateTime} from '../../components/dates/WrappedDateTime';
import {withEmptyContent, WithEmptyContentProps} from '../../components/hoc/withEmptyContent';
import {Row} from '../../components/layouts/row/Row';
import {Status} from '../../components/status/Status';
import {Table, TableColumn} from '../../components/table/Table';
import '../../components/table/Table.scss';
import {TableHead} from '../../components/table/TableHead';
import {TableInfoText} from '../../components/table/TableInfoText';
import {Tab} from '../../components/tabs/components/Tab';
import {TabContent} from '../../components/tabs/components/TabContent';
import {TabHeaders} from '../../components/tabs/components/TabHeaders';
import {Tabs} from '../../components/tabs/components/Tabs';
import {TabSettings} from '../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../components/tabs/components/TabTopBar';
import {Maybe} from '../../helpers/Maybe';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Gateway, GatewayMandatory} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {statusChangelogDataFormatter} from '../../state/domain-models-paginated/gateway/gatewaySchema';
import {MeterStatusChangelog} from '../../state/domain-models-paginated/meter/meterModels';
import {DomainModel} from '../../state/domain-models/domainModels';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {Children, Identifiable, OnClickWithId} from '../../types/Types';
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

export interface RenderableMeasurement extends Identifiable {
  quantity: Quantity;
  value?: number | string;
  unit?: string;
  created?: number;
}

const renderStatus = ({name}: MeterStatusChangelog): Children => <Status label={name}/>;

const renderDate = ({start}: MeterStatusChangelog): Children =>
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

    const statusChangelog = statusChangelogDataFormatter(meter);

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
        <Tabs className="full-width first-letter">
          <TabTopBar>
            <TabHeaders selectedTab={selectedTab} onChangeTab={this.changeTab}>
              <Tab tab={TabName.values} title={translate('latest value')}/>
              <Tab tab={TabName.log} title={translate('status log')}/>
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
            <MeterMeasurementsContainer meter={meter} />
          </TabContent>
          <TabContent tab={TabName.log} selectedTab={selectedTab}>
            <Table {...statusChangelog}>
              <TableColumn
                header={<TableHead>{translate('date')}</TableHead>}
                renderCell={renderDate}
              />
              <TableColumn
                header={<TableHead>{translate('status')}</TableHead>}
                renderCell={renderStatus}
              />
            </Table>
            <TableInfoText/>
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
