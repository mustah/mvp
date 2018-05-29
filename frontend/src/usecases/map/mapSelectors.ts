import {createSelector} from 'reselect';
import {Maybe} from '../../helpers/Maybe';
import {RootState} from '../../reducers/rootReducer';
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

export const getBounds =
  createSelector<NormalizedState<MapMarker>, DomainModel<MapMarker>, Bounds>(
    getDomainModel,
    ({entities}: DomainModel<MapMarker>) => boundsFromMarkers(entities),
  );

const getTotalMeters = (state: RootState): number => state.summary.payload.numMeters;
const getMeterMapMarkers = (state: RootState) => state.domainModels.meterMapMarkers;
const getGatewayMapMarkers = (state: RootState) => state.domainModels.gatewayMapMarkers;

const totalMapMarkers = (markers: NormalizedState<MapMarker>): number => markers.total;

const getTotalMeterMapMarkers =
  createSelector<RootState, NormalizedState<MapMarker>, number>(
    getMeterMapMarkers,
    totalMapMarkers,
  );

const getTotalGatewayMapMarkers =
  createSelector<RootState, NormalizedState<MapMarker>, number>(
    getGatewayMapMarkers,
    totalMapMarkers,
  );

export const getMeterLowConfidenceTextInfo =
  createSelector<RootState, number, number, string | undefined>(
    getTotalMeters,
    getTotalMeterMapMarkers,
    meterLowConfidenceTextInfo,
  );

export const getGatewayLowConfidenceTextInfo =
  createSelector<RootState, number, number, string | undefined>(
    getTotalMeters,
    getTotalGatewayMapMarkers,
    gatewayLowConfidenceTextInfo,
  );
