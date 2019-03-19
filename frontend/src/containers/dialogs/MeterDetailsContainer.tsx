import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withLargeLoader} from '../../components/hoc/withLoaders';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {getDomainModelById} from '../../state/domain-models/domainModelsSelectors';
import {fetchMeter} from '../../state/domain-models/meter-details/meterDetailsApiActions';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {CallbackWithId, OnClickWith, uuid} from '../../types/Types';
import {MapMarker, SelectedId} from '../../usecases/map/mapModels';
import {syncWithMetering} from '../../usecases/meter/meterActions';
import {addToReport} from '../../usecases/report/reportActions';
import {LegendItem} from '../../usecases/report/reportModels';
import './MeterDetailsContainer.scss';
import {MeterDetailsInfoContainer} from './MeterDetailsInfoContainer';
import {MeterDetailsTabsContainer} from './MeterDetailsTabs';

interface StateToProps {
  isFetching: boolean;
  meter: Maybe<MeterDetails>;
  meterMapMarker: Maybe<MapMarker>;
}

interface DispatchToProps {
  fetchMeter: CallbackWithId;
  addToReport: OnClickWith<LegendItem>;
  syncWithMetering: CallbackWithId;
}

interface OwnProps extends SelectedId {
  useCollectionPeriod?: boolean;
}

type Props = StateToProps & DispatchToProps & OwnProps;

const MeterDetailsContent = (props: Props) => {
  if (props.meter.isNothing()) {
    return null;
  }
  const newProps = {...props, meter: props.meter.get()};
  return (
    <div>
      <MeterDetailsInfoContainer {...newProps}/>
      <MeterDetailsTabsContainer {...newProps}/>
    </div>
  );
};

const LoadingMeterDetails = withLargeLoader<StateToProps>(MeterDetailsContent);

const MeterDetailsComponent = (props: Props) => {
  const {fetchMeter, selectedId} = props;
  React.useEffect(() => {
    selectedId.do((id: uuid) => {
      fetchMeter(id);
    });
  });

  return <LoadingMeterDetails {...props}/>;
};

const mapStateToProps = (
  {
    domainModels: {meterMapMarkers, meters},
    userSelection: {userSelection: {selectionParameters: {dateRange: periodDateRange}}},
  }: RootState,
  {selectedId}: OwnProps,
): StateToProps =>
  ({
    isFetching: meters.isFetching,
    meter: selectedId
      .flatMap((id: uuid) => getDomainModelById<MeterDetails>(id)(meters)),
    meterMapMarker: selectedId
      .flatMap((id: uuid) => getDomainModelById<MapMarker>(id)(meterMapMarkers)),
  });

const mapDispatchToProps = (dispatch) => bindActionCreators({
  addToReport,
  fetchMeter,
  syncWithMetering,
}, dispatch);

export const MeterDetailsContainer = connect<StateToProps, DispatchToProps, OwnProps>(
  () => mapStateToProps,
  mapDispatchToProps,
)(MeterDetailsComponent);
