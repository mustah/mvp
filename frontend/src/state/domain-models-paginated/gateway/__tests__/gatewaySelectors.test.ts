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
// describe('gatewaySelectors', () => {
//
//   initLanguage({code: 'en', name: 'english'});
//
//   type PartialDomainModel = ObjectsById<Partial<Gateway> & Identifiable>;
//
//   describe('getGatewayDataSummary', () => {
//
//     it('aggregates gateway data to a summary structure', () => {
//       const status: IdNamed = {id: Status.ok, name: Status.ok};
//       const gateways: PartialDomainModel = {
//         1: {
//           id: 1,
//           status,
//           flagged: false,
//           location: {
//             city: {id: 'sto', name: 'stockholm'},
//             address: {id: 'ad', name: 'ad'},
//             position: {longitude: 14.205929, latitude: 59.666749, confidence: 0.689},
//           },
//           productModel: 'Ci2000',
//         },
//         2: {
//           id: 1,
//           status,
//           flagged: false,
//           location: {
//             city: {id: 'sto', name: 'stockholm'},
//             address: {id: 'kgatan', name: 'kgatan'},
//             position: {longitude: 14.205929, latitude: 59.666749, confidence: 0.67},
//           },
//           productModel: 'Cm3000',
//         },
//         3: {
//           id: 1,
//           status,
//           flagged: true,
//           location: {
//             city: {id: 'got', name: 'göteborg'},
//             address: {id: 'kgatan', name: 'kgatan'},
//             position: {longitude: 14.205929, latitude: 59.666749, confidence: 0.98},
//           },
//           productModel: 'Cm2000',
//         },
//       };
//
//       const gatewayIds: uuid[] = [1, 2, 3];
//
//       const gatewayState: Partial<GatewaysState> = {
//         entities: gateways as ObjectsById<Gateway>,
//         result: gatewayIds,
//       };
//
//       const gatewayDataSummary = getGatewayDataSummary(gatewayState as GatewaysState);
//
//       expect(gatewayDataSummary.get()).toEqual({
//         status:
//           {
//             ok: {name: Status.ok, value: 3, filterParam: Status.ok},
//           },
//         flagged:
//           {
//             flagged: {name: 'flagged', value: 1, filterParam: true},
//             unFlagged: {name: 'unFlagged', value: 2, filterParam: false},
//           },
//         location:
//           {
//             sto: {name: 'stockholm', value: 2, filterParam: 'sto'},
//             got: {name: 'göteborg', value: 1, filterParam: 'got'},
//           },
//         productModel:
//           {
//             Ci2000: {name: 'Ci2000', value: 1, filterParam: 'Ci2000'},
//             Cm3000: {name: 'Cm3000', value: 1, filterParam: 'Cm3000'},
//             Cm2000: {name: 'Cm2000', value: 1, filterParam: 'Cm2000'},
//           },
//       });
//     });
//
//     it('is not defined when there are no gateways', () => {
//       const gatewayState: Partial<GatewaysState> = {
//         entities: {},
//         result: [],
//       };
//
//       const gatewayDataSummary = getGatewayDataSummary(gatewayState as GatewaysState);
//
//       expect(gatewayDataSummary.isNothing()).toBe(true);
//     });
//   });
// });
