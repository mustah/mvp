import {Maybe} from '../../../../helpers/Maybe';
import {uuid} from '../../../../types/Types';
import {Gateway} from '../gatewayModels';
import {getGatewayMeterIdsFrom} from '../gatewaySelectors';

describe('gatewaySelectors', () => {

  describe('getGatewayMeterIdsFrom', () => {

    it('gets meter ids from gateway', () => {
      const gateway: Maybe<Partial<Gateway>> = Maybe.maybe({meterIds: [1, 2, 3]});

      const meterIds: uuid[] = getGatewayMeterIdsFrom(gateway as Maybe<Gateway>);

      expect(meterIds).toEqual([1, 2, 3]);
    });

    it('return empty list when gateway does not have any meters connected', () => {
      const gateway: Maybe<Partial<Gateway>> = Maybe.maybe({meterIds: []});

      const meterIds: uuid[] = getGatewayMeterIdsFrom(gateway as Maybe<Gateway>);
      const emptyMeterIds: uuid[] = [];

      expect(meterIds).toEqual(emptyMeterIds);
    });

    it('returns the same instance when gateway meter ids have been memoized', () => {
      const gateway: Maybe<Partial<Gateway>> = Maybe.maybe({meterIds: [1, 2, 3]});

      const meterIds: uuid[] = getGatewayMeterIdsFrom(gateway as Maybe<Gateway>);
      const actual: uuid[] = getGatewayMeterIdsFrom(gateway as Maybe<Gateway>);

      expect(meterIds).toEqual([1, 2, 3]);
      expect(actual).toBe(meterIds);
    });

    it('does not return the same instance with two different gateways', () => {
      const gateway1: Maybe<Partial<Gateway>> = Maybe.maybe({meterIds: [1, 2]});
      const gateway2: Maybe<Partial<Gateway>> = Maybe.maybe({meterIds: [3]});

      const meterIds: uuid[] = getGatewayMeterIdsFrom(gateway1 as Maybe<Gateway>);
      const expected: uuid[] = getGatewayMeterIdsFrom(gateway2 as Maybe<Gateway>);

      expect(meterIds).not.toBe(expected);
    });

  });
});
