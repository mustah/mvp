import {Maybe} from '../../../../helpers/Maybe';
import {Identifiable} from '../../../../types/Types';
import {NormalizedPaginatedState} from '../../paginatedDomainModels';
import {Gateway, GatewaysState} from '../gatewayModels';
import {getGateway} from '../gatewaySelectors';

describe('gatewaySelectors', () => {
  describe('getGateway', () => {
    const gatewayState: Partial<NormalizedPaginatedState<Partial<Gateway> & Identifiable>> = {
      entities: {1: {id: 1}},
    };
    it('returns a Maybe.just() when requesting existing gateway', () => {
      expect(getGateway(gatewayState as GatewaysState, 1)).toEqual(Maybe.just({id: 1}));
    });
    it('returns a maybe.nothing when requesting a non-existing gateway', () => {
      expect(getGateway(gatewayState as GatewaysState, 2)).toEqual(Maybe.nothing());
    });
  });
});
