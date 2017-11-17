import 'GatewayDialog.scss';
import Checkbox from 'material-ui/Checkbox';
import Dialog from 'material-ui/Dialog';
import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {Meter} from '../../../../state/domain-models/meter/meterModels';
import {OnClick} from '../../../../types/Types';
import MapContainer, {PopupMode} from '../../../map/containers/MapContainer';
import {MapMarker} from '../../../map/mapModels';
import {ButtonClose} from '../buttons/ButtonClose';
import {Column} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Status} from '../status/Status';
import {NormalizedRows, Table, TableColumn} from '../table/Table';
import {TableHead} from '../table/TableHead';
import {Tab} from '../tabs/components/Tab';
import {TabContent} from '../tabs/components/TabContent';
import {TabHeaders} from '../tabs/components/TabHeaders';
import {Tabs} from '../tabs/components/Tabs';
import {TabSettings} from '../tabs/components/TabSettings';
import {TabTopBar} from '../tabs/components/TabTopBar';
import {tabType} from '../tabs/models/TabsModel';
import {MainTitle} from '../texts/Titles';
import {Gateway} from '../../../../state/domain-models/gateway/gatewayModels';

interface GatewayDialogProps {
  displayDialog: boolean;
  close: OnClick;
  gateway: Gateway;
}

interface GatewayDialogState {
  selectedTab: tabType;
}

export class GatewayDialog extends React.Component<GatewayDialogProps, GatewayDialogState> {

  constructor(props) {
    super(props);

    this.state = {
      selectedTab: tabType.values,
    };
  }

  render() {
    const {selectedTab} = this.state;
    const {displayDialog} = this.props;
    const {close} = this.props;
    const {gateway} = this.props;

    const renderStatusCell = (meter: Meter) => <Status {...meter.status}/>;
    const renderMoid = (item: Meter) => item.moid;
    const renderManufacturer = (item: Meter) => item.manufacturer;
    const renderDate = (item: Meter) => item.date;
    const renderMedium = (item: Meter) => item.medium;

    // TODO are these example values too large? i.e. current state, not diff between current and last state
    const gatewayData: NormalizedRows = {
      byId: {
        id1: {
          moid: '26544',
          date: '2017-11-22 09:34',
          status: {
            id: 0,
            name: 'OK',
          },
          medium: 'Heat, Return temp',
          manufacturer: 'ELV',
          comment: '',
        },
        id2: {
          moid: '98754',
          date: '2017-11-22 08:34',
          status: {
            id: 0,
            name: 'OK',
          },
          medium: 'Heat, Return temp',
          manufacturer: 'ELV',
          comment: '',
        },
        id3: {
          moid: '16345',
          date: '2017-11-22 07:34',
          status: {
            id: 0,
            name: 'OK',
          },
          medium: 'Heat, Return temp',
          manufacturer: 'ELV',
          comment: '',
        },
        id4: {
          moid: '74982',
          date: '2017-11-22 06:34',
          status: {
            id: 0,
            name: 'OK',
          },
          medium: 'Heat, Return temp',
          manufacturer: 'ELV',
          comment: '',
        },
        id5: {
          moid: '49852',
          date: '2017-11-22 05:34',
          status: {
            id: 0,
            name: 'OK',
          },
          medium: 'Heat, Return temp',
          manufacturer: 'ELV',
          comment: '',
        },
        id6: {
          moid: '65774',
          date: '2017-11-22 04:34',
          status: {
            id: 3,
            name: 'Fel',
          },
          medium: 'Heat, Return temp',
          manufacturer: 'ELV',
          comment: '',
        },
        id7: {
          moid: '32168',
          date: '2017-11-22 03:34',
          status: {
            id: 0,
            name: 'OK',
          },
          medium: 'Heat, Return temp',
          manufacturer: 'ELV',
          comment: '',
        },
      },
      allIds: ['id1', 'id2', 'id3', 'id4', 'id5', 'id6', 'id7'],
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

    const changeTab = (option: tabType) => {
      this.setState({selectedTab: option});
    };

    return (
      <Dialog
        actions={[(<ButtonClose onClick={close}/>)]}
        autoScrollBodyContent={true}
        contentClassName="Dialog"
        onRequestClose={close}
        open={displayDialog}
      >
        <Row className="Column-space-between">
          <Column>
            <MainTitle>{translate('gateway details')}</MainTitle>
          </Column>
          <Column className="Column-center">
            <Row className="Address">
              <Column>
                <Row className="capitalize Bold">
                  {translate('city')}
                </Row>
                <Row>
                  {gateway.city.name}
                </Row>
              </Column>
              <Column>
                <Row className="capitalize Bold">
                  {translate('address')}
                </Row>
                <Row>
                  {gateway.address.name}
                </Row>
              </Column>
            </Row>
          </Column>
        </Row>
        <Row>
          <Column className="ProductImage">
            <img src="cme2110.jpg" width="100"/>
          </Column>
          <Column className="OverView">
            <Row>
              <Column>
                <Row>
                  {translate('gateway id')}
                </Row>
                <Row>
                  {gateway.id}
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('product model')}
                </Row>
                <Row>
                  {gateway.productModel}
                </Row>
              </Column>
            </Row>
            <Row>
              <Column>
                <Row>
                  {translate('collection')}
                </Row>
                <Status id={gateway.status.id} name={gateway.status.name}/>
              </Column>
              <Column>
                <Row>
                  {translate('interval')}
                </Row>
                <Row>
                  24h
                  {/* TODO gateway model is missing this value*/}
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('flagged for action')}
                </Row>
                <Row>
                  Nej
                  {/* TODO use flags {gateway.flags}*/}
                </Row>
              </Column>
            </Row>
          </Column>
        </Row>
        <Row>
          <Tabs className="full-width">
            <TabTopBar>
              <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
                <Tab tab={tabType.values} title={translate('meter')}/>
                <Tab tab={tabType.log} title={translate('status log')}/>
                <Tab tab={tabType.map} title={translate('map')}/>
              </TabHeaders>
              <TabSettings/>
            </TabTopBar>
            <TabContent tab={tabType.values} selectedTab={selectedTab}>
              <Table data={gatewayData}>
                <TableColumn
                  header={<TableHead className="first">{translate('meter')}</TableHead>}
                  renderCell={renderMoid}
                />
                <TableColumn
                  header={<TableHead>{translate('manufacturer')}</TableHead>}
                  renderCell={renderManufacturer}
                />
                <TableColumn
                  header={<TableHead>{translate('medium')}</TableHead>}
                  renderCell={renderMedium}
                />
                <TableColumn
                  header={<TableHead>{translate('status')}</TableHead>}
                  renderCell={renderStatusCell}
                />
              </Table>
            </TabContent>
            <TabContent tab={tabType.log} selectedTab={selectedTab}>
              <Row>
                <Checkbox iconStyle={checkbox} labelStyle={checkboxLabel} label={translate('show only changes')}/>
              </Row>
              <Table data={gatewayData}>
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
          </Tabs>
        </Row>
      </Dialog>
    );
  }
}
