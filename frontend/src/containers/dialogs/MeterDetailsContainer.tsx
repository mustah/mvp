import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {now} from '../../helpers/dateHelpers';
import {withLargeLoader} from '../../helpers/hoc';
import {Maybe} from '../../helpers/Maybe';
import {makeApiParametersOf} from '../../helpers/urlFactory';
import {RootState} from '../../reducers/rootReducer';
import {getDomainModelById} from '../../state/domain-models/domainModelsSelectors';
import {fetchMeterDetails} from '../../state/domain-models/meter-details/meterDetailsApiActions';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {CallbackWithId, uuid} from '../../types/Types';
import {fetchMeterMapMarker} from '../../usecases/map/mapMarkerActions';
import {MapMarker} from '../../usecases/map/mapModels';
import {selectEntryAdd} from '../../usecases/report/reportActions';
import {syncWithMetering} from '../../usecases/validation/validationActions';
import './MeterDetailsContainer.scss';
import {MeterDetailsInfoContainer} from './MeterDetailsInfo';
import {MeterDetailsTabs} from './MeterDetailsTabs';

interface OwnProps {
  meterId: uuid;
}

interface StateToProps {
  isFetching: boolean;
  meter: Maybe<MeterDetails>;
  meterMapMarker: Maybe<MapMarker>;
  dateRange: SelectionInterval;
}

interface DispatchToProps {
  fetchMeterDetails: CallbackWithId;
  fetchMeterMapMarker: CallbackWithId;
  selectEntryAdd: CallbackWithId;
  syncWithMetering: CallbackWithId;
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
      <MeterDetailsTabs {...newProps}/>
    </div>
  );
};

const LoadingMeterDetails = withLargeLoader<StateToProps>(MeterDetailsContent);

const fetchMeterAndMapMarker =
  ({dateRange, fetchMeterDetails, fetchMeterMapMarker, meterId}: Props) => {
    fetchMeterDetails(meterId, makeApiParametersOf(now(), dateRange));
    fetchMeterMapMarker(meterId);
  };

class MeterDetailsComponent extends React.Component<Props> {

  componentDidMount() {
    fetchMeterAndMapMarker(this.props);
  }

  componentWillReceiveProps(props: Props) {
    fetchMeterAndMapMarker(props);
  }

  render() {
    return <LoadingMeterDetails {...this.props}/>;
  }
}

const mapStateToProps = (
  {
    domainModels: {meterMapMarkers, meters},
    userSelection: {userSelection: {selectionParameters: {dateRange}}},
  }: RootState,
  {meterId}: OwnProps,
): StateToProps => ({
  dateRange,
  isFetching: [meterMapMarkers, meters].some((models) => models.isFetching),
  meter: getDomainModelById<MeterDetails>(meterId)(meters),
  meterMapMarker: getDomainModelById<MapMarker>(meterId)(meterMapMarkers),
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchMeterDetails,
  fetchMeterMapMarker,
  selectEntryAdd,
  syncWithMetering,
}, dispatch);

export const MeterDetailsContainer = connect<StateToProps, DispatchToProps, OwnProps>(
  () => mapStateToProps,
  mapDispatchToProps,
)(MeterDetailsComponent);
