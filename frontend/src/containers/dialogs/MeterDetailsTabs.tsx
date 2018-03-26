import Checkbox from 'material-ui/Checkbox';
import * as React from 'react';
import {checkbox, checkboxLabel} from '../../app/themes';
import {HasContent} from '../../components/content/HasContent';
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
import {Maybe} from '../../helpers/Maybe';
import {translate} from '../../services/translationService';
import {Gateway, GatewayMandatory} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {Meter, MeterStatusChangelog} from '../../state/domain-models-paginated/meter/meterModels';
import {DomainModel, Normalized} from '../../state/domain-models/domainModels';
import {Measurement} from '../../state/domain-models/measurement/measurementModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {isGeoPositionWithinThreshold} from '../../usecases/map/containers/clusterHelper';
import {Map} from '../../usecases/map/containers/Map';
import {MapMarker} from '../../usecases/map/mapModels';
import {normalizedStatusChangelogFor} from './dialogHelper';

// TODO: [!must!] use real measurement data from backend (another MR)
const measurements: Normalized<any> = {
  entities: {
    id0: {
      quantity: 'Date',
      value: '2017-11-16 09:34',
    },
    id1: {
      quantity: 'Energy',
      value: '170.97 MWh',
    },
    id2: {
      quantity: 'Volume',
      value: '3109.81 m^3',
    },
    id3: {
      quantity: 'Power',
      value: '1.6 kW',
    },
    id4: {
      quantity: 'Volume flow',
      value: '0.029 m^3/h',
    },
    id5: {
      quantity: 'Flow temp.',
      value: '82.5 Celcius',
    },
    id6: {
      quantity: 'Return temp.',
      value: '33.7 Celcius',
    },
    id7: {
      quantity: 'Difference temp.',
      value: '48.86 Kelvin',
    },
  },
  result: ['id0', 'id1', 'id2', 'id3', 'id4', 'id5', 'id6', 'id7'],
};

interface State {
  selectedTab: TabName;
}

interface Props {
  meter: Meter;
  meterMapMarker: Maybe<MapMarker>;
}

export class MeterDetailsTabs extends React.Component<Props, State> {

  state: State = {selectedTab: TabName.values};

  render() {
    const {selectedTab} = this.state;
    const {meter, meterMapMarker} = this.props;

    const gateway = meter.gateway;

    const normalizedGateways: DomainModel<GatewayMandatory> = {
      entities: {[gateway.id]: gateway},
      result: [gateway.id],
    };

    const statusChangelog = normalizedStatusChangelogFor(meter);

    const renderStatusCell = (item: MeterStatusChangelog) =>
      (
        <Status
          {...{
            id: item.name,
            name: item.name,
          }}
        />
      );
    const renderQuantity = ({quantity}: Measurement) => quantity;
    const renderValue = ({value}: Measurement) => value;
    const renderDate = (item: MeterStatusChangelog) => item.start;
    const renderSerial = ({serial}: Gateway) => serial;
    const hasConfidentPosition: boolean = meterMapMarker.filter(isGeoPositionWithinThreshold).isJust();

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
            <HasContent
              hasContent={hasConfidentPosition}
              fallbackContent={<h2 style={{padding: 8}}>{translate('no reliable position')}</h2>}
            >
              <Map height={400} viewCenter={meter.location.position} defaultZoom={7}>
                <ClusterContainer markers={meterMapMarker.get()}/>
              </Map>
            </HasContent>
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
