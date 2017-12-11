import {Checkbox} from 'material-ui';
import * as React from 'react';
import {connect} from 'react-redux';
import {checkbox, checkboxLabel} from '../../app/themes';
import {IconStatus} from '../../components/icons/IconStatus';
import {Column} from '../../components/layouts/column/Column';
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
import {MainTitle, Subtitle} from '../../components/texts/Titles';
import {RootState} from '../../reducers/rootReducer';
import {translate} from '../../services/translationService';
import {DomainModel, Normalized} from '../../state/domain-models/domainModels';
import {Gateway} from '../../state/domain-models/gateway/gatewayModels';
import {getGatewayEntities} from '../../state/domain-models/gateway/gatewaySelectors';
import {Meter} from '../../state/domain-models/meter/meterModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {Map} from '../../usecases/map/containers/Map';
import {normalizedStatusChangelogFor, titleOf} from './dialogHelper';
import {Info} from './Info';
import './MeterDetailsContainer.scss';
import {Content} from '../../components/content/Content';
import {isGeoPositionWithinThreshold} from '../../usecases/map/containers/clusterHelper';

// TODO[!must!] use real measurement data from backend (another MR)
const measurements: Normalized<any> = {
  entities: {
    id0: {
      quantity: 'Date',
      value: '2017-11-16 09:34',
    },
    id1: {
      quantity: 'Energy',
      value: '170.97 MWh',
      comment: '',
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

interface OwnProps {
  meter: Meter;
}

interface StateToProps {
  gateways: DomainModel<Gateway>;
}

interface State {
  selectedTab: TabName;
}

type Props = StateToProps & OwnProps;

const MeterDetailsInfo = (props: Props) => {
  const {gateways, meter} = props;

  const renderAlarm = () => meter.alarm !== ':Inget fel:' && (<Info label={translate('alarm')} value={meter.alarm}/>);

  const gateway = gateways[meter.gatewayId];
  const status = gateway.status;

  return (
    <Row>
      <Column className="OverView">
        <Row>
          <Column>
            <Row>
              <MainTitle>{translate('meter')}</MainTitle>
            </Row>
          </Column>
          <Info label={translate('meter id')} value={meter.id}/>
          <Info label={translate('product model')} value={meter.manufacturer}/>
          <Info label={translate('medium')} value={meter.medium}/>
          <Info label={translate('city')} value={meter.city.name}/>
          <Info label={translate('address')} value={meter.address.name}/>
        </Row>
        <Row>
          <Column>
            <Row>
              <Subtitle>{translate('collection')}</Subtitle>
            </Row>
          </Column>
          <Info
            label={translate('status')}
            value={<IconStatus id={status.id} name={status.name}/>}
          />
          <Info label={translate('interval')} value="24h"/>
          <Info label={translate('resolution')} value="1h"/>
          <Info label={translate('flagged for action')} value={titleOf(gateway.flags)}/>
        </Row>
        <Row>
          <Column>
            <Row>
              <Subtitle>{translate('validation')}</Subtitle>
            </Row>
          </Column>
          <Info label={translate('status')} value={<IconStatus id={meter.status.id} name={meter.status.name}/>}/>
          {renderAlarm()}
          <Info label={translate('flagged for action')} value={titleOf(meter.flags)}/>
        </Row>
        <Row>
          <Column>
            <Row>
              <Subtitle>{translate('labels')}</Subtitle>
            </Row>
          </Column>
          <Info label={translate('sap id')} value={meter.sapId}/>
          <Info label={translate('facility id')} value={meter.facility}/>
          <Info label={translate('measure id')} value={meter.measurementId}/>
        </Row>
      </Column>
    </Row>
  );
};

class MeterDetailsTabs extends React.Component<Props, State> {

  state: State = {selectedTab: TabName.values};

  render() {
    const {selectedTab} = this.state;
    const {meter, gateways} = this.props;

    const gateway = gateways[meter.gatewayId];
    const normalizedGateways: Normalized<Gateway> = {
      entities: {[gateway.id]: gateway},
      result: [gateway.id],
    };

    const statusChangelog = normalizedStatusChangelogFor(meter);

    const renderStatusCell = ({status}: Meter) => <Status {...status}/>;
    const renderQuantity = (item: any) => item.quantity;
    const renderValue = (item: any) => item.value;
    const renderDate = (item: any) => item.date;
    const renderSerial = ({id}: Gateway) => id;
    const renderSignalNoiseRatio = ({signalToNoiseRatio}: Gateway) => signalToNoiseRatio || translate('n/a');
    const hasConfidentPosition: boolean = isGeoPositionWithinThreshold(meter) ? true : false;

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
              <Checkbox iconStyle={checkbox} labelStyle={checkboxLabel} label={translate('show only changes')}/>
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
            <Content hasContent={hasConfidentPosition} noContentTextKey={'no reliable position'}>
              <Map height={400} viewCenter={meter.position}>
                <ClusterContainer markers={meter}/>
              </Map>
            </Content>
          </TabContent>
          <TabContent tab={TabName.connectedGateways} selectedTab={selectedTab}>
            <Row>
              <Table result={normalizedGateways.result} entities={normalizedGateways.entities}>
                <TableColumn
                  header={<TableHead>{translate('gateway id')}</TableHead>}
                  renderCell={renderSerial}
                />
                <TableColumn
                  header={<TableHead>{translate('latest snr')}</TableHead>}
                  renderCell={renderSignalNoiseRatio}
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

const MeterDetails = (props: Props) => {
  return (
    <div>
      <MeterDetailsInfo {...props}/>
      <MeterDetailsTabs {...props}/>
    </div>
  );
};

const mapStateToProps = ({domainModels: {gateways}}: RootState): StateToProps => ({
  gateways: getGatewayEntities(gateways),
});

export const MeterDetailsContainer = connect<StateToProps, null, OwnProps>(mapStateToProps)(MeterDetails);
