import {Maybe} from '../../../helpers/Maybe';
import {Identifiable} from '../../../types/Types';
import {NormalizedState} from '../domainModels';
import {getAllEntities, getDomainModelById, getFirstDomainModel} from '../domainModelsSelectors';
import {Organisation} from '../organisation/organisationModels';

describe('domainModelsSelectors', () => {

  describe('getDomainModelById - organisation', () => {
    const organisationState: Partial<NormalizedState<Partial<Organisation> & Identifiable>> = {
      entities: {1: {id: 1}},
    };

    it('gets the domain model by id', () => {
      const state = organisationState as NormalizedState<Organisation>;

      expect(getDomainModelById<Organisation>(1)(state)).toEqual(Maybe.just({id: 1}));
    });

    it('returns nothing when domain model with id does not exists', () => {
      const state = organisationState as NormalizedState<Organisation>;

      expect(getDomainModelById<Organisation>(2)(state)).toEqual(Maybe.nothing());
    });
  });

  describe('getFirstDomainModel', () => {

    it('has no domain model', () => {
      expect(getFirstDomainModel({entities: {}, result: []})).toEqual(Maybe.nothing());
    });

    it('has one domain model', () => {
      expect(getFirstDomainModel({entities: {1: {id: 1, name: 'a'}}, result: [1]}).isJust()).toBe(true);
    });
  });

  describe('get all entities', () => {

    it('returns organisations as array', () => {
      const organisationState: Partial<NormalizedState<Organisation>> = {
        entities: {
          1: {id: 1, name: 'elvaco', slug: 'elvaco'},
          2: {id: 2, name: 'hif', slug: 'hif'},
        },
        result: [1, 2],
      };

      const expected: Organisation[] = [{id: 1, name: 'elvaco', slug: 'elvaco'}, {id: 2, name: 'hif', slug: 'hif'}];
      const result: Organisation[] = getAllEntities(organisationState as NormalizedState<Organisation>);

      expect(result).toEqual(expected);
    });

  });

});
