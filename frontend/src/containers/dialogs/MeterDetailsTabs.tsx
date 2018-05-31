import 'MeterDetailsTabs.scss';
import * as React from 'react';
import {ListActionsDropdown} from '../../components/actions-dropdown/ListActionsDropdown';
import {HasContent} from '../../components/content/HasContent';
import {DateTime} from '../../components/dates/DateTime';
import {Row} from '../../components/layouts/row/Row';
import {Separator} from '../../components/separators/Separator';
import {Status} from '../../components/status/Status';
import {Table, TableColumn} from '../../components/table/Table';
import {TableHead} from '../../components/table/TableHead';
import {Tab} from '../../components/tabs/components/Tab';
import {TabContent} from '../../components/tabs/components/TabContent';
import {TabHeaders} from '../../components/tabs/components/TabHeaders';
import {Tabs} from '../../components/tabs/components/Tabs';
import {TabSettings} from '../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../components/tabs/components/TabTopBar';
import {Normal} from '../../components/texts/Texts';
import {timestamp} from '../../helpers/dateHelpers';
import {roundMeasurement} from '../../helpers/formatters';
import {Maybe} from '../../helpers/Maybe';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Gateway, GatewayMandatory} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {Meter, MeterStatusChangelog} from '../../state/domain-models-paginated/meter/meterModels';
import {DomainModel} from '../../state/domain-models/domainModels';
import {Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {Children, Identifiable, OnClickWithId} from '../../types/Types';
import {Map} from '../../usecases/map/components/Map';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {MapMarker} from '../../usecases/map/mapModels';
import {meterMeasurementsForTable, normalizedStatusChangelogFor} from './dialogHelper';

interface State {
  selectedTab: TabName;
}

interface Props {
  meter: Meter;
  meterMapMarker: Maybe<MapMarker>;
  selectEntryAdd: OnClickWithId;
  syncWithMetering: OnClickWithId;
}

export interface RenderableMeasurement extends Identifiable {
  quantity: Quantity;
  value?: number | string;
  unit?: string;
  created?: number;
}

const renderQuantity = ({quantity}: RenderableMeasurement): string => quantity as string;

const renderValue = ({value = null, unit}: RenderableMeasurement): string =>
  value !== null && unit ? `${roundMeasurement(value)} ${unit}` : '';

const renderCreated = ({created}: RenderableMeasurement): Children =>
  created
    ? timestamp(created * 1000)
    : <Normal className="Italic">{firstUpperTranslated('never collected')}</Normal>;

const renderStatusCell = ({name}: MeterStatusChangelog): Children => <Status name={name}/>;

const renderDate = ({start}: MeterStatusChangelog): Children =>
  <DateTime date={start} fallbackContent={<Separator/>}/>;

const renderSerial = ({serial}: Gateway): string => serial;

export class MeterDetailsTabs extends React.Component<Props, State> {

  state: State = {selectedTab: TabName.values};

  render() {
    const {selectedTab} = this.state;
    const {meter, meterMapMarker, selectEntryAdd, syncWithMetering} = this.props;

    const gateway = meter.gateway;

    const normalizedGateways: DomainModel<GatewayMandatory> = {
      entities: {[gateway.id]: gateway},
      result: [gateway.id],
    };

    const statusChangelog = normalizedStatusChangelogFor(meter);
    const measurements: DomainModel<RenderableMeasurement> = meterMeasurementsForTable(meter);

    const noReliablePosition =
      <h2 style={{padding: 8}}>{firstUpperTranslated('no reliable position')}</h2>;

    return (
      <Row>
        <Tabs className="full-width first-letter">
          <TabTopBar>
            <TabHeaders selectedTab={selectedTab} onChangeTab={this.changeTab}>
              <Tab tab={TabName.values} title={translate('latest value')}/>
              <Tab tab={TabName.log} title={translate('status log')}/>
              <Tab tab={TabName.map} title={translate('map')}/>
              <Tab tab={TabName.connectedGateways} title={translate('gateways')}/>
            </TabHeaders>
            <TabSettings>
              <ListActionsDropdown
                item={{id: meter.id, name: meter.manufacturer}}
                selectEntryAdd={selectEntryAdd}
                syncWithMetering={syncWithMetering}
              />
            </TabSettings>
          </TabTopBar>
          <TabContent tab={TabName.values} selectedTab={selectedTab}>
            <Table {...measurements} className="Measurements">
              <TableColumn
                header={<TableHead className="first">{translate('quantity')}</TableHead>}
                renderCell={renderQuantity}
              />
              <TableColumn
                header={<TableHead>{translate('value')}</TableHead>}
                renderCell={renderValue}
              />
              <TableColumn
                header={<TableHead>{translate('collected at')}</TableHead>}
                renderCell={renderCreated}
              />
            </Table>
          </TabContent>
          <TabContent tab={TabName.log} selectedTab={selectedTab}>
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
            <HasContent hasContent={meterMapMarker.isJust()} fallbackContent={noReliablePosition}>
              <Map height={400} viewCenter={meter.location.position}>
                {meterMapMarker.isJust() && <ClusterContainer markers={meterMapMarker.get()}/>}
              </Map>
            </HasContent>
          </TabContent>
          <TabContent tab={TabName.connectedGateways} selectedTab={selectedTab}>
            <Row>
              <Table result={normalizedGateways.result} entities={normalizedGateways.entities}>
                <TableColumn
                  header={<TableHead>{translate('gateway serial')}</TableHead>}
                  renderCell={renderSerial}
                />
              </Table>
            </Row>
          </TabContent>
        </Tabs>
      </Row>
    );
  }

  changeTab = (selectedTab: TabName) => this.setState({selectedTab});

}
