import {Maybe} from '../../../../helpers/Maybe';
import {Identifiable} from '../../../../types/Types';
import {NormalizedState, ObjectsById} from '../../../domain-models/domainModels';
import {MeterDetails} from '../../../domain-models/meter-details/meterDetailsModels';
import {Meter} from '../meterModels';
import {getMeterDetailsByIds} from '../meterSelectors';

describe('meterSelectors', () => {

  type IdentifiableMeter = ObjectsById<Partial<Meter> & Identifiable>;

  describe('getMetersById', () => {
    const metersState: Partial<NormalizedState<Partial<MeterDetails> & Identifiable>> = {
      entities: {
        1: {id: 1},
        2: {id: 2},
        3: {id: 3},
      },
    };
    const state = metersState as NormalizedState<MeterDetails>;

    it('returns a Maybe.nothing() if gateway is maybe.nothing()', () => {
      const actual: Maybe<ObjectsById<Meter>> = getMeterDetailsByIds([])(state);

      expect(actual).toEqual(Maybe.nothing<ObjectsById<Meter>>());
    });

    it('returns a Maybe.nothing() if any of the gateway.meterIds are missing i metersState', () => {
      const actual: Maybe<ObjectsById<Meter>> = getMeterDetailsByIds([1, 2, 3, 4])(state);

      expect(actual).toEqual(Maybe.nothing<ObjectsById<Meter>>());
    });

    it('returns a Maybe.just() if all gateway.meterIds exist in metersState', () => {
      const actual: Maybe<ObjectsById<Meter>> = getMeterDetailsByIds([1, 2])(state);

      expect(actual).toEqual(Maybe.just<IdentifiableMeter>({1: {id: 1}, 2: {id: 2}}));
    });
  });
});
