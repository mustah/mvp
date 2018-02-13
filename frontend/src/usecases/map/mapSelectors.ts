import {Maybe} from '../../helpers/Maybe';
import {Gateway} from '../../state/domain-models/gateway/gatewayModels';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {MapState} from './mapReducer';

export const getSelectedMeterMarker = (state: MapState): Maybe<Meter> =>
  Maybe.maybe(state.selectedMarker as Meter);

export const getSelectedGatewayMarker = (state: MapState): Maybe<Gateway> =>
  Maybe.maybe(state.selectedMarker as Gateway);
