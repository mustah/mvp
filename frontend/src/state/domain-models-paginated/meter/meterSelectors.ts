import {createSelector} from 'reselect';
import {Maybe} from '../../../helpers/Maybe';
import {uuid} from '../../../types/Types';
import {ObjectsById} from '../../domain-models/domainModels';
import {getPaginatedEntities} from '../paginatedDomainModelsSelectors';
import {Meter, MetersState} from './meterModels';

export const getMetersByIds = (meterIds: uuid[]) =>
  createSelector<MetersState, ObjectsById<Meter>, Maybe<ObjectsById<Meter>>>(
    getPaginatedEntities,
    (entities: ObjectsById<Meter>) => {
      const metersInState = meterIds.filter((id: uuid) => entities[id] !== undefined);
      return metersInState.length > 0 && (metersInState.length === meterIds.length)
        ? Maybe.just(metersInState.reduce(
          (prev, curr: uuid) => ({...prev, [curr]: entities[curr]}), {},
        ))
        : Maybe.nothing();
    },
  );
