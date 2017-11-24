import 'GatewayDialogContainer.scss';
import Checkbox from 'material-ui/Checkbox';
import Dialog from 'material-ui/Dialog';
import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {Meter} from '../../../../state/domain-models/meter/meterModels';
import {OnClick} from '../../../../types/Types';
import MapContainer, {PopupMode} from '../../../map/containers/MapContainer';
import {ButtonClose} from '../../components/buttons/ButtonClose';
import {Column} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
import {Status} from '../../components/status/Status';
import {NormalizedRows, Table, TableColumn} from '../../components/table/Table';
import {TableHead} from '../../components/table/TableHead';
import {Tab} from '../../components/tabs/components/Tab';
import {TabContent} from '../../components/tabs/components/TabContent';
import {TabHeaders} from '../../components/tabs/components/TabHeaders';
import {Tabs} from '../../components/tabs/components/Tabs';
import {TabSettings} from '../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../components/tabs/components/TabTopBar';
import {tabType} from '../../components/tabs/models/TabsModel';
import {MainTitle} from '../../components/texts/Titles';
import {Gateway} from '../../../../state/domain-models/gateway/gatewayModels';
import {renderFlags} from './dialogHelper';
import {RootState} from '../../../../reducers/rootReducer';
import {getMeterEntities} from '../../../../state/domain-models/meter/meterSelectors';
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';

interface GatewayDialogProps {
  displayDialog: boolean;
  close: OnClick;
  gateway: Gateway;
}

interface StateToProps {
  entities: { [key: string]: Meter };
}

interface GatewayDialogState {
  selectedTab: tabType;
}

class GatewayDialog extends React.Component<GatewayDialogProps & StateToProps, GatewayDialogState> {

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
    const {entities} = this.props;

    const renderStatusCell = (meter: Meter) => <Status {...meter.status}/>;
    const renderFacility = (item: Meter) => item.facility;
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

    const gatewayMeters: NormalizedRows = {
      byId: entities,
      allIds: gateway.meterIds,
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
            <img src="assets/images/cme2110.jpg" width="100"/>
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
                  {renderFlags(gateway.flags)}
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
              <Table data={gatewayMeters}>
                <TableColumn
                  header={<TableHead className="first">{translate('meter')}</TableHead>}
                  renderCell={renderFacility}
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
              <MapContainer height={400} markers={gateway} viewCenter={gateway.position} popupMode={PopupMode.none}/>
            </TabContent>
          </Tabs>
        </Row>
      </Dialog>
    );
  }
}

const mapStateToProps = ({domainModels: {meters}}: RootState): StateToProps => {
  return {
    entities: getMeterEntities(meters),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({}, dispatch);

export const GatewayDialogContainer =
  connect<StateToProps, {}, GatewayDialogProps>(mapStateToProps, mapDispatchToProps)(GatewayDialog);
