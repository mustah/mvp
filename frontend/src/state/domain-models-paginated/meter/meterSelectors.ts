import {createSelector} from 'reselect';
import {Maybe} from '../../../helpers/Maybe';
import {uuid} from '../../../types/Types';
import {NormalizedState, ObjectsById} from '../../domain-models/domainModels';
import {getEntitiesDomainModels} from '../../domain-models/domainModelsSelectors';
import {MeterDetails} from '../../domain-models/meter-details/meterDetailsModels';

export const getMeterDetailsByIds = (meterIds: uuid[]) =>
  createSelector<NormalizedState<MeterDetails>, ObjectsById<MeterDetails>, Maybe<ObjectsById<MeterDetails>>>(
    getEntitiesDomainModels,
    (entities: ObjectsById<MeterDetails>) => {
      const metersInState = meterIds.filter((id: uuid) => entities[id] !== undefined);
      return metersInState.length > 0 && (metersInState.length === meterIds.length)
        ? Maybe.just(metersInState.reduce((prev, curr: uuid) => ({...prev, [curr]: entities[curr]}), {}))
        : Maybe.nothing();
    },
  );
