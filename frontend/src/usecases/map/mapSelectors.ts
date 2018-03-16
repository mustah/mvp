import {Maybe} from '../../helpers/Maybe';
import {NormalizedState} from '../../state/domain-models/domainModels';
import {uuid} from '../../types/Types';
import {MapMarker} from './mapModels';
import {MapState} from './mapReducer';

export const getSelectedMapMarker = (state: MapState): Maybe<uuid> =>
  Maybe.maybe(state.selectedMarker);

export const getMapMarker = ({entities}: NormalizedState<MapMarker>, id: uuid): Maybe<MapMarker> =>
  Maybe.maybe(entities[id]);
