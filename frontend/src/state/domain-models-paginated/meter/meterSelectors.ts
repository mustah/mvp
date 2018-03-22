import {Maybe} from '../../../helpers/Maybe';
import {uuid} from '../../../types/Types';
import {ObjectsById} from '../../domain-models/domainModels';
import {Gateway} from '../gateway/gatewayModels';
import {Meter, MetersState} from './meterModels';

export const getMeter = ({entities}: MetersState, id: uuid): Maybe<Meter> =>
  Maybe.maybe(entities[id]);

export const getMetersByGateway = ({entities}: MetersState, gateway: Maybe<Gateway>): Maybe<ObjectsById<Meter>> => {
  const allMetersExistInState = (prev, curr) => prev && !!entities[curr];
  return (
    gateway.flatMap(({meterIds}) =>
      meterIds.reduce(allMetersExistInState, true) ?
        Maybe.just(toMeterDict(meterIds, entities))
        : Maybe.nothing()));
};

const toMeterDict = (ids: uuid[], entities: ObjectsById<Meter>) =>
  ids.reduce((prev, curr) => ({...prev, [curr]: entities[curr]}), {});
