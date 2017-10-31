import 'Gateway.scss';
import Dialog from 'material-ui/Dialog';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {translate} from '../../services/translationService';
import {routes} from '../app/routes';
import {Column} from '../common/components/layouts/column/Column';
import {Row} from '../common/components/layouts/row/Row';
import {Status} from '../common/components/table/status/Status';
import {StatusIcon} from '../common/components/table/status/StatusIcon';
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
import {ButtonClose} from '../common/containers/button-close/ButtonClose';
import MapContainer from '../map/containers/MapContainer';

interface GatewayProps {
  id: string;
}

interface GatewayState {
  displayDialog: boolean;
  selectedTab: tabType;
}

export class Gateway extends React.Component<GatewayProps, GatewayState> {

  constructor(props) {
    super(props);

    this.state = {
      displayDialog: false,
      selectedTab: tabType.list,
    };
  }

  render() {
    const {selectedTab} = this.state;
    const {id} = this.props;

    const open = (event: any): void => {
      event.preventDefault();
      this.setState({displayDialog: true});
    };

    const close = (): void => this.setState({displayDialog: false});

    const renderStatusCell = (value, index) => <Status code={value.code} content={value.text}/>;

    // TODO are these example values too large? i.e. current state, not diff between current and last state
    const gatewayData = {
      byId: {
        id1: {
          date: '2017-11-16 09:34',
          status: {
            code: 0,
            text: '',
          },
          quantity: 'Energy',
          value: '170.97 MWh',
          comment: '',
        },
        id2: {
          date: '2017-11-16 09:34',
          status: {
            code: 0,
            text: '',
          },
          quantity: 'Volume',
          value: '3109.81 m^3',
          comment: '',
        },
        id3: {
          date: '2017-11-16 09:34',
          status: {
            code: 0,
            text: '',
          },
          quantity: 'Power',
          value: '1.6 kW',
          comment: '',
        },
        id4: {
          date: '2017-11-16 09:34',
          status: {
            code: 0,
            text: '',
          },
          quantity: 'Volume flow',
          value: '0.029 m^3/h',
          comment: '',
        },
        id5: {
          date: '2017-11-16 09:34',
          status: {
            code: 0,
            text: '',
          },
          quantity: 'Flow temp.',
          value: '82.5 Celcius',
          comment: '',
        },
        id6: {
          date: '2017-11-16 09:34',
          status: {
            code: 3,
            text: 'Läckage',
          },
          quantity: 'Return temp.',
          value: '33.7 Celcius',
          comment: '',
        },
        id7: {
          date: '2017-11-16 09:34',
          status: {
            code: 0,
            text: '',
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

    return (
      <div>
        <Link to={`${routes.gateway}/${id}`} onClick={open}>{id}</Link>
        <Dialog
          actions={[(<ButtonClose onClick={close}/>)]}
          autoScrollBodyContent={true}
          contentClassName="Dialog"
          onRequestClose={close}
          open={this.state.displayDialog}
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
                  <Row>
                    <StatusIcon code={0}/>
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('validation')}
                  </Row>
                  <Row>
                    <StatusIcon code={3}/>
                  </Row>
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
                  <Tab tab={tabType.dashboard} title={translate('dashboard')}/>
                  <Tab tab={tabType.list} title={translate('list')}/>
                  <Tab tab={tabType.map} title={translate('map')}/>
                </TabHeaders>
                <TabSettings useCase={'gateway'}/>
              </TabTopBar>
              <TabContent tab={tabType.dashboard} selectedTab={selectedTab}>
                hej hej
              </TabContent>
              <TabContent tab={tabType.list} selectedTab={selectedTab}>
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
                <MapContainer/>
              </TabContent>
            </Tabs>
          </Row>
        </Dialog>
      </div>
    );
  }
}
