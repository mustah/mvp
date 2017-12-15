import {uuid} from '../../../../types/Types';
import {DomainModel} from '../../domainModels';
import {Gateway, GatewaysState} from '../gatewayModels';
import {getGatewayDataSummary} from '../gatewaySelectors';

type PartialDomainModel = DomainModel<Partial<Gateway>>;

describe('gatewaySelectors', () => {

  describe('getGatewayDataSummary', () => {

    it('aggregates gateway data to a summary structure', () => {
      const gatewayIds: uuid[] = [1, 2, 3];
      const gateways: PartialDomainModel = {
        1: {
          status: {id: 0, name: 'ok'},
          flagged: false,
          city: {id: 'sto', name: 'stockholm'},
          productModel: 'Ci2000',
        },
        2: {
          status: {id: 0, name: 'ok'},
          flagged: false,
          city: {id: 'sto', name: 'stockholm'},
          productModel: 'Cm3000',
        },
        3: {
          status: {id: 0, name: 'ok'},
          flagged: true,
          city: {id: 'got', name: 'göteborg'},
          productModel: 'Cm2000',
        },
      };
      const gatewayState: Partial<GatewaysState> = {
        entities: gateways as DomainModel<Gateway>,
        result: gatewayIds,
      };

      const gatewayDataSummary = getGatewayDataSummary(gatewayState as GatewaysState);

      expect(gatewayDataSummary.get()).toEqual({
        status: {
          0: {name: 'ok', value: 3, filterParam: 0},
        },
        flagged:
          {
            flagged: {name: 'flagged', value: 1, filterParam: true},
            unFlagged: {name: 'unFlagged', value: 2, filterParam: false},
          },
        city:
          {
            sto: {name: 'stockholm', value: 2, filterParam: 'sto'},
            got: {name: 'göteborg', value: 1, filterParam: 'got'},
          },
        productModel: {
          Cm3000: {name: 'Cm3000', value: 1, filterParam: 'Cm3000'},
          Cm2000: {name: 'Cm2000', value: 1, filterParam: 'Cm2000'},
          Ci2000: {name: 'Ci2000', value: 1, filterParam: 'Ci2000'},
        },
      });
    });

    it('is not defined when there are no gateways', () => {
      const gatewayState: Partial<GatewaysState> = {
        entities: {},
        result: [],
      };

      const gatewayDataSummary = getGatewayDataSummary(gatewayState as GatewaysState);

      expect(gatewayDataSummary.isNothing()).toBe(true);
    });
  });
});
