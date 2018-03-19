import Checkbox from 'material-ui/Checkbox';
import * as React from 'react';
import {connect} from 'react-redux';
import {checkbox, checkboxLabel} from '../../app/themes';
import {HasContent} from '../../components/content/HasContent';
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
import {MainTitle, MissingDataTitle} from '../../components/texts/Titles';
import {RootState} from '../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {getEntitiesDomainModels} from '../../state/domain-models/domainModelsSelectors';
import {Gateway} from '../../state/domain-models/gateway/gatewayModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {isGeoPositionWithinThreshold} from '../../usecases/map/containers/clusterHelper';
import {Map} from '../../usecases/map/containers/Map';
import {MapMarker} from '../../usecases/map/mapModels';
import {normalizedStatusChangelogFor, titleOf} from './dialogHelper';
import './GatewayDetailsContainer.scss';
import {Info} from './Info';

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
  const {location: {city, address}, serial, productModel, status, flags} = gateway;

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
            <Info label={translate('gateway serial')} value={serial}/>
            <Info label={translate('product model')} value={productModel}/>
          </Row>
          <Row>
            <Info
              label={translate('collection')}
              value={<Status id={status.id} name={status.name}/>}
            />
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
    const {status, location} = gateway;

    const renderStatusCell = (meter: Meter) => <Status {...meter.status}/>;
    const renderFacility = ({facility}: Meter) => facility;
    const renderManufacturer = ({manufacturer}: Meter) => manufacturer;
    const renderDate = ({date}: Meter) => date;
    const renderMedium = ({medium}: Meter) => medium;
    const markers: MapMarker = {status, location};
    const hasConfidentPosition: boolean = isGeoPositionWithinThreshold(markers);

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
              fallbackContent={<MissingDataTitle title={firstUpperTranslated('no reliable position')}/>}
            >
              <Map height={400} viewCenter={gateway.location.position} defaultZoom={7}>
                <ClusterContainer markers={markers}/>
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

const mapStateToProps = ({domainModels: {allMeters}}: RootState): StateToProps => ({
  meters: getEntitiesDomainModels(allMeters),
});

export const GatewayDetailsContainer =
  connect<StateToProps, null, OwnProps>(mapStateToProps)(GatewayDetails);
