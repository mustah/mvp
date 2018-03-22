import {Maybe} from '../../../../helpers/Maybe';
import {Identifiable} from '../../../../types/Types';
import {Gateway} from '../../gateway/gatewayModels';
import {NormalizedPaginatedState} from '../../paginatedDomainModels';
import {Meter, MetersState} from '../meterModels';
import {getMeter, getMetersByGateway} from '../meterSelectors';

describe('meterSelectors', () => {
  describe('getMeter', () => {
    const meterState: Partial<NormalizedPaginatedState<Partial<Meter> & Identifiable>> = {
      entities: {1: {id: 1}},
    };
    it('returns a Maybe.just() when requesting existing gateway', () => {
      expect(getMeter(meterState as MetersState, 1)).toEqual(Maybe.just({id: 1}));
    });
    it('returns a maybe.nothing when requesting a non-existing gateway', () => {
      expect(getMeter(meterState as MetersState, 2)).toEqual(Maybe.nothing());
    });
  });

  describe('getMetersByGateway', () => {
    const metersState: Partial<NormalizedPaginatedState<Partial<Meter> & Identifiable>> = {
      entities: {
        1: {id: 1},
        2: {id: 2},
        3: {id: 3},
      },
    };

    it('returns a Maybe.nothing() if gateway is maybe.nothing()', () => {
      const gateway: Maybe<Gateway> = Maybe.nothing();

      expect(getMetersByGateway(metersState as MetersState, gateway)).toEqual(Maybe.nothing());
    });

    it('returns a Maybe.nothing() if any of the gateway.meterIds are missing i metersState', () => {
      const gateway: Maybe<Partial<Gateway>> = Maybe.just({meterIds: [1, 2, 3, 4]});

      expect(getMetersByGateway(metersState as MetersState, gateway as Maybe<Gateway>)).toEqual(Maybe.nothing());
    });

    it('returns a Maybe.just() if all gateway.meterIds exist in metersState', () => {
      const gateway: Maybe<Partial<Gateway>> = Maybe.just({meterIds: [1, 2]});

      expect(getMetersByGateway(metersState as MetersState, gateway as Maybe<Gateway>))
        .toEqual(Maybe.just({1: {id: 1}, 2: {id: 2}}));
    });
  });
});
