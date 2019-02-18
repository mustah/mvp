import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withLargeLoader} from '../../components/hoc/withLoaders';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {getDomainModelById} from '../../state/domain-models/domainModelsSelectors';
import {fetchMeterDetails} from '../../state/domain-models/meter-details/meterDetailsApiActions';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {CallbackWithId, CallbackWithIds, OnClickWith, uuid} from '../../types/Types';
import {fetchMeterMapMarker} from '../../usecases/map/mapMarkerActions';
import {MapMarker, SelectedId} from '../../usecases/map/mapModels';
import {syncWithMetering} from '../../usecases/meter/meterActions';
import {addToReport} from '../../usecases/report/reportActions';
import {LegendItem} from '../../usecases/report/reportModels';
import {useFetchMeterAndMapMarker} from './fetchDialogDataHook';
import './MeterDetailsContainer.scss';
import {MeterDetailsInfoContainer} from './MeterDetailsInfoContainer';
import {MeterDetailsTabsContainer} from './MeterDetailsTabs';

interface StateToProps {
  isFetching: boolean;
  meter: Maybe<MeterDetails>;
  meterMapMarker: Maybe<MapMarker>;
  periodDateRange: SelectionInterval;
}

interface DispatchToProps {
  fetchMeterDetails: CallbackWithIds;
  fetchMeterMapMarker: CallbackWithId;
  addToReport: OnClickWith<LegendItem>;
  syncWithMetering: CallbackWithId;
}

type Props = StateToProps & DispatchToProps & SelectedId;

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
  const {periodDateRange, fetchMeterDetails, fetchMeterMapMarker, selectedId} = props;
  useFetchMeterAndMapMarker({periodDateRange, fetchMeterDetails, fetchMeterMapMarker, selectedId});

  return <LoadingMeterDetails {...props}/>;
};

const mapStateToProps = (
  {
    domainModels: {meterMapMarkers, meters},
    userSelection: {userSelection: {selectionParameters: {dateRange: periodDateRange}}},
  }: RootState,
  {selectedId}: SelectedId,
): StateToProps => ({
  isFetching: [meterMapMarkers, meters].some((models) => models.isFetching),
  periodDateRange,
  meter: selectedId
    .flatMap((id: uuid) => getDomainModelById<MeterDetails>(id)(meters)),
  meterMapMarker: selectedId
    .flatMap((id: uuid) => getDomainModelById<MapMarker>(id)(meterMapMarkers)),
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  addToReport,
  fetchMeterDetails,
  fetchMeterMapMarker,
  syncWithMetering,
}, dispatch);

export const MeterDetailsContainer = connect<StateToProps, DispatchToProps, SelectedId>(
  () => mapStateToProps,
  mapDispatchToProps,
)(MeterDetailsComponent);
