import {Gateway} from '../../state/domain-models/gateway/gatewayModels';
import {Meter} from '../../state/domain-models/meter/meterModels';
import {Maybe} from '../../types/Types';
import {MapState} from './mapReducer';

export const getSelectedMeterMarker = (state: MapState): Maybe<Meter> =>
  state.selectedMarker && state.selectedMarker as Meter;

export const getSelectedGatewayMarker = (state: MapState): Maybe<Gateway> =>
  state.selectedMarker && state.selectedMarker as Gateway;
