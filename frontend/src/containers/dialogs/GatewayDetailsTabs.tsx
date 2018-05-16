import * as React from 'react';
import {HasContent} from '../../components/content/HasContent';
import {Row} from '../../components/layouts/row/Row';
import {Status} from '../../components/status/Status';
import {Table, TableColumn} from '../../components/table/Table';
import {TableHead} from '../../components/table/TableHead';
import {Tab} from '../../components/tabs/components/Tab';
import {TabContent} from '../../components/tabs/components/TabContent';
import {TabHeaders} from '../../components/tabs/components/TabHeaders';
import {Tabs} from '../../components/tabs/components/Tabs';
import {TabTopBar} from '../../components/tabs/components/TabTopBar';
import {MissingDataTitle} from '../../components/texts/Titles';
import {Maybe} from '../../helpers/Maybe';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Gateway} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {Map} from '../../usecases/map/components/Map';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {isGeoPositionWithinThreshold} from '../../usecases/map/helper/mapHelper';
import {MapMarker} from '../../usecases/map/mapModels';

interface Props {
  gateway: Gateway;
  gatewayMapMarker: Maybe<MapMarker>;
  meters: ObjectsById<Meter>;
}

interface TabsState {
  selectedTab: TabName;
}

const renderStatusCell = (meter: Meter) => <Status name={meter.status.name}/>;

const renderFacility = ({facility}: Meter) => facility;

const renderMeterAddress = ({address}: Meter) =>
  address ? address : firstUpperTranslated('unknown');

const renderManufacturer = ({manufacturer}: Meter) => manufacturer;

const renderMedium = ({medium}: Meter) => medium;

export class GatewayDetailsTabs extends React.Component<Props, TabsState> {

  state: TabsState = {selectedTab: TabName.values};

  render() {
    const {selectedTab} = this.state;
    const {gateway, meters, gatewayMapMarker} = this.props;

    const hasConfidentPosition: boolean =
      gatewayMapMarker.filter(isGeoPositionWithinThreshold).isJust();

    const noReliablePosition = firstUpperTranslated('no reliable position');

    return (
      <Row>
        <Tabs className="full-width">
          <TabTopBar>
            <TabHeaders selectedTab={selectedTab} onChangeTab={this.changeTab}>
              <Tab tab={TabName.values} title={translate('meter')}/>
              <Tab tab={TabName.map} title={translate('map')}/>
            </TabHeaders>
          </TabTopBar>
          <TabContent tab={TabName.values} selectedTab={selectedTab} className="Scrollable-Table">
            <Table result={gateway.meterIds} entities={meters}>
              <TableColumn
                header={<TableHead>{translate('facility id')}</TableHead>}
                renderCell={renderFacility}
              />
              <TableColumn
                header={<TableHead>{translate('meter')}</TableHead>}
                renderCell={renderMeterAddress}
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
          <TabContent tab={TabName.map} selectedTab={selectedTab}>
            <HasContent
              hasContent={hasConfidentPosition}
              fallbackContent={<MissingDataTitle title={noReliablePosition}/>}
            >
              <Map height={400} viewCenter={gateway.location.position}>
                <ClusterContainer markers={gatewayMapMarker.get()}/>
              </Map>
            </HasContent>
          </TabContent>
        </Tabs>
      </Row>
    );
  }

  changeTab = (selectedTab: TabName) => this.setState({selectedTab});

}
