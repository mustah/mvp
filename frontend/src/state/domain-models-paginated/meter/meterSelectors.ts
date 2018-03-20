import {Maybe} from '../../../helpers/Maybe';
import {uuid} from '../../../types/Types';
import {ObjectsById} from '../../domain-models/domainModels';
import {Gateway} from '../gateway/gatewayModels';
import {NormalizedPaginatedState} from '../paginatedDomainModels';
import {Meter} from './meterModels';

export const getMeter = ({entities}: NormalizedPaginatedState<Meter>, id: uuid): Maybe<Meter> =>
  Maybe.maybe(entities[id]);

export const getMetersByGateway = (
  {entities}: NormalizedPaginatedState<Meter>,
  gateway: Maybe<Gateway>,
): Maybe<ObjectsById<Meter>> => {
  return (
    gateway.flatMap(({meterIds}) => meterIds.reduce((
      prev,
      curr,
    ) => prev && !!entities[curr], true) ? Maybe.just(toMeterDict(meterIds, entities)) : Maybe.nothing()));
};

const toMeterDict = (ids, entities) => ids.reduce((prev, curr) => ({...prev, [curr]: entities[curr]}), {});
