import {Checkbox} from 'material-ui';
import Dialog from 'material-ui/Dialog';
import 'MeteringPointDialogContainer.scss';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {ButtonClose} from '../../components/buttons/ButtonClose';
import {IconDistrictHeating} from '../../components/icons/IconDistrictHeating';
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
import {tabType} from '../../components/tabs/models/TabsModel';
import {Normal} from '../../components/texts/Texts';
import {MainTitle, Subtitle} from '../../components/texts/Titles';
import {RootState} from '../../reducers/rootReducer';
import {translate} from '../../services/translationService';
import {Gateway} from '../../state/domain-models/gateway/gatewayModels';
import {getGatewayEntities} from '../../state/domain-models/gateway/gatewaySelectors';
import {Meter} from '../../state/domain-models/meter/meterModels';
import MapContainer, {PopupMode} from '../../usecases/map/containers/MapContainer';
import {renderFlags} from './dialogHelper';
import {Normalized} from '../../state/domain-models/domainModels';

interface MeteringPointDialogProps {
  meter: Meter;
  displayDialog: boolean;
  close: any;
}

interface StateToProps {
  entities: {[key: string]: Gateway};
}

interface MeteringPointDialogState {
  selectedTab: tabType;
}

class MeteringPointDialog extends React.Component <MeteringPointDialogProps & StateToProps, MeteringPointDialogState> {
  constructor(props) {
    super(props);

    this.state = {
      selectedTab: tabType.values,
    };
  }

  render() {
    const {
      displayDialog,
      close,
      meter,
      entities,
    } = this.props;

    const {
      selectedTab,
    } = this.state;

    const renderStatusCell = (item: any) => <Status {...item.status}/>;
    const renderQuantity = (item: any) => item.quantity;
    const renderValue = (item: any) => item.value;
    const renderDate = (item: any) => item.date;
    const renderSerial = (item: any) => item.id;
    const renderSnr = (item: any) => translate('n/a'); // TODO Gateway should hold SNR (Signal Noise Ratio) information
    const renderAlarm = () => meter.alarm !== ':Inget fel:' && (
      <Column>
        <Row>
          {translate('alarm')}
        </Row>
        <Row>
          {meter.alarm}
        </Row>
      </Column>);

    // TODO We need to support that a meter is connected to several gateways
    // TODO are these example values too large? i.e. current state, not diff between current and last state
    const meterData: Normalized<any> = {
      entities: {
        id0: {
          date: '2017-11-16 09:34',
          status: {
            id: 0,
            name: 'OK',
          },
          quantity: 'Date',
          value: '2017-11-16 09:34',
          comment: '',
        },
        id1: {
          date: '2017-11-16 09:34',
          status: {
            id: 0,
            name: 'OK',
          },
          quantity: 'Energy',
          value: '170.97 MWh',
          comment: '',
        },
        id2: {
          date: '2017-11-16 09:34',
          status: {
            id: 0,
            name: 'OK',
          },
          quantity: 'Volume',
          value: '3109.81 m^3',
          comment: '',
        },
        id3: {
          date: '2017-11-16 09:34',
          status: {
            id: 0,
            name: 'OK',
          },
          quantity: 'Power',
          value: '1.6 kW',
          comment: '',
        },
        id4: {
          date: '2017-11-16 09:34',
          status: {
            id: 0,
            name: 'OK',
          },
          quantity: 'Volume flow',
          value: '0.029 m^3/h',
          comment: '',
        },
        id5: {
          date: '2017-11-16 09:34',
          status: {
            id: 0,
            name: 'OK',
          },
          quantity: 'Flow temp.',
          value: '82.5 Celcius',
          comment: '',
        },
        id6: {
          date: '2017-11-16 09:34',
          status: {
            id: 3,
            name: 'Fel',
          },
          quantity: 'Return temp.',
          value: '33.7 Celcius',
          comment: '',
        },
        id7: {
          date: '2017-11-16 09:34',
          status: {
            id: 0,
            name: 'OK',
          },
          quantity: 'Difference temp.',
          value: '48.86 Kelvin',
          comment: '',
        },
      },
      result: ['id0', 'id1', 'id2', 'id3', 'id4', 'id5', 'id6', 'id7'],
    };

    const changeTab = (option: tabType) => {
      this.setState({selectedTab: option});
    };

    const checkbox: React.CSSProperties = {
      padding: 0,
      margin: 5,
      marginLeft: 0,
    };

    const checkboxLabel: React.CSSProperties = {
      padding: 0,
      margin: 5,
      marginTop: 10,
    };

    return (
      <Dialog
        actions={[(<ButtonClose onClick={close}/>)]}
        autoScrollBodyContent={true}
        contentClassName="Dialog"
        onRequestClose={close}
        open={displayDialog}
      >
        <Row>
          <Column className="OverView">
            <Row>
              <Column>
                <Row>
                  <MainTitle>{translate('meter')}</MainTitle>
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('meter id')}
                </Row>
                <Row>
                  {meter.id}
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('product model')}
                </Row>
                <Row>
                  {meter.manufacturer}
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('medium')}
                </Row>
                <Row>
                  <IconDistrictHeating color={'#2b6ea3'}/>
                  {meter.medium}
                </Row>
              </Column>
              <Column className="address">
                <Row className="capitalize Bold">
                  {translate('city')}
                </Row>
                <Row>
                  {meter.city.name}
                </Row>
              </Column>
              <Column className="Column-center">
                <Row className="capitalize Bold">
                  {translate('address')}
                </Row>
                <Row>
                  {meter.address.name}
                </Row>
              </Column>
            </Row>
            <Row>
              <Column>
                <Row>
                  <Subtitle>{translate('collection')}</Subtitle>
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('status')}
                </Row>
                <Row>
                  {/*TODO A meter could be found on serveral gateways*/}
                  <IconStatus id={entities[meter.gatewayId].status.id} name={entities[meter.gatewayId].status.name}/>
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('interval')}
                </Row>
                <Row>
                  24h
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('resolution')}
                </Row>
                <Row>
                  1h
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('flagged for action')}
                </Row>
                <Row>
                  {/*TODO A meter could be found on serveral gateways*/}
                  <Normal className="first-uppercase">
                    {renderFlags(entities[meter.gatewayId].flags)}
                  </Normal>
                </Row>
              </Column>
            </Row>
            <Row>
              <Column>
                <Row>
                  <Subtitle>{translate('validation')}</Subtitle>
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('status')}
                </Row>
                <Row>
                  <IconStatus id={meter.status.id} name={meter.status.name}/>
                </Row>
              </Column>
              {renderAlarm()}
              <Column>
                <Row>
                  {translate('flagged for action')}
                </Row>
                <Row>
                  <Normal className="first-uppercase">
                    {renderFlags(meter.flags)}
                  </Normal>
                </Row>
              </Column>
            </Row>
            <Row>
              <Column>
                <Row>
                  <Subtitle>{translate('labels')}</Subtitle>
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('sap id')}
                </Row>
                <Row>
                  123234
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('facility id')}
                </Row>
                <Row>
                  {meter.facility}
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('measure id')}
                </Row>
                <Row>
                  12312312
                </Row>
              </Column>
            </Row>
          </Column>
        </Row>
        <Row>
          <Tabs className="full-width first-letter">
            <TabTopBar>
              <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
                <Tab tab={tabType.values} title={translate('latest value')}/>
                <Tab tab={tabType.log} title={translate('status log')}/>
                <Tab tab={tabType.map} title={translate('map')}/>
                <Tab tab={tabType.connectedGateways} title={translate('gateways')}/>
              </TabHeaders>
              <TabSettings/>
            </TabTopBar>
            <TabContent tab={tabType.values} selectedTab={selectedTab}>
              <Table {...meterData}>
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
            <TabContent tab={tabType.log} selectedTab={selectedTab}>
              <Row>
                <Checkbox iconStyle={checkbox} labelStyle={checkboxLabel} label={translate('show only changes')}/>
              </Row>
              <Table {...meterData}>
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
            <TabContent tab={tabType.map} selectedTab={selectedTab}>
              <MapContainer height={400} markers={meter} viewCenter={meter.position} popupMode={PopupMode.none}/>
            </TabContent>
            <TabContent tab={tabType.connectedGateways} selectedTab={selectedTab}>
              <Row>
                <Table result={['id1']} entities={{id1: entities[meter.gatewayId]}}>
                  <TableColumn
                    header={<TableHead>{translate('gateway id')}</TableHead>}
                    renderCell={renderSerial}
                  />
                  <TableColumn
                    header={<TableHead>{translate('latest snr')}</TableHead>}
                    renderCell={renderSnr}
                  />
                </Table>
              </Row>
            </TabContent>
          </Tabs>
        </Row>
      </Dialog>
    );

  }
}

const mapStateToProps = ({domainModels: {gateways}}: RootState): StateToProps => {
  return {
    entities: getGatewayEntities(gateways),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({}, dispatch);

export const MeteringPointDialogContainer =
  connect<StateToProps, {}, MeteringPointDialogProps>(mapStateToProps, mapDispatchToProps)(MeteringPointDialog);
