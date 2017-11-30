import Checkbox from 'material-ui/Checkbox';
import * as React from 'react';
import {connect} from 'react-redux';
import {checkbox, checkboxLabel} from '../../app/themes';
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
import {MainTitle} from '../../components/texts/Titles';
import {RootState} from '../../reducers/rootReducer';
import {translate} from '../../services/translationService';
import {DomainModel} from '../../state/domain-models/domainModels';
import {Gateway} from '../../state/domain-models/gateway/gatewayModels';
import {Meter} from '../../state/domain-models/meter/meterModels';
import {getMeterEntities} from '../../state/domain-models/meter/meterSelectors';
import {TopLevelTab} from '../../state/ui/tabs/tabsModels';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {Map} from '../../usecases/map/containers/Map';
import {normalizedStatusChangelogs, renderFlags} from './dialogHelper';
import './GatewayDetailsContainer.scss';
import './GatewayDialogContainer.scss';

interface OwnProps {
  gateway: Gateway;
}

interface OwnState {
  selectedTab: TopLevelTab;
}

interface StateToProps {
  meters: DomainModel<Meter>;
}

class GatewayDetails extends React.Component<OwnProps & StateToProps, OwnState> {

  constructor(props) {
    super(props);
    this.state = {selectedTab: TopLevelTab.values};
  }

  render() {
    const {selectedTab} = this.state;
    const {gateway, meters} = this.props;

    const renderStatusCell = (meter: Meter) => <Status {...meter.status}/>;
    const renderFacility = (item: Meter) => item.facility;
    const renderManufacturer = (item: Meter) => item.manufacturer;
    const renderDate = (item: Meter) => item.date;
    const renderMedium = (item: Meter) => item.medium;

    const statusChangelog = normalizedStatusChangelogs(gateway);

    return (
      <div className="GatewayDetails">
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
              <TabHeaders selectedTab={selectedTab} onChangeTab={this.changeTab}>
                <Tab tab={TopLevelTab.values} title={translate('meter')}/>
                <Tab tab={TopLevelTab.log} title={translate('status log')}/>
                <Tab tab={TopLevelTab.map} title={translate('map')}/>
              </TabHeaders>
              <TabSettings/>
            </TabTopBar>
            <TabContent tab={TopLevelTab.values} selectedTab={selectedTab}>
              <Table result={gateway.meterIds} entities={meters}>
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
            <TabContent tab={TopLevelTab.log} selectedTab={selectedTab}>
              <Row>
                <Checkbox iconStyle={checkbox} labelStyle={checkboxLabel} label={translate('show only changes')}/>
              </Row>
              <Table entities={statusChangelog.entities} result={statusChangelog.result}>
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
            <TabContent tab={TopLevelTab.map} selectedTab={selectedTab}>
              <Map height={400} viewCenter={gateway.position}>
                <ClusterContainer markers={gateway}/>
              </Map>
            </TabContent>
          </Tabs>
        </Row>
      </div>
    );
  }

  changeTab = (tab: TopLevelTab) => {
    this.setState({selectedTab: tab});
  }
}

const mapStateToProps = ({domainModels: {meters}}: RootState): StateToProps => {
  return {
    meters: getMeterEntities(meters),
  };
};

export const GatewayDetailsContainer = connect<StateToProps, null, OwnProps>(mapStateToProps)(GatewayDetails);
