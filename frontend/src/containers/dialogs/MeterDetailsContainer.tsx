import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withEmptyContent, WithEmptyContentProps} from '../../components/hoc/withEmptyContent';
import {withLargeLoader} from '../../components/hoc/withLoaders';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {firstUpperTranslated} from '../../services/translationService';
import {getDomainModelById} from '../../state/domain-models/domainModelsSelectors';
import {fetchMeter} from '../../state/domain-models/meter-details/meterDetailsApiActions';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {addToReport} from '../../state/report/reportActions';
import {LegendItem} from '../../state/report/reportModels';
import {CallbackWithId, OnClickWith, uuid} from '../../types/Types';
import {emptyClusters, makeLeafletCompatibleMarkersFrom} from '../../usecases/map/helper/clusterHelper';
import {onCenterMap} from '../../usecases/map/mapActions';
import {fetchMeterMapMarker as fetchMapMarker} from '../../usecases/map/mapMarkerActions';
import {MapMarker, MapMarkerClusters, OnCenterMapEvent, SelectedId} from '../../usecases/map/mapModels';
import {syncWithMetering} from '../../usecases/meter/meterActions';
import './MeterDetailsContainer.scss';
import {MeterDetailsInfoContainer} from './MeterDetailsInfoContainer';
import {MeterDetailsTabs} from './MeterDetailsTabs';

interface StateToProps {
  isFetching: boolean;
  meter: Maybe<MeterDetails>;
  mapMarkerClusters: MapMarkerClusters;
}

interface DispatchToProps extends OnCenterMapEvent {
  addToReport: OnClickWith<LegendItem>;
  fetchMeter: CallbackWithId;
  fetchMapMarker: CallbackWithId;
  syncWithMetering: CallbackWithId;
}

interface OwnProps extends SelectedId {
  useCollectionPeriod: boolean;
}

type Props = StateToProps & DispatchToProps & OwnProps;

const MeterDetailsContent = (props: Props) => {
  const newProps = {...props, meter: props.meter.get()};
  return (
    <div>
      <MeterDetailsInfoContainer {...newProps}/>
      <MeterDetailsTabs {...newProps}/>
    </div>
  );
};

const noSuchMeterMessage = (id: Maybe<uuid>): string =>
  firstUpperTranslated('invalid meter') + id.map(id => ' "' + id + '"').orElse('');

const MeterDetailsContentWrapper = withEmptyContent<Props & WithEmptyContentProps>(MeterDetailsContent);

const LoadingMeterDetails = withLargeLoader<StateToProps & WithEmptyContentProps>(MeterDetailsContentWrapper);

const MeterDetailsComponent = (props: Props) => {
  const {fetchMeter, fetchMapMarker, selectedId} = props;
  React.useEffect(() => {
    selectedId.do((id: uuid) => {
      fetchMapMarker(id);
      fetchMeter(id);
    });
  });

  return (
    <LoadingMeterDetails
      {...props}
      isFetching={props.isFetching}
      hasContent={props.meter.isJust()}
      noContentText={noSuchMeterMessage(props.selectedId)}
    />
  );
};

const mapStateToProps = (
  {domainModels: {meterMapMarkers, meters}}: RootState,
  {selectedId}: OwnProps,
): StateToProps =>
  ({
    isFetching: meters.isFetching,
    meter: selectedId.flatMap((id: uuid) => getDomainModelById<MeterDetails>(id)(meters)),
    mapMarkerClusters: selectedId.flatMap((id: uuid) => getDomainModelById<MapMarker>(id)(meterMapMarkers))
      .map(makeLeafletCompatibleMarkersFrom)
      .orElseGet(() => emptyClusters),
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addToReport,
  fetchMeter,
  fetchMapMarker,
  onCenterMap,
  syncWithMetering,
}, dispatch);

export const MeterDetailsContainer = connect<StateToProps, DispatchToProps, OwnProps>(
  () => mapStateToProps,
  mapDispatchToProps,
)(MeterDetailsComponent);
