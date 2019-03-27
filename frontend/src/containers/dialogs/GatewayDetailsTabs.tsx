import * as React from 'react';
import {OpenDialogInfoButton} from '../../components/dialog/OpenDialogInfoButton';
import {withEmptyContent, WithEmptyContentProps} from '../../components/hoc/withEmptyContent';
import {Row, RowMiddle} from '../../components/layouts/row/Row';
import {MeterAlarm} from '../../components/status/MeterAlarm';
import {Table, TableColumn} from '../../components/table/Table';
import {TableHead} from '../../components/table/TableHead';
import {Tab} from '../../components/tabs/components/Tab';
import {TabContent} from '../../components/tabs/components/TabContent';
import {TabHeaders} from '../../components/tabs/components/TabHeaders';
import {Tabs} from '../../components/tabs/components/Tabs';
import {TabTopBar} from '../../components/tabs/components/TabTopBar';
import {Normal} from '../../components/texts/Texts';
import {formatCollectionPercentage} from '../../helpers/formatters';
import {Maybe} from '../../helpers/Maybe';
import {orUnknown} from '../../helpers/translations';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {Gateway} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {TabName} from '../../state/ui/tabs/tabsModels';
import {uuid} from '../../types/Types';
import {Map} from '../../usecases/map/components/Map';
import {ClusterContainer} from '../../usecases/map/containers/ClusterContainer';
import {MapMarker} from '../../usecases/map/mapModels';
import './GatewayDetailsTabs.scss';
import {MeterDetailsContainer} from './MeterDetailsContainer';

interface SuperAdmin {
  isSuperAdmin: boolean;
}

interface OwnProps {
  gateway: Gateway;
  gatewayMapMarker: Maybe<MapMarker>;
  meters: ObjectsById<Meter>;
}

interface TabsState {
  selectedTab: TabName;
}

const iconStyle: React.CSSProperties = {
  marginLeft: 0,
  marginRight: 4,
  padding: 0,
  width: 24,
  height: 24,
};

const renderAlarm = ({alarms}: Meter) => <MeterAlarm items={alarms}/>;

const renderFacility = ({id, facility}: Meter) => {
  const selectedId: Maybe<uuid> = Maybe.maybe(id);
  return (
    <OpenDialogInfoButton
      label={facility}
      autoScrollBodyContent={true}
      iconStyle={iconStyle}
    >
      <MeterDetailsContainer selectedId={selectedId} useCollectionPeriod={false}/>
    </OpenDialogInfoButton>
  );
};

const renderMeterAddressAndReported = ({address, isReported}: Meter) => {
  const label = address ? address : firstUpperTranslated('unknown');
  return (
    <RowMiddle>
      <Normal>{label}</Normal>
      <MeterAlarm items={isReported} label={translate('reported')}/>
    </RowMiddle>
  );
};

const renderManufacturer = ({manufacturer}: Meter) => orUnknown(manufacturer);

const renderMedium = ({medium}: Meter) => medium;

const MapContent = ({gateway, gatewayMapMarker}: OwnProps) => (
  <Map height={400} viewCenter={gateway.location.position}>
    {gatewayMapMarker.isJust() && <ClusterContainer markers={gatewayMapMarker.get()}/>}
  </Map>
);

type MapProps = OwnProps & WithEmptyContentProps;
type Props = SuperAdmin & OwnProps;

const MapContentWrapper = withEmptyContent<MapProps>(MapContent);

export class GatewayDetailsTabs extends React.Component<Props, TabsState> {

  state: TabsState = {selectedTab: TabName.values};

  render() {
    const {selectedTab} = this.state;
    const {gateway, meters, gatewayMapMarker, isSuperAdmin} = this.props;

    const wrapperProps: MapProps = {
      gateway,
      meters,
      gatewayMapMarker,
      hasContent: gatewayMapMarker.isJust(),
      noContentText: firstUpperTranslated('no reliable position'),
    };

    const renderCollectionStatus = ({collectionPercentage, readIntervalMinutes}: Meter) =>
      formatCollectionPercentage(collectionPercentage, readIntervalMinutes, isSuperAdmin);

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
            <Table result={gateway.meterIds} entities={meters} className="GatewayMeters">
              <TableColumn
                header={<TableHead>{translate('facility id')}</TableHead>}
                renderCell={renderFacility}
              />
              <TableColumn
                header={<TableHead>{translate('meter')}</TableHead>}
                renderCell={renderMeterAddressAndReported}
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
                header={<TableHead>{translate('alarm')}</TableHead>}
                renderCell={renderAlarm}
              />
              <TableColumn
                cellClassName="number"
                header={<TableHead className="number">{translate('collection percentage')}</TableHead>}
                renderCell={renderCollectionStatus}
              />
            </Table>
          </TabContent>
          <TabContent tab={TabName.map} selectedTab={selectedTab}>
            <MapContentWrapper {...wrapperProps}/>
          </TabContent>
        </Tabs>
      </Row>
    );
  }

  changeTab = (selectedTab: TabName) => this.setState({selectedTab});

}
