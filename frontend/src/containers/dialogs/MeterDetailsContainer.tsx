import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
import {fetchMeter} from '../../state/domain-models-paginated/meter/meterApiActions';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {getMeter} from '../../state/domain-models-paginated/meter/meterSelectors';
import {CallbackWithId, uuid} from '../../types/Types';
import {MapMarker} from '../../usecases/map/mapModels';
import {getMapMarker} from '../../usecases/map/mapSelectors';
import {selectEntryAdd} from '../../usecases/report/reportActions';
import {syncWithMetering} from '../../usecases/validation/validationActions';
import './MeterDetailsContainer.scss';
import {MeterDetailsInfo} from './MeterDetailsInfo';
import {MeterDetailsTabs} from './MeterDetailsTabs';

interface OwnProps {
  meterId: uuid;
}

interface StateToProps {
  meter: Maybe<Meter>;
  meterMapMarker: Maybe<MapMarker>;
}

interface DispatchToProps {
  fetchMeter: CallbackWithId;
  selectEntryAdd: CallbackWithId;
  syncWithMetering: CallbackWithId;
}

type MeterDetailsContainerProps = StateToProps & DispatchToProps & OwnProps;

class MeterDetails extends React.Component<MeterDetailsContainerProps> {

  componentDidMount() {
    const {fetchMeter, meterId} = this.props;
    fetchMeter(meterId);
  }

  componentWillReceiveProps({fetchMeter, meterId}: MeterDetailsContainerProps) {
    fetchMeter(meterId);
  }

  render() {
    if (this.props.meter.isJust()) {
      const newProps = {...this.props, meter: this.props.meter.get()};
      return (
        <div>
          <MeterDetailsInfo {...newProps}/>
          <MeterDetailsTabs {...newProps}/>
        </div>
      );
    } else {
      return (<div/>);
    }
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
    meter: getMeter(meters, meterId),
    meterMapMarker: getMapMarker(meterMapMarkers, meterId),
  });

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchMeter,
  selectEntryAdd,
  syncWithMetering,
}, dispatch);

export const MeterDetailsContainer = connect<StateToProps, DispatchToProps, OwnProps>(
  () => mapStateToProps,
  mapDispatchToProps,
)(MeterDetails);
