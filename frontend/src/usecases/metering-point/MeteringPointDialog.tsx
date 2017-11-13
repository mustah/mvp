import Dialog from 'material-ui/Dialog';
import 'MeteringPoint.scss';
import * as React from 'react';
import {translate} from '../../services/translationService';
import {IdNamed} from '../../types/Types';
import {ButtonClose} from '../common/components/buttons/ButtonClose';
import {IconDistrictHeating} from '../common/components/icons/IconDistrictHeating';
import {Column} from '../common/components/layouts/column/Column';
import {Row} from '../common/components/layouts/row/Row';
import {Status} from '../common/components/table/status/Status';
import {Table} from '../common/components/table/table/Table';
import {TableHead} from '../common/components/table/table/TableHead';
import {TableColumn} from '../common/components/table/tableColumn/TableColumn';
import {Tab} from '../common/components/tabs/components/Tab';
import {TabContent} from '../common/components/tabs/components/TabContent';
import {TabHeaders} from '../common/components/tabs/components/TabHeaders';
import {Tabs} from '../common/components/tabs/components/Tabs';
import {TabSettings} from '../common/components/tabs/components/TabSettings';
import {TabTopBar} from '../common/components/tabs/components/TabTopBar';
import {tabType} from '../common/components/tabs/models/TabsModel';
import MapContainer, {PopupMode} from '../map/containers/MapContainer';
import {MapMarker} from '../map/mapModels';
import {StatusIcon} from '../common/components/table/status/StatusIcon';
import {Checkbox} from 'material-ui';
import {MainTitle, Subtitle} from '../common/components/texts/Titles';

interface MeteringPointDialogProps {
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

    const renderStatusCell = (status: IdNamed) => <Status {...status}/>;

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
    const meterData = {
      byId: {
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
      allIds: ['id1', 'id2', 'id3', 'id4', 'id5', 'id6', 'id7'],
    };

    const changeTab = (option: tabType) => {
      this.setState({selectedTab: option});
    };

    // TODO retrieve real location data for the gateway
    const markers: { [key: string]: MapMarker } = {};
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
                  12000747
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('product model')}
                </Row>
                <Row>
                  KAM
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('medium')}
                </Row>
                <Row>
                  <IconDistrictHeating color={'#2b6ea3'}/>
                  Värme
                </Row>
              </Column>
              <Column className="address">
                <Row className="capitalize Bold">
                  {translate('city')}
                </Row>
                <Row>
                  Perstorp
                </Row>
              </Column>
              <Column className="Column-center">
                <Row className="capitalize Bold">
                  {translate('address')}
                </Row>
                <Row>
                  Duvstigen 5
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
                  <StatusIcon id={0} name="OK"/>
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
                  <StatusIcon id={3} name="Battery low"/>
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
          <Tabs className="full-width">
            <TabTopBar>
              <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
                <Tab tab={tabType.values} title={translate('latest value')}/>
                <Tab tab={tabType.log} title={translate('status log')}/>
                <Tab tab={tabType.map} title={translate('map')}/>
                <Tab tab={tabType.connectedGateways} title={translate('gateways')}/>
              </TabHeaders>
              <TabSettings useCase={'meteringPoint'}/>
            </TabTopBar>
            <TabContent tab={tabType.values} selectedTab={selectedTab}>
              <Table data={meterData}>
                <TableColumn
                  id={'quantity'}
                  header={<TableHead className="first">{translate('quantity')}</TableHead>}
                />
                <TableColumn
                  id={'value'}
                  header={<TableHead>{translate('value')}</TableHead>}
                />
              </Table>
            </TabContent>
            <TabContent tab={tabType.log} selectedTab={selectedTab}>
              <Row>
                <Checkbox iconStyle={checkbox} labelStyle={checkboxLabel} label={translate('show only changes')}/>
              </Row>
              <Table data={meterData}>
                <TableColumn
                  id={'date'}
                  header={<TableHead>{translate('date')}</TableHead>}
                />
                <TableColumn
                  id={'status'}
                  header={<TableHead>{translate('status')}</TableHead>}
                  cell={renderStatusCell}
                />
              </Table>
            </TabContent>
            <TabContent tab={tabType.map} selectedTab={selectedTab}>
              <MapContainer markers={markers} popupMode={PopupMode.none}/>
            </TabContent>
            <TabContent tab={tabType.connectedGateways} selectedTab={selectedTab}>
              <Row>
                <Table data={meterGateways}>
                  <TableColumn
                    id={'serial'}
                    header={<TableHead>{translate('gateway id')}</TableHead>}
                  />
                  <TableColumn
                    id={'snr'}
                    header={<TableHead>{translate('latest snr')}</TableHead>}
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
