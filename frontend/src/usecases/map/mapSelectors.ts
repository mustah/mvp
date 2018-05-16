import {createSelector} from 'reselect';
import {Maybe} from '../../helpers/Maybe';
import {DomainModel, NormalizedState} from '../../state/domain-models/domainModels';
import {getDomainModel} from '../../state/domain-models/domainModelsSelectors';
import {uuid} from '../../types/Types';
import {
  boundsFromMarkers,
  gatewayLowConfidenceTextInfo,
  meterLowConfidenceTextInfo,
} from './helper/mapHelper';
import {Bounds, MapMarker} from './mapModels';
import {MapState} from './mapReducer';

export const getSelectedMapMarker = (state: MapState): Maybe<uuid> =>
  Maybe.maybe(state.selectedMarker);

export const getMapMarker = ({entities}: NormalizedState<MapMarker>, id: uuid): Maybe<MapMarker> =>
  Maybe.maybe(entities[id]);

export const getBounds =
  createSelector<NormalizedState<MapMarker>, DomainModel<MapMarker>, Bounds>(
    getDomainModel,
    ({entities}: DomainModel<MapMarker>) => boundsFromMarkers(entities),
  );

export const getMeterLowConfidenceTextInfo =
  createSelector<NormalizedState<MapMarker>, DomainModel<MapMarker>, string | undefined>(
    getDomainModel,
    meterLowConfidenceTextInfo,
  );

export const getGatewayLowConfidenceTextInfo =
  createSelector<NormalizedState<MapMarker>, DomainModel<MapMarker>, string | undefined>(
    getDomainModel,
    gatewayLowConfidenceTextInfo,
  );
