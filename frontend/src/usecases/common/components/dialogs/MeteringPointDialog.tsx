import {Checkbox} from 'material-ui';
import Dialog from 'material-ui/Dialog';
import 'MeteringPointDialog.scss';
import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {IdNamed} from '../../../../types/Types';
import {ButtonClose} from '../buttons/ButtonClose';
import {IconDistrictHeating} from '../icons/IconDistrictHeating';
import {Column} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Status} from '../status/Status';
import {NormalizedRows, Table, TableColumn} from '../table/Table';
import {TableHead} from '../table/TableHead';
import {Tab} from '../tabs/components/Tab';
import {TabContent} from '../tabs/components/TabContent';
import MapContainer, {PopupMode} from '../../../map/containers/MapContainer';
import {IconStatus} from '../icons/IconStatus';
import {MapMarker} from '../../../map/mapModels';
import {tabType} from '../tabs/models/TabsModel';
import {MainTitle, Subtitle} from '../texts/Titles';
import {Tabs} from '../tabs/components/Tabs';
import {TabTopBar} from '../tabs/components/TabTopBar';
import {TabSettings} from '../tabs/components/TabSettings';
import {Meter} from '../../../../state/domain-models/meter/meterModels';
import {TabHeaders} from '../tabs/components/TabHeaders';

interface MeteringPointDialogProps {
  meter: Meter;
  displayDialog: boolean;
  close: any;
}

interface MeteringPointDialogState {
  selectedTab: tabType;
}

export class MeteringPointDialog extends React.Component<MeteringPointDialogProps, MeteringPointDialogState> {

  constructor(props) {
    super(props);

    this.state = {
      selectedTab: tabType.values,
    };
  }

  render() {
    const {displayDialog} = this.props;
    const {selectedTab} = this.state;
    const {close} = this.props;
    const {meter} = this.props;

    const renderStatusCell = (status: IdNamed) => <Status {...status}/>;
    const renderQuantity = (item: any) => item.quantity;
    const renderValue = (item: any) => item.value;
    const renderDate = (item: any) => item.date;
    const renderSerial = (item: any) => item.serial;
    const renderSnr = (item: any) => item.snr;

    const meterGateways = {
      byId: {
        id1: {
          serial: '0012100026',
          snr: -122,
        },
        id2: {
          serial: '0012105462',
          snr: -96,
        },
      },
      allIds: ['id1', 'id2'],
    };

    // TODO are these example values too large? i.e. current state, not diff between current and last state
    const meterData: NormalizedRows = {
      byId: {
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
            name: 'Läckage',
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
      allIds: ['id0', 'id1', 'id2', 'id3', 'id4', 'id5', 'id6', 'id7'],
    };

    const changeTab = (option: tabType) => {
      this.setState({selectedTab: option});
    };

    // TODO retrieve real location data for the gateway
    const markers: {[key: string]: MapMarker} = {};
    markers[0] = {
      status: {id: 0, name: 'OK'},
      address: {id: '', cityId: '', name: ''},
      city: {id: '', name: ''},
      position: {
        confidence: 1,
        latitude: '57.505281',
        longitude: '12.069336',
      },
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
                  <IconStatus id={0} name="OK"/>
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
                  Nej
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
                  <IconStatus id={3} name="Battery low"/>
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('flagged for action')}
                </Row>
                <Row>
                  Nej
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
                  12312321
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
              <Table data={meterData}>
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
              <Table data={meterData}>
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
              <MapContainer height={400} markers={markers} popupMode={PopupMode.none}/>
            </TabContent>
            <TabContent tab={tabType.connectedGateways} selectedTab={selectedTab}>
              <Row>
                <Table data={meterGateways}>
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
