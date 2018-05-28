import {Maybe} from '../../../../helpers/Maybe';
import {Identifiable} from '../../../../types/Types';
import {ObjectsById} from '../../../domain-models/domainModels';
import {NormalizedPaginatedState} from '../../paginatedDomainModels';
import {getPaginatedDomainModelById} from '../../paginatedDomainModelsSelectors';
import {Meter, MetersState} from '../meterModels';
import {getMetersByIds} from '../meterSelectors';

describe('meterSelectors', () => {

  type IdentifiableMeter = ObjectsById<Partial<Meter> & Identifiable>;

  describe('getPaginatedDomainModelById - Meter', () => {
    const meterState: Partial<NormalizedPaginatedState<Partial<Meter> & Identifiable>> = {
      entities: {1: {id: 1}},
    };

    it('returns a Maybe.just() when requesting existing gateway', () => {
      const state = meterState as MetersState;

      const actual: Maybe<Meter> = getPaginatedDomainModelById<Meter>(1)(state);

      expect(actual).toEqual(Maybe.just<Partial<Meter>>({id: 1}));
    });

    it('returns a maybe.nothing when requesting a non-existing gateway', () => {
      const state = meterState as MetersState;

      const actual: Maybe<Meter> = getPaginatedDomainModelById<Meter>(2)(state);

      expect(actual).toEqual(Maybe.nothing<Meter>());
    });
  });

  describe('getMetersById', () => {
    const metersState: Partial<NormalizedPaginatedState<Partial<Meter> & Identifiable>> = {
      entities: {
        1: {id: 1},
        2: {id: 2},
        3: {id: 3},
      },
    };
    const state = metersState as MetersState;

    it('returns a Maybe.nothing() if gateway is maybe.nothing()', () => {
      const actual: Maybe<ObjectsById<Meter>> = getMetersByIds([])(state);

      expect(actual).toEqual(Maybe.nothing<ObjectsById<Meter>>());
    });

    it('returns a Maybe.nothing() if any of the gateway.meterIds are missing i metersState', () => {
      const actual: Maybe<ObjectsById<Meter>> = getMetersByIds([1, 2, 3, 4])(state);

      expect(actual).toEqual(Maybe.nothing<ObjectsById<Meter>>());
    });

    it('returns a Maybe.just() if all gateway.meterIds exist in metersState', () => {
      const actual: Maybe<ObjectsById<Meter>> = getMetersByIds([1, 2])(state);

      expect(actual).toEqual(Maybe.just<IdentifiableMeter>({1: {id: 1}, 2: {id: 2}}));
    });
  });
});
