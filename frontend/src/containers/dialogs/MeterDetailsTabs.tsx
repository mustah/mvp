import Checkbox from 'material-ui/Checkbox';
import * as React from 'react';
import {checkbox, checkboxLabel} from '../../app/themes';
import {Row} from '../../components/layouts/row/Row';
import {Status} from '../../components/status/Status';
import {Table, TableColumn} from '../../components/table/Table';
import {TableHead} from '../../components/table/TableHead';
import {Tab} from '../../components/tabs/components/Tab';
import {TabContent} from '../../components/tabs/components/TabContent';
import {TabHeaders} from '../../components/tabs/components/TabHeaders';
import {Tabs} from '../../components/tabs/components/Tabs';
import {TabSettings} from '../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../components/tabs/components/TabTopBar';
import {formatLabelTimeStamp} from '../../helpers/dateHelpers';
import {Maybe} from '../../helpers/Maybe';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Gateway, GatewayMandatory} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {Meter, MeterStatusChangelog} from '../../state/domain-models-paginated/meter/meterModels';
import {DomainModel} from '../../state/domain-models/domainModels';
import {Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {Identifiable} from '../../types/Types';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {isGeoPositionWithinThreshold} from '../../usecases/map/containers/clusterHelper';
import {Map} from '../../usecases/map/containers/Map';
import {MapMarker} from '../../usecases/map/mapModels';
import {meterMeasurementsForTable, normalizedStatusChangelogFor} from './dialogHelper';

interface State {
  selectedTab: TabName;
}

interface Props {
  meter: Meter;
  meterMapMarker: Maybe<MapMarker>;
}

export interface RenderableMeasurement extends Identifiable {
  quantity: Quantity;
  value?: number | string;
  unit?: string;
  created?: number;
}

const renderQuantity = ({quantity}: RenderableMeasurement) => quantity;
const renderValue = ({value = null, unit}: RenderableMeasurement) => value !== null && unit ? `${value} ${unit}` : '';
const renderCreated = ({created}: RenderableMeasurement) =>
  created ? formatLabelTimeStamp(created) : firstUpperTranslated('never collected');

const renderStatusCell = ({name}: MeterStatusChangelog) => <Status name={name}/>;
const renderDate = (item: MeterStatusChangelog) => item.start;
const renderSerial = ({serial}: Gateway) => serial;

export class MeterDetailsTabs extends React.Component<Props, State> {

  state: State = {selectedTab: TabName.values};
  changeTab = (selectedTab: TabName) => this.setState({selectedTab});

  render() {
    const {selectedTab} = this.state;
    const {meter, meterMapMarker} = this.props;

    const gateway = meter.gateway;

    const normalizedGateways: DomainModel<GatewayMandatory> = {
      entities: {[gateway.id]: gateway},
      result: [gateway.id],
    };

    const statusChangelog = normalizedStatusChangelogFor(meter);
    const measurements: DomainModel<RenderableMeasurement> = meterMeasurementsForTable(meter);
    const hasConfidentPosition: boolean = meterMapMarker.filter(isGeoPositionWithinThreshold).isJust();

    // TODO <HasContent> didn't work here, together with the Maybe
    const map = hasConfidentPosition ? (
      <Map height={400} viewCenter={meter.location.position} defaultZoom={7}>
        <ClusterContainer markers={meterMapMarker.get()}/>
      </Map>
    ) : <h2 style={{padding: 8}}>{firstUpperTranslated('no reliable position')}</h2>;

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
            <TabSettings/>
          </TabTopBar>
          <TabContent tab={TabName.values} selectedTab={selectedTab}>
            <Table {...measurements}>
              <TableColumn
                header={<TableHead className="first">{translate('quantity')}</TableHead>}
                renderCell={renderQuantity}
              />
              <TableColumn
                header={<TableHead>{translate('value')}</TableHead>}
                renderCell={renderValue}
              />
              <TableColumn
                header={<TableHead>{translate('collected at')}</TableHead>}
                renderCell={renderCreated}
              />
            </Table>
          </TabContent>
          <TabContent tab={TabName.log} selectedTab={selectedTab}>
            <Row>
              <Checkbox
                iconStyle={checkbox}
                labelStyle={checkboxLabel}
                label={translate('show only changes')}
              />
            </Row>
            <Table {...statusChangelog}>
              <TableColumn
                header={<TableHead>{translate('date')}</TableHead>}
                renderCell={renderDate}
              />
              <TableColumn
                header={<TableHead>{translate('status')}</TableHead>}
                renderCell={renderStatusCell}
              />
            </Table>
          </TabContent>
          <TabContent tab={TabName.map} selectedTab={selectedTab}>
            {map}
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
}
