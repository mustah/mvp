import {NormalizedState} from '../../domainModels';
import {Organisation} from '../organisationModels';
import {getOrganisations} from '../organisationSelectors';

describe('organisationSelectors', () => {
  describe('getOrganisations', () => {
    it('returns organisations as array', () => {
      const organisationState: Partial<NormalizedState<Organisation>> = {
        entities: {1: {id: 1, name: 'elvaco', code: 'elvaco'}, 2: {id: 2, name: 'hif', code: 'hif'}},
        result: [1, 2],
      };

      const expected: Organisation[] = [{id: 1, name: 'elvaco', code: 'elvaco'}, {id: 2, name: 'hif', code: 'hif'}];
      const result: Organisation[] = getOrganisations(organisationState as NormalizedState<Organisation>);

      expect(result).toEqual(expected);
    });
  });
});
