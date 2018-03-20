import Checkbox from 'material-ui/Checkbox';
import * as React from 'react';
import {checkbox, checkboxLabel} from '../../app/themes';
import {HasContent} from '../../components/content/HasContent';
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
import {MissingDataTitle} from '../../components/texts/Titles';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Gateway} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {isGeoPositionWithinThreshold} from '../../usecases/map/containers/clusterHelper';
import {Map} from '../../usecases/map/containers/Map';
import {MapMarker} from '../../usecases/map/mapModels';
import {normalizedStatusChangelogFor} from './dialogHelper';

interface Props {
  gateway: Gateway;
  gatewayMapMarker: MapMarker;
  meters: ObjectsById<Meter>;
}

interface TabsState {
  selectedTab: TabName;
}

export class GatewayDetailsTabs extends React.Component<Props, TabsState> {

  state: TabsState = {selectedTab: TabName.values};

  changeTab = (selectedTab: TabName) => this.setState({selectedTab});

  render() {
    const {selectedTab} = this.state;
    const {gateway, meters, gatewayMapMarker} = this.props;

    const renderStatusCell = (meter: Meter) => <Status {...meter.status}/>;
    const renderFacility = ({facility}: Meter) => facility;
    const renderManufacturer = ({manufacturer}: Meter) => manufacturer;
    const renderDate = ({date}: Meter) => date;
    const renderMedium = ({medium}: Meter) => medium;
    const hasConfidentPosition: boolean = isGeoPositionWithinThreshold(gatewayMapMarker);

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
                <ClusterContainer markers={gatewayMapMarker}/>
              </Map>
            </HasContent>
          </TabContent>
        </Tabs>
      </Row>
    );
  }
}
