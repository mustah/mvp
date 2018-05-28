import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withLargeLoader} from '../../helpers/hoc';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {fetchMeter} from '../../state/domain-models-paginated/meter/meterApiActions';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {getMeter} from '../../state/domain-models-paginated/meter/meterSelectors';
import {CallbackWithId, uuid} from '../../types/Types';
import {MapMarker} from '../../usecases/map/mapModels';
import {getMapMarker} from '../../usecases/map/mapSelectors';
import {fetchMeterMapMarker} from '../../usecases/map/meterMapMarkerApiActions';
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
  meter: Maybe<Meter>;
  meterMapMarker: Maybe<MapMarker>;
}

interface DispatchToProps {
  fetchMeter: CallbackWithId;
  fetchMeterMapMarker: CallbackWithId;
  selectEntryAdd: CallbackWithId;
  syncWithMetering: CallbackWithId;
}

type MeterDetailsProps = StateToProps & DispatchToProps & OwnProps;

const MeterDetailsContent = (props: MeterDetailsProps) => {
  const newProps = {...props, meter: props.meter.get()};
  return (
    <div>
      <MeterDetailsInfoContainer {...newProps}/>
      <MeterDetailsTabs {...newProps}/>
    </div>
  );
};

const LoadingMeterDetails = withLargeLoader<StateToProps>(MeterDetailsContent);

class MeterDetails extends React.Component<MeterDetailsProps> {

  componentDidMount() {
    const {fetchMeter, fetchMeterMapMarker, meterId} = this.props;
    fetchMeter(meterId);
    fetchMeterMapMarker(meterId);
  }

  componentWillReceiveProps({fetchMeter, fetchMeterMapMarker, meterId}: MeterDetailsProps) {
    fetchMeter(meterId);
    fetchMeterMapMarker(meterId);
  }

  render() {
    return <LoadingMeterDetails {...this.props}/>;
  }
}

const mapStateToProps = (
  {
    domainModels: {meterMapMarkers},
    paginatedDomainModels: {meters},
  }: RootState,
  {meterId}: OwnProps,
): StateToProps =>
  ({
    isFetching: meterMapMarkers.isFetching || meters.isFetchingSingle,
    meter: getMeter(meters, meterId),
    meterMapMarker: getMapMarker(meterMapMarkers, meterId),
  });

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchMeter,
  fetchMeterMapMarker,
  selectEntryAdd,
  syncWithMetering,
}, dispatch);

export const MeterDetailsContainer = connect<StateToProps, DispatchToProps, OwnProps>(
  () => mapStateToProps,
  mapDispatchToProps,
)(MeterDetails);
