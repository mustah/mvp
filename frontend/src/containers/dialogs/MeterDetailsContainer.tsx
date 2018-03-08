import {Checkbox} from 'material-ui';
import * as React from 'react';
import {connect} from 'react-redux';
import {checkbox, checkboxLabel} from '../../app/themes';
import {HasContent} from '../../components/content/HasContent';
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
import {Meter, MeterStatusChangelog} from '../../state/domain-models-paginated/meter/meterModels';
import {DomainModel, Normalized, ObjectsById} from '../../state/domain-models/domainModels';
import {getEntitiesDomainModels} from '../../state/domain-models/domainModelsSelectors';
import {Gateway} from '../../state/domain-models/gateway/gatewayModels';
import {Measurement} from '../../state/domain-models/measurement/measurementModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {isGeoPositionWithinThreshold} from '../../usecases/map/containers/clusterHelper';
import {Map} from '../../usecases/map/containers/Map';
import {normalizedStatusChangelogFor, titleOf} from './dialogHelper';
import {Info} from './Info';
import './MeterDetailsContainer.scss';

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
  gateways: ObjectsById<Gateway>;
}

interface State {
  selectedTab: TabName;
}

type Props = StateToProps & OwnProps;

const MeterDetailsInfo = (props: Props) => {
  const {gateways, meter} = props;

  const renderAlarm = () => meter.alarm
    ? <Info label={translate('alarm')} value={meter.alarm}/>
    : null;

  const gateway = gateways[meter.gateway.id];
  const meterFlags = meter.flags || [];
  const meterStatus = meter.statusChangelog[0];

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
          <Info label={translate('city')} value={meter.location.city.name}/>
          <Info label={translate('address')} value={meter.location.address.name}/>
        </Row>
        <Row>
          <Column>
            <Row>
              <Subtitle>{translate('collection')}</Subtitle>
            </Row>
          </Column>
          <Info
            label={translate('status')}
            value={<IconStatus id={gateway.status.id} name={gateway.status.name}/>}
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
          <Info
            label={translate('status')}
            value={<IconStatus id={meterStatus.statusId} name={meterStatus.name}/>}
          />
          {renderAlarm()}
          <Info label={translate('flagged for action')} value={titleOf(meterFlags)}/>
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

    const gateway = gateways[meter.gateway.id];

    const normalizedGateways: DomainModel<Gateway> = {
      entities: {[gateway.id]: gateway},
      result: [gateway.id],
    };

    const statusChangelog = normalizedStatusChangelogFor(meter);

    const renderStatusCell = (item: MeterStatusChangelog) =>
      (
        <Status
          {...{
            id: item.statusId,
            name: item.name,
          }}
        />
      );
    const renderQuantity = ({quantity}: Measurement) => quantity;
    const renderValue = ({value}: Measurement) => value;
    const renderDate = (item: MeterStatusChangelog) => item.start;
    const renderSerial = ({id}: Gateway) => id;
    const renderSignalNoiseRatio = ({signalToNoiseRatio}: Gateway) =>
      signalToNoiseRatio || translate('n/a');
    const hasConfidentPosition: boolean = isGeoPositionWithinThreshold(meter);

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
              <Map height={400} viewCenter={meter.location.position}>
                <ClusterContainer markers={meter}/>
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
  gateways: getEntitiesDomainModels(gateways),
});

export const MeterDetailsContainer =
  connect<StateToProps, null, OwnProps>(mapStateToProps)(MeterDetails);
