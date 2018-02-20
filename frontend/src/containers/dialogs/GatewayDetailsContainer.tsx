import Checkbox from 'material-ui/Checkbox';
import * as React from 'react';
import {connect} from 'react-redux';
import {checkbox, checkboxLabel} from '../../app/themes';
import {Column, ColumnCenter} from '../../components/layouts/column/Column';
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
import {getPaginatedEntities} from '../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {Gateway} from '../../state/domain-models/gateway/gatewayModels';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {Map} from '../../usecases/map/containers/Map';
import {normalizedStatusChangelogFor, titleOf} from './dialogHelper';
import './GatewayDetailsContainer.scss';
import {Info} from './Info';
import {HasContent} from '../../components/content/HasContent';
import {isGeoPositionWithinThreshold} from '../../usecases/map/containers/clusterHelper';

interface OwnProps {
  gateway: Gateway;
}

interface TabsState {
  selectedTab: TabName;
}

interface StateToProps {
  meters: ObjectsById<Meter>;
}

type Props = OwnProps & StateToProps;

const GatewayDetailsInfo = ({gateway}: OwnProps) => {
  const {city, address, id, productModel, status, flags} = gateway;

  return (
    <div className="GatewayDetailsInfo">
      <Row className="space-between">
        <Column>
          <MainTitle>{translate('gateway details')}</MainTitle>
        </Column>
        <ColumnCenter>
          <Row className="Address">
            <Info label={translate('city')} value={city.name}/>
            <Info label={translate('address')} value={address.name}/>
          </Row>
        </ColumnCenter>
      </Row>
      <Row>
        <Column>
          <img src="assets/images/cme2110.jpg" width={100}/>
        </Column>
        <Column className="OverView">
          <Row>
            <Info label={translate('gateway id')} value={id}/>
            <Info label={translate('product model')} value={productModel}/>
          </Row>
          <Row>
            <Info label={translate('collection')} value={<Status id={status.id} name={status.name}/>}/>
            <Info label={translate('interval')} value={'24h'}/>
            <Info label={translate('flagged for action')} value={titleOf(flags)}/>
          </Row>
        </Column>
      </Row>
    </div>
  );
};

class GatewayDetailsTabs extends React.Component<Props, TabsState> {

  state: TabsState = {selectedTab: TabName.values};

  render() {
    const {selectedTab} = this.state;
    const {gateway, meters} = this.props;

    const renderStatusCell = (meter: Meter) => <Status {...meter.status}/>;
    const renderFacility = (item: Meter) => item.facility;
    const renderManufacturer = (item: Meter) => item.manufacturer;
    const renderDate = (item: Meter) => item.date;
    const renderMedium = (item: Meter) => item.medium;
    const hasConfidentPosition: boolean = !!isGeoPositionWithinThreshold(gateway);

    const statusChangelog = normalizedStatusChangelogFor(gateway);

    return (
      <Row>
        <Tabs className="full-width">
          <TabTopBar>
            <TabHeaders selectedTab={selectedTab} onChangeTab={this.changeTab}>
              <Tab tab={TabName.values} title={translate('meter')}/>
              <Tab tab={TabName.log} title={translate('status log')}/>
              <Tab tab={TabName.map} title={translate('map')}/>
            </TabHeaders>
            <TabSettings/>
          </TabTopBar>
          <TabContent tab={TabName.values} selectedTab={selectedTab}>
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
            <HasContent hasContent={hasConfidentPosition} noContentText={translate('no reliable position')}>
            <Map height={400} viewCenter={gateway.position}>
              <ClusterContainer markers={gateway}/>
            </Map>
            </HasContent>
          </TabContent>
        </Tabs>
      </Row>
    );
  }

  changeTab = (selectedTab: TabName) => this.setState({selectedTab});
}

const GatewayDetails = (props: Props) => (
  <div>
    <GatewayDetailsInfo gateway={props.gateway}/>
    <GatewayDetailsTabs {...props}/>
  </div>
);

const mapStateToProps = ({paginatedDomainModels: {meters}}: RootState): StateToProps => ({
  meters: getPaginatedEntities(meters),
});

export const GatewayDetailsContainer = connect<StateToProps, null, OwnProps>(mapStateToProps)(GatewayDetails);
