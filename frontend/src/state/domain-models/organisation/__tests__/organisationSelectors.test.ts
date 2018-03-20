import {NormalizedState} from '../../domainModels';
import {Organisation} from '../organisationModels';
import {getOrganisations} from '../organisationSelectors';

describe('organisationSelectors', () => {
  describe('getOrganisations', () => {
    it('returns organisations as array', () => {
      const organisationState: Partial<NormalizedState<Organisation>> = {
        entities: {1: {id: 1, name: 'elvaco', slug: 'elvaco'}, 2: {id: 2, name: 'hif', slug: 'hif'}},
        result: [1, 2],
      };

      const expected: Organisation[] = [{id: 1, name: 'elvaco', slug: 'elvaco'}, {id: 2, name: 'hif', slug: 'hif'}];
      const result: Organisation[] = getOrganisations(organisationState as NormalizedState<Organisation>);

      expect(result).toEqual(expected);
    });
  });
});
