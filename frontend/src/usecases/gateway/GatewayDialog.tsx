import 'Gateway.scss';
import Dialog from 'material-ui/Dialog';
import * as React from 'react';
import {translate} from '../../services/translationService';
import {IdNamed} from '../../types/Types';
import {ButtonClose} from '../common/components/buttons/ButtonClose';
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

interface GatewayDialogProps {
  displayDialog: boolean;
  close: any;
}

interface GatewayDialogState {
  selectedTab: tabType;
}

export class GatewayDialog extends React.Component<GatewayDialogProps, GatewayDialogState> {

  constructor(props) {
    super(props);

    this.state = {
      selectedTab: tabType.statusChanges,
    };
  }

  render() {
    const {selectedTab} = this.state;
    const {displayDialog} = this.props;
    const {close} = this.props;

    const renderStatusCell = (status: IdNamed) => <Status {...status}/>;

    // TODO are these example values too large? i.e. current state, not diff between current and last state
    const gatewayData = {
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
        <h2 className="capitalize">{translate('gateway details')}</h2>
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
                  {translate('city')}
                </Row>
                <Row>
                  Perstorp
                </Row>
              </Column>
              <Column>
                <Row>
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
                  {translate('collection')}
                </Row>
                <Status id={0} name="OK"/>
              </Column>
              <Column>
                <Row>
                  {translate('validation')}
                </Row>
                <Status id={3} name="OK"/>
              </Column>
              <Column>
                <Row>
                  {translate('status')}
                </Row>
                <Row>
                  Läckage
                </Row>
              </Column>
              <Column>
                <Row>
                  {translate('status')}
                </Row>
                <Row>
                  {translate('action pending')}
                </Row>
              </Column>
            </Row>
          </Column>
        </Row>
        <Row>
          <Tabs className="full-width">
            <TabTopBar>
              <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
                <Tab tab={tabType.statusChanges} title={translate('status changes')}/>
                <Tab tab={tabType.map} title={translate('map')}/>
              </TabHeaders>
              <TabSettings useCase={'gateway'}/>
            </TabTopBar>
            <TabContent tab={tabType.statusChanges} selectedTab={selectedTab}>
              <Table data={gatewayData}>
                <TableColumn
                  id={'date'}
                  header={<TableHead>{translate('date')}</TableHead>}
                />
                <TableColumn
                  id={'status'}
                  header={<TableHead>{translate('status')}</TableHead>}
                  cell={renderStatusCell}
                />
                <TableColumn
                  id={'quantity'}
                  header={<TableHead>{translate('quantity')}</TableHead>}
                />
                <TableColumn
                  id={'value'}
                  header={<TableHead>{translate('value')}</TableHead>}
                />
                <TableColumn
                  id={'comment'}
                  header={<TableHead>{translate('comment')}</TableHead>}
                />
              </Table>
            </TabContent>
            <TabContent tab={tabType.map} selectedTab={selectedTab}>
              <MapContainer markers={markers} popupMode={PopupMode.none}/>
            </TabContent>
          </Tabs>
        </Row>
      </Dialog>
    );
  }
}
